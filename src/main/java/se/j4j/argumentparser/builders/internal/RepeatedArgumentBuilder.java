package se.j4j.argumentparser.builders.internal;

import java.util.List;

import se.j4j.argumentparser.builders.ArgumentBuilder;
import se.j4j.argumentparser.handlers.internal.RepeatedArgument;

public class RepeatedArgumentBuilder<T> extends ArgumentBuilder<ListArgumentBuilder<T>, List<T>>
{
	public RepeatedArgumentBuilder(final ArgumentBuilder<? extends ArgumentBuilder<?,T>, T> builder)
	{
		super(new RepeatedArgument<T>(builder.handler));
		copy(builder);
	}

	/**
	 * This method should be called before repeated()
	 */
	@Deprecated
	@Override
	public ListArgumentBuilder<List<T>> arity(final int numberOfParameters)
	{
		throw new IllegalStateException("Programmer Error. Call arity(...) before repeated()");
	}

	/**
	 * This method should be called before repeated()
	 */
	@Override
	@Deprecated
	public ListArgumentBuilder<List<T>> consumeAll()
	{
		throw new IllegalStateException("Programmer Error. Call consumeAll(...) before repeated()");
	}
}