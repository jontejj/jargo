package se.j4j.argumentparser.argumentbuilder;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.StringParser;

public class FooParser implements StringParser<Foo>
{
	private final int bar;

	public FooParser(final int aBar)
	{
		bar = aBar;
	}

	@Override
	public Foo parse(String argument) throws ArgumentException
	{
		return new Foo(argument, bar);
	}

	@Override
	public String descriptionOfValidValues()
	{
		return "foos";
	}

	@Override
	public Foo defaultValue()
	{
		return new Foo("", bar);
	}

	@Override
	public String metaDescription()
	{
		return "foo";
	}

}
