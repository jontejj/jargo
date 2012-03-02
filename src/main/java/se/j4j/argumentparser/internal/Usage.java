package se.j4j.argumentparser.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.utils.Lines;
import se.j4j.argumentparser.utils.Strings;

public class Usage
{
	public static String forArguments(final String programName, final Collection<Argument<?>> arguments)
	{
		Usage usage = new Usage(programName, arguments);
		usage.mainUsage(programName);
		List<Argument<?>> sortedArgumentsByName = new ArrayList<Argument<?>>(arguments);
		Collections.sort(sortedArgumentsByName, new ArgumentByName());
		for(Argument<?> arg : sortedArgumentsByName)
		{
			usage.usageForArgument(arg);
		}
		return usage.builder.toString();
	}

	/**
	 * The builder to append usage texts to
	 */
	StringBuilder builder;

	/**
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
	private final Collection<Argument<?>> arguments;

	private static final int CHARACTERS_IN_AVERAGE_ARGUMENT_DESCRIPTION = 30;

	private int expectedUsageTextSize()
	{
		//Two lines for each argument
		return 2 * arguments.size() * (indexOfDescriptionColumn + CHARACTERS_IN_AVERAGE_ARGUMENT_DESCRIPTION);
	}

	private Usage(final String programName, final Collection<Argument<?>> arguments)
	{
		this.arguments = arguments;
		indexOfDescriptionColumn = determineLongestNameColumn() + 4;
		builder = new StringBuilder(expectedUsageTextSize());
	}

	private void mainUsage(final String programName)
	{
		builder.append("Usage: " + programName + (!arguments.isEmpty() ? " [Options]" : ""));
		builder.append(Lines.NEWLINE);
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

	private int lengthOfFirstColumn(final Argument<?> argument)
	{
		int length = 0;
		if(argument.isRequired())
		{
			length += 2;
		}

		for(String name : argument.names())
		{
			length += name.length() + 2;
		}
		return length;
	}

	public static String forSingleArgument(final Argument<?> arg)
	{
		return new Usage("", Arrays.<Argument<?>>asList(arg)).usageForArgument(arg);
	}

	/**
	 * 	"*" indicates a required argument
	 * 		* -test	Test something
	 *         		Valid values:
	 * 		-test	Test something
	 *         		Default: 0
	 */
	private String usageForArgument(final Argument<?> arg)
	{
		if(arg.isRequired())
		{
			builder.append("* ");
		}
		for(String name : arg.names())
		{
			builder.append(name + ", ");
		}
		Strings.appendSpaces(indexOfDescriptionColumn - lengthOfFirstColumn(arg), builder);
		//TODO: handle long descriptions
		builder.append(arg.description());
		builder.append(Lines.NEWLINE);
		Strings.appendSpaces(indexOfDescriptionColumn, builder);
		builder.append(valueExplanation(arg));
		builder.append(Lines.NEWLINE);
		return builder.toString();
	}

	private String valueExplanation(final Argument<?> arg)
	{
		if(arg.isRequired())
		{
			//TODO: return arg.handler.validValues()
			//TODO: handle long value explanations
			return "";
		}
		return "Default: " + arg.defaultValue();
	}

	private static final class ArgumentByName implements Comparator<Argument<?>>
	{
		public int compare(final Argument<?> one, final Argument<?> two)
		{
			if(one.isNamed() && !two.isNamed())
			{
				return -1;
			}
			else if(two.isNamed() && !one.isNamed())
			{
				return 1;
			}
			String name = one.names().get(0);
			return name.compareToIgnoreCase(two.names().get(0));
		}
	}
}
