package se.j4j.argumentparser.stringparsers.custom;

import static se.j4j.argumentparser.ArgumentFactory.withParser;

import java.util.Locale;

import se.j4j.argumentparser.ArgumentBuilder.DefaultArgumentBuilder;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.StringParser;

public class ObjectParser implements StringParser<Object>
{
	@Override
	public Object parse(String argument, Locale locale) throws ArgumentException
	{
		return argument;
	}

	@Override
	public String descriptionOfValidValues(Locale locale)
	{
		return "Any string";
	}

	@Override
	public Object defaultValue()
	{
		return 0;
	}

	@Override
	public String metaDescription()
	{
		return "<an-object>";
	}

	public static DefaultArgumentBuilder<Object> objectArgument()
	{
		return withParser(new ObjectParser());
	}

}
