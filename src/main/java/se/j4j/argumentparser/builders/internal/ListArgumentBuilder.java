package se.j4j.argumentparser.builders.internal;

import java.util.List;

import se.j4j.argumentparser.builders.ArgumentBuilder;
import se.j4j.argumentparser.handlers.internal.ListArgument;

public class ListArgumentBuilder<T> extends ArgumentBuilder<ListArgumentBuilder<T>, List<T>>
{
	public ListArgumentBuilder(final ArgumentBuilder<? extends ArgumentBuilder<?,T>, T> builder, final int argumentsToConsume)
	{
		super(new ListArgument<T>(builder.handler, argumentsToConsume));
		copy(builder);
	}
}