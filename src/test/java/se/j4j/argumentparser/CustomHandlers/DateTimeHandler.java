package se.j4j.argumentparser.CustomHandlers;

import static se.j4j.argumentparser.ArgumentFactory.customArgument;

import org.joda.time.DateTime;

import se.j4j.argumentparser.builders.DefaultArgumentBuilder;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.interfaces.StringConverter;

public class DateTimeHandler implements StringConverter<DateTime>
{
	@Override
	public String descriptionOfValidValues()
	{
		return "An ISO8601 date, such as 2011-02-28";
	}

	@Override
	public DateTime convert(final String value) throws ArgumentException
	{
		try
		{
			return DateTime.parse(value);
		}
		catch(IllegalArgumentException wrongDateFormat)
		{
			throw InvalidArgument.create(value, wrongDateFormat.getLocalizedMessage());
		}
	}

	@Override
	public DateTime defaultValue()
	{
		return DateTime.now();
	}

	public static DefaultArgumentBuilder<DateTime> dateArgument(String ... names)
	{
		return customArgument(new DateTimeHandler()).metaDescription("date").defaultValueDescription("Current time").names(names);
	}
}
