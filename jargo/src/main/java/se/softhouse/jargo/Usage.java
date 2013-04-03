/* Copyright 2013 Jonatan Jönsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package se.softhouse.jargo;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static java.lang.Math.max;
import static se.softhouse.common.strings.StringsUtil.NEWLINE;
import static se.softhouse.common.strings.StringsUtil.spaces;
import static se.softhouse.jargo.Argument.IS_INDEXED;
import static se.softhouse.jargo.Argument.IS_OF_VARIABLE_ARITY;
import static se.softhouse.jargo.Argument.IS_VISIBLE;

import java.io.Serializable;
import java.text.BreakIterator;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.annotation.concurrent.NotThreadSafe;

import se.softhouse.common.strings.StringBuilders;
import se.softhouse.jargo.internal.Texts.UsageTexts;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Responsible for formatting usage texts for {@link CommandLineParser#usage()} and
 * {@link Arguments#helpArgument(String, String...)}.
 * Using it is often as simple as:
 * 
 * <pre class="prettyprint">
 * <code class="language-java">
 * System.out.print(CommandLineParser.withArguments(someArguments).usage());
 * </code>
 * </pre>
 * 
 * <pre>
 * Sorts {@link Argument}s in the following order:
 * <ol>
 *   <li>{@link ArgumentBuilder#names(String...) indexed arguments} without a {@link ArgumentBuilder#variableArity() variable arity}</li>
 * 	 <li>By their {@link ArgumentBuilder#names(String...) first name}  in an alphabetical order</li>
 * 	 <li>The remaining {@link ArgumentBuilder#names(String...) indexed arguments} that are of {@link ArgumentBuilder#variableArity() variable arity}</li>
 * </ol>
 * </pre>
 */
@NotThreadSafe
public final class Usage implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final class Row
	{
		StringBuilder nameColumn = new StringBuilder();
		StringBuilder descriptionColumn = new StringBuilder();

		@Override
		public String toString()
		{
			return nameColumn.toString() + " " + descriptionColumn.toString();
		}
	}

	private transient final Collection<Argument<?>> unfilteredArguments;
	private transient String message = "";
	private transient final Locale locale;
	private transient final ProgramInformation program;
	private transient final boolean forCommand;
	private transient String fromSerializedUsage = null;
	// TODO(jontejj): try getting the correct value automatically, if not possible fall back to 80
	private transient int columnWidth = 80;

	// Lazily initialized members (for performance)

	/**
	 * <pre>
	 * For:
	 * -l, --enable-logging Output debug information to standard out
	 * -p, --listen-port    The port clients should connect to.
	 * 
	 * This would be 20.
	 * </pre>
	 */
	private transient int indexOfDescriptionColumn;
	private transient ImmutableList<Argument<?>> argumentsToPrint;

	Usage(Collection<Argument<?>> arguments, Locale locale, ProgramInformation program, boolean forCommand)
	{
		this.unfilteredArguments = arguments;
		this.locale = locale;
		this.program = program;
		this.forCommand = forCommand;
	}

	private Usage(String fromSerializedUsage)
	{
		// All these are unused as the usage is already constructed
		this(null, null, null, false);
		this.fromSerializedUsage = fromSerializedUsage;
	}

	/**
	 * An optional message to print before any usage
	 */
	Usage withMessage(String aMessage)
	{
		this.message = aMessage;
		return this;
	}

	/**
	 * Returns the usage text that's suitable to print on {@link System#out}.
	 */
	@Override
	public String toString()
	{
		return message + usage();
	}

	private String usage()
	{
		if(fromSerializedUsage != null)
			return fromSerializedUsage;
		init();

		StringBuilder builder = newStringBuilder();
		builder.append(header());

		List<Row> rows = newArrayListWithExpectedSize(argumentsToPrint.size());
		for(Argument<?> arg : argumentsToPrint)
		{
			Row forArgument = usageForArgument(arg);
			rows.add(forArgument);
		}
		printRowsOn(rows, builder);
		return builder.toString();
	}

	private String header()
	{
		if(forCommand) // Commands get their header from their meta description
			return hasArguments() ? NEWLINE : "";

		String mainUsage = UsageTexts.USAGE_HEADER + program.programName();

		if(hasArguments())
		{
			mainUsage += UsageTexts.ARGUMENT_INDICATOR;
		}

		mainUsage += NEWLINE + wordWrap(program.programDescription(), 0, columnWidth);

		if(hasArguments())
		{
			mainUsage += NEWLINE + UsageTexts.ARGUMENT_HEADER + ":" + NEWLINE;
		}

		return mainUsage;
	}

	private static final int SPACES_BETWEEN_COLUMNS = 4;

	private void init()
	{
		Collection<Argument<?>> visibleArguments = filter(unfilteredArguments, IS_VISIBLE);
		this.argumentsToPrint = copyOf(sortedArguments(visibleArguments));
		this.indexOfDescriptionColumn = determineLongestNameColumn() + SPACES_BETWEEN_COLUMNS;
	}

	private Iterable<Argument<?>> sortedArguments(Collection<Argument<?>> arguments)
	{
		Collection<Argument<?>> indexedArguments = filter(arguments, IS_INDEXED);
		Iterable<Argument<?>> indexedWithoutVariableArity = filter(indexedArguments, not(IS_OF_VARIABLE_ARITY));
		Iterable<Argument<?>> indexedWithVariableArity = filter(indexedArguments, IS_OF_VARIABLE_ARITY);

		List<Argument<?>> sortedArgumentsByName = newArrayList(filter(arguments, not(IS_INDEXED)));
		Collections.sort(sortedArgumentsByName, Argument.NAME_COMPARATOR);

		return Iterables.concat(indexedWithoutVariableArity, sortedArgumentsByName, indexedWithVariableArity);
	}

	private int determineLongestNameColumn()
	{
		int longestNameSoFar = 0;
		for(Argument<?> arg : argumentsToPrint)
		{
			longestNameSoFar = max(longestNameSoFar, lengthOfNameColumn(arg));
		}
		return Math.min(longestNameSoFar, maxNameColumnWidth());
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

	/**
	 * A minimum of 1 third must be available to print descriptions on
	 */
	private int maxNameColumnWidth()
	{
		return columnWidth / 3 * 2;
	}

	private StringBuilder newStringBuilder()
	{
		// Two lines for each argument
		return StringBuilders.withExpectedSize(2 * argumentsToPrint.size() * columnWidth);
	}

	private boolean hasArguments()
	{
		return !argumentsToPrint.isEmpty();
	}

	private static final Joiner NAME_JOINER = Joiner.on(UsageTexts.NAME_SEPARATOR);

	/**
	 * <pre>
	 * 	-foo   Foo something [Required]
	 *         	Valid values: 1 to 5
	 *        -bar   Bar something
	 *         	Default: 0
	 * </pre>
	 */
	private Row usageForArgument(final Argument<?> arg)
	{
		Row row = new Row();

		// Left column: name <meta>
		NAME_JOINER.appendTo(row.nameColumn, arg.names());
		row.nameColumn.append(arg.metaDescriptionInLeftColumn());

		// Right column: description of what the argument means [indicators]
		// <meta>: description of valid values
		// Default: default value
		String description = arg.description();
		if(!description.isEmpty())
		{
			row.descriptionColumn.append(wordWrap(description, indexOfDescriptionColumn, columnWidth));
			addIndicators(arg, row.descriptionColumn);
			row.descriptionColumn.append(NEWLINE);
			valueExplanation(arg, row.descriptionColumn);
		}
		else
		{
			valueExplanation(arg, row.descriptionColumn);
			addIndicators(arg, row.descriptionColumn);
		}
		return row;
	}

	private <T> void addIndicators(final Argument<T> arg, StringBuilder target)
	{
		if(arg.isRequired())
		{
			target.append(UsageTexts.REQUIRED);
		}
		if(arg.isAllowedToRepeat())
		{
			target.append(UsageTexts.ALLOWS_REPETITIONS);
		}
	}

	private <T> void valueExplanation(final Argument<T> arg, StringBuilder target)
	{
		String validValuesDescription = arg.descriptionOfValidValues(locale);
		if(!validValuesDescription.isEmpty())
		{
			String meta = arg.metaDescriptionInRightColumn();
			target.append(meta + ": ").append(validValuesDescription);
		}
		if(arg.isRequired())
			return;

		String descriptionOfDefaultValue = arg.defaultValueDescription(locale);
		if(descriptionOfDefaultValue != null)
		{
			if(!validValuesDescription.isEmpty())
			{
				target.append(NEWLINE);
			}
			String spaces = spaces(UsageTexts.DEFAULT_VALUE_START.length());
			descriptionOfDefaultValue = descriptionOfDefaultValue.replace(NEWLINE, NEWLINE + spaces);
			target.append(UsageTexts.DEFAULT_VALUE_START).append(descriptionOfDefaultValue);
		}
	}

	private StringBuilder wordWrap(String value, int startingLength, int maxLength)
	{
		// TODO(jontejj): move into StringsUtil?
		StringBuilder result = new StringBuilder(value.length());
		BreakIterator boundary = BreakIterator.getLineInstance(locale);
		boundary.setText(value);
		int start = boundary.first();
		int end = boundary.next();
		int lineLength = startingLength;

		while(end != BreakIterator.DONE)
		{
			String word = value.substring(start, end);
			lineLength = lineLength + word.length();
			if(lineLength >= maxLength)
			{
				result.append(NEWLINE);
				lineLength = startingLength;
			}
			result.append(word);
			start = end;
			end = boundary.next();
		}
		return result;
	}

	// TODO: refactor and expose Appendable alternative
	private void printRowsOn(Iterable<Row> rows, StringBuilder target)
	{
		for(Row row : rows)
		{
			StringBuilder nameColumn = wordWrap(row.nameColumn.toString(), 0, indexOfDescriptionColumn);
			String descriptionColumn = row.descriptionColumn.toString();
			Iterable<String> nameLines = Splitter.on(NEWLINE).split(nameColumn);
			Iterator<String> descriptionLines = Splitter.on(NEWLINE).split(descriptionColumn).iterator();
			for(String nameLine : nameLines)
			{
				target.append(nameLine);
				if(descriptionLines.hasNext())
				{
					int lengthOfNameColumn = nameLine.length();
					target.append(spaces(indexOfDescriptionColumn - lengthOfNameColumn));
					target.append(descriptionLines.next());
				}
				target.append(NEWLINE);
			}
			while(descriptionLines.hasNext())
			{
				target.append(spaces(indexOfDescriptionColumn));
				target.append(descriptionLines.next());
				target.append(NEWLINE);
			}

		}
	}

	private static final class SerializationProxy implements Serializable
	{
		/**
		 * @serial all arguments described. Constructed lazily when serialized.
		 */
		private final String serializedUsage;

		private static final long serialVersionUID = 1L;

		private SerializationProxy(Usage usage)
		{
			// TODO(jontejj): how to support calling withConsoleWidth() after serialization? Some
			// kind of marker where the break iterator did something?
			serializedUsage = usage.usage();
		}

		private Object readResolve()
		{
			return new Usage(serializedUsage);
		}
	}

	private Object writeReplace()
	{
		return new SerializationProxy(this);
	}
}
