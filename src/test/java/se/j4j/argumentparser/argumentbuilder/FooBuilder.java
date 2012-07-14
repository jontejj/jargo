package se.j4j.argumentparser.argumentbuilder;

import se.j4j.argumentparser.ArgumentBuilder;
import se.j4j.argumentparser.StringParser;

public class FooBuilder extends ArgumentBuilder<FooBuilder, Foo>
{
	private int bar;

	public FooBuilder bar(int barToSetOnCreatedFoos)
	{
		bar = barToSetOnCreatedFoos;
		return this;
	}

	@Override
	protected StringParser<Foo> parser()
	{
		return new FooParser(bar);
	}
}
