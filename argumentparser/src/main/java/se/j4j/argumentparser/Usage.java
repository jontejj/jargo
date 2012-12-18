package se.j4j.argumentparser;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Ordering.usingToString;
import static java.lang.Math.max;
import static se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings.IS_INDEXED;
import static se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings.IS_OF_VARIABLE_ARITY;
import static se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings.IS_VISIBLE;
import static se.j4j.argumentparser.ProgramInformation.programName;
import static se.j4j.strings.StringsUtil.NEWLINE;
import static se.j4j.strings.StringsUtil.spaces;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import se.j4j.argumentparser.internal.Texts.UsageTexts;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@NotThreadSafe
final class Usage implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final int CHARACTERS_IN_AVERAGE_ARGUMENT_DESCRIPTION = 40;
	private static final int SPACES_BETWEEN_COLUMNS = 4;
	private static final Joiner NAME_JOINER = Joiner.on(UsageTexts.NAME_SEPARATOR);

	private final transient ImmutableList<Argument<?>> argumentsToPrint;

	/**
	 * Used temporarily to build argumentDescriptions
	 */
	private final transient StringBuilder builder;

	private transient String argumentDescriptions;

	/**
	 * <pre>
	 * For:
	 * -l, --enable-logging Output debug information to standard out
	 * -p, --listen-port    The port clients should connect to.
	 * 
	 * This would be 20.
	 */
	private final transient int indexOfDescriptionColumn;
	private transient boolean needsNewline = false;

	Usage(Set<Argument<?>> arguments)
	{
		Collection<Argument<?>> visibleArguments = filter(arguments, IS_VISIBLE);
		this.argumentsToPrint = copyOf(sortedArguments(visibleArguments));
		this.indexOfDescriptionColumn = determineLongestNameColumn() + SPACES_BETWEEN_COLUMNS;
		this.builder = new StringBuilder(expectedUsageTextSize());
	}

	/**
	 * Constructs Usage from a serialized string, {@code argumentDescriptions} that is
	 */
	Usage(String argumentDescriptions)
	{
		this.argumentDescriptions = argumentDescriptions;

		// These aren't needed when the description already is built
		this.indexOfDescriptionColumn = 0;
		this.builder = null;
		this.argumentsToPrint = null;
	}

	String forProgram(ProgramInformation programInformation)
	{
		return mainUsage(programInformation) + argumentDescriptions();
	}

	String forCommand()
	{
		return commandUsage() + argumentDescriptions();
	}

	private String argumentDescriptions()
	{
		if(argumentDescriptions == null)
		{
			argumentDescriptions = buildArgumentDescriptions();
		}
		return argumentDescriptions;
	}

	private Iterable<Argument<?>> sortedArguments(Collection<Argument<?>> arguments)
	{
		Collection<Argument<?>> indexedArguments = filter(arguments, IS_INDEXED);
		Iterable<Argument<?>> indexedWithoutVariableArity = filter(indexedArguments, not(IS_OF_VARIABLE_ARITY));
		Iterable<Argument<?>> indexedWithVariableArity = filter(indexedArguments, IS_OF_VARIABLE_ARITY);

		List<Argument<?>> sortedArgumentsByName = newArrayList(filter(arguments, not(IS_INDEXED)));
		// TODO: sort in a lexicographical way?
		Collections.sort(sortedArgumentsByName, usingToString());

		return Iterables.concat(indexedWithoutVariableArity, sortedArgumentsByName, indexedWithVariableArity);
	}

	private int determineLongestNameColumn()
	{
		int longestNameSoFar = 0;
		for(Argument<?> arg : argumentsToPrint)
		{
			longestNameSoFar = max(longestNameSoFar, lengthOfNameColumn(arg));
		}
		return longestNameSoFar;
	}

	private int lengthOfNameColumn(final Argument<?> argument)
	{
		int namesLength = 0;

		for(String name : argument.names())
		{
			namesLength += name.length();
		}
		int separatorLength = max(0, UsageTexts.NAME_SEPARATOR.length() * (argument.names().size() - 1));

		int metaLength = argument.metaDescriptionInLeftColumn().length();

		return namesLength + separatorLength + metaLength;
	}

	private int expectedUsageTextSize()
	{
		// Two lines for each argument
		return 2 * argumentsToPrint.size() * (indexOfDescriptionColumn + CHARACTERS_IN_AVERAGE_ARGUMENT_DESCRIPTION);
	}

	private String buildArgumentDescriptions()
	{
		for(Argument<?> arg : argumentsToPrint)
		{
			usageForArgument(arg);
		}
		return builder.toString();
	}

	@Override
	public String toString()
	{
		return mainUsage(programName("")) + argumentDescriptions();
	};

	private String mainUsage(ProgramInformation programInformation)
	{
		String mainUsage = UsageTexts.USAGE_HEADER + programInformation.programName();

		if(hasArguments())
		{
			mainUsage += UsageTexts.ARGUMENT_INDICATOR + NEWLINE;
		}

		mainUsage += programInformation.programDescription();

		if(hasArguments())
		{
			mainUsage += NEWLINE + UsageTexts.ARGUMENT_HEADER + NEWLINE;
		}

		return mainUsage;
	}

	private String commandUsage()
	{
		return hasArguments() ? UsageTexts.ARGUMENT_HEADER + NEWLINE : "";
	}

	private boolean hasArguments()
	{
		return !argumentDescriptions().isEmpty();
	}

	/**
	 * <pre>
	 * 	-foo   Foo something [Required]
	 *         	Valid values: 1 to 5
	 *        -bar   Bar something
	 *         	Default: 0
	 * </pre>
	 */
	@Nonnull
	private void usageForArgument(final Argument<?> arg)
	{
		int lengthBeforeCurrentArgument = builder.length();

		NAME_JOINER.appendTo(builder, arg.names());

		builder.append(arg.metaDescriptionInLeftColumn());

		int lengthOfFirstColumn = builder.length() - lengthBeforeCurrentArgument;
		builder.append(spaces(indexOfDescriptionColumn - lengthOfFirstColumn));

		// TODO: handle long descriptions, names, meta descriptions, default value
		// descriptions
		String description = arg.description();
		if(!description.isEmpty())
		{
			builder.append(description);
			addIndicators(arg);
			needsNewline = true;
			newlineWithIndentation();
			valueExplanation(arg);
		}
		else
		{
			valueExplanation(arg);
			addIndicators(arg);
		}
		builder.append(NEWLINE);
		needsNewline = false;
	}

	private void newlineWithIndentation()
	{
		if(needsNewline)
		{
			builder.append(NEWLINE);
			builder.append(spaces(indexOfDescriptionColumn));
			needsNewline = false;
		}
	}

	private <T> void addIndicators(final Argument<T> arg)
	{
		if(arg.isRequired())
		{
			builder.append(UsageTexts.REQUIRED);
		}
		if(arg.isAllowedToRepeat())
		{
			builder.append(UsageTexts.ALLOWS_REPETITIONS);
		}
	}

	private <T> void valueExplanation(final Argument<T> arg)
	{
		// TODO: handle long value explanations, replace each newline with enough spaces,
		// split up long lines, Use BreakIterator.getLineInstance()?
		String description = arg.descriptionOfValidValues();
		if(!description.isEmpty())
		{
			boolean isCommand = arg.parser() instanceof Command;
			if(isCommand)
			{
				// For commands the validValues is a usage text itself for the command arguments
				// +1 = indentation so that command options are tucked under the command
				String spaces = spaces(indexOfDescriptionColumn + 1);
				description = description.replace(NEWLINE, NEWLINE + spaces);
			}
			else
			{
				String meta = arg.metaDescriptionInRightColumn();
				builder.append(meta + ": ");
			}

			builder.append(description);
			needsNewline = true;
		}
		if(arg.isRequired())
			return;

		String descriptionOfDefaultValue = arg.defaultValueDescription();
		if(descriptionOfDefaultValue != null)
		{
			newlineWithIndentation();
			String spaces = spaces(indexOfDescriptionColumn + UsageTexts.DEFAULT_VALUE_START.length());
			descriptionOfDefaultValue = descriptionOfDefaultValue.replace(NEWLINE, NEWLINE + spaces);

			builder.append(UsageTexts.DEFAULT_VALUE_START).append(descriptionOfDefaultValue);
		}
	}

	private static final class SerializationProxy implements Serializable
	{
		/**
		 * All arguments described. Constructed lazily when serialized.
		 * 
		 * @serial
		 */
		private final String argumentDescriptions;

		private static final long serialVersionUID = 1L;

		private SerializationProxy(Usage usage)
		{
			argumentDescriptions = usage.argumentDescriptions();
		}

		private Object readResolve()
		{
			return new Usage(argumentDescriptions);
		}
	}

	private Object writeReplace()
	{
		return new SerializationProxy(this);
	}
}
