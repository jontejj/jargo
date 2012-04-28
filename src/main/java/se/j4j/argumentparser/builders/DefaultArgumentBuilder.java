package se.j4j.argumentparser.builders;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import se.j4j.argumentparser.ArgumentBuilder;
import se.j4j.argumentparser.interfaces.ArgumentHandler;

@NotThreadSafe
public class DefaultArgumentBuilder<T> extends ArgumentBuilder<DefaultArgumentBuilder<T>, T>
{
	public DefaultArgumentBuilder(final @Nonnull ArgumentHandler<T> handler)
	{
		super(handler);
	}
}
