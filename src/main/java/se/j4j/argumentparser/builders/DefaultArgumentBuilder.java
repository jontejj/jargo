package se.j4j.argumentparser.builders;

import se.j4j.argumentparser.ArgumentHandler;

public class DefaultArgumentBuilder<T> extends ArgumentBuilder<DefaultArgumentBuilder<T>, T>
{
	public DefaultArgumentBuilder(final ArgumentHandler<T> handler)
	{
		super(handler);
	}
}
