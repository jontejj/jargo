package se.j4j.argumentparser.internal;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.utils.Lines;
import se.j4j.argumentparser.utils.Strings;

import com.google.common.base.Joiner;

public class Usage
{
	@CheckReturnValue
	@Nonnull
	public static Usage forArguments(final @Nonnull String programName, final @Nonnull Collection<Argument<?>> arguments)
	{
		Usage usage = new Usage(arguments);
		usage.mainUsage(programName);
		List<Argument<?>> sortedArgumentsByName = new ArrayList<Argument<?>>(arguments);
		Collections.sort(sortedArgumentsByName, new ArgumentByName());
		for(Argument<?> arg : sortedArgumentsByName)
		{
			usage.usageForArgument(arg);
		}
		return usage;
	}

	/**
	 * Print usage on System.out
	 */
	public void print()
	{
		System.out.println(builder.toString());
	}

	@Override
	public String toString()
	{
		return builder.toString();
	};

	/**
	 * The builder to append usage texts to
	 */
	@Nonnull StringBuilder builder;

	/**
	 * <pre>
	 * For:
	 * -l, --enable-logging Output debug information to standard out
	 * -p, --listen-port    The port clients should connect to.
	 * 
	 * This would be 20.
	 */
	final int indexOfDescriptionColumn;

	/**
	 * The arguments to describe
	 */
	private final @Nonnull Collection<Argument<?>> arguments;

	private static final int CHARACTERS_IN_AVERAGE_ARGUMENT_DESCRIPTION = 30;

	private int expectedUsageTextSize()
	{
		// Two lines for each argument
		return 2 * arguments.size() * (indexOfDescriptionColumn + CHARACTERS_IN_AVERAGE_ARGUMENT_DESCRIPTION);
	}

	private Usage(final @Nonnull Collection<Argument<?>> arguments)
	{
		this.arguments = arguments;
		indexOfDescriptionColumn = determineLongestNameColumn() + 4;
		builder = new StringBuilder(expectedUsageTextSize());
	}

	private void mainUsage(final @Nonnull String programName)
	{
		builder.append("Usage: " + programName);
		if(!arguments.isEmpty())
		{
			builder.append(" [Options]");
			builder.append(Lines.NEWLINE);
		}

	}

	private int determineLongestNameColumn()
	{
		int longestNames = 0;
		for(Argument<?> arg : arguments)
		{
			int length = lengthOfFirstColumn(arg);
			if(length > longestNames)
			{
				longestNames = length;
			}
		}
		return longestNames;
	}

	private int lengthOfFirstColumn(final @Nonnull Argument<?> argument)
	{
		if(argument.shouldBeHiddenInUsage())
			return 0;

		int namesLength = 0;

		for(String name : argument.names())
		{
			namesLength += name.length();
		}
		int separatorLength = max(0, NAME_SEPARATOR.length() * (argument.names().size() - 1));

		int metaLength = argument.metaDescription().length();

		return namesLength + separatorLength + metaLength;
	}

	@CheckReturnValue
	@Nonnull
	public static String forSingleArgument(final @Nonnull Argument<?> arg)
	{
		return new Usage(Arrays.<Argument<?>>asList(arg)).usageForArgument(arg);
	}

	/**
	 * <pre>
	 * 	-test   Test something [Required]
	 *         	Valid values: 1 to 5
	 *        -test   Test something
	 *         	Default: 0
	 * </pre>
	 */
	@Nonnull
	private String usageForArgument(final @Nonnull Argument<?> arg)
	{
		if(arg.shouldBeHiddenInUsage())
			return "";

		int lengthOfFirstColumn = lengthOfFirstColumn(arg);

		Joiner.on(NAME_SEPARATOR).appendTo(builder, arg.names());

		builder.append(arg.metaDescription());

		Strings.appendSpaces(indexOfDescriptionColumn - lengthOfFirstColumn, builder);
		// TODO: handle long descriptions
		// TODO: handle arity
		if(!arg.description().isEmpty())
		{
			builder.append(arg.description());
			addIndicators(arg);
			builder.append(Lines.NEWLINE);
			Strings.appendSpaces(indexOfDescriptionColumn, builder);
		}
		else
		{
			addIndicators(arg);
		}
		builder.append(valueExplanation(arg));
		builder.append(Lines.NEWLINE);
		return builder.toString();
	}

	private <T> void addIndicators(final Argument<T> arg)
	{
		if(arg.isRequired())
		{
			builder.append(" [Required]");
		}
		if(arg.isAllowedToRepeat())
		{
			builder.append(" [Supports Multiple occurences]");
		}
	}

	private <T> StringBuilder valueExplanation(final Argument<T> arg)
	{
		// TODO: handle long value explanations
		StringBuilder valueExplanation = new StringBuilder();
		String description = arg.handler().descriptionOfValidValues();
		if(!description.isEmpty())
		{
			if(arg.metaDescription().isEmpty())
			{
				valueExplanation.append("Valid input: ");
			}
			else
			{
				valueExplanation.append(arg.metaDescription() + ": ");
			}

			valueExplanation.append(description);
			valueExplanation.append(Lines.NEWLINE);
			Strings.appendSpaces(indexOfDescriptionColumn, valueExplanation);
		}
		if(arg.isRequired())
			return valueExplanation;

		String descriptionOfDefaultValue = arg.defaultValueDescription();
		if(descriptionOfDefaultValue == null)
		{
			descriptionOfDefaultValue = arg.handler().describeValue(arg.defaultValue());
		}
		if(descriptionOfDefaultValue != null)
		{
			valueExplanation.append("Default: ");
			valueExplanation.append(descriptionOfDefaultValue);
			valueExplanation.append(Lines.NEWLINE);
			Strings.appendSpaces(indexOfDescriptionColumn, valueExplanation);
		}

		return valueExplanation;
	}

	private static final class ArgumentByName implements Comparator<Argument<?>>
	{
		@Override
		public int compare(final Argument<?> one, final Argument<?> two)
		{
			if(one.isNamed() && !two.isNamed())
				return -1;
			else if(two.isNamed() && !one.isNamed())
				return 1;

			String name = one.names().get(0);
			return name.compareToIgnoreCase(two.names().get(0));
		}
	}

	private static final String NAME_SEPARATOR = ", ";
}
