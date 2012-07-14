package se.j4j.argumentparser.stringparsers.custom;

import static se.j4j.argumentparser.ArgumentExceptions.forInvalidValue;
import static se.j4j.argumentparser.ArgumentFactory.withParser;

import org.joda.time.DateTime;

import se.j4j.argumentparser.ArgumentBuilder.DefaultArgumentBuilder;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.StringParser;

public class DateTimeParser implements StringParser<DateTime>
{
	@Override
	public String descriptionOfValidValues()
	{
		return "an ISO8601 date, such as 2011-02-28";
	}

	@Override
	public DateTime parse(final String value) throws ArgumentException
	{
		try
		{
			return DateTime.parse(value);
		}
		catch(IllegalArgumentException wrongDateFormat)
		{
			throw forInvalidValue(value, wrongDateFormat.getLocalizedMessage());
		}
	}

	@Override
	public DateTime defaultValue()
	{
		return DateTime.now();
	}

	public static DefaultArgumentBuilder<DateTime> dateArgument(String ... names)
	{
		return withParser(new DateTimeParser()).defaultValueDescription("Current time").names(names);
	}

	@Override
	public String metaDescription()
	{
		return "date";
	}
}
