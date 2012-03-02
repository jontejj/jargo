package se.j4j.argumentparser.builders;

import se.j4j.argumentparser.handlers.IntegerArithmeticArgument;

public class IntegerArithmeticArgumentBuilder extends ArgumentBuilder<IntegerArithmeticArgumentBuilder, Integer>
{
	private char	operation;

	public IntegerArithmeticArgumentBuilder(final String ... names)
	{
		super(null);
		names(names);
	}

	public IntegerArithmeticArgumentBuilder operation(final char operation)
	{
		this.operation = operation;
		return this;
	}

	@Override
	public Argument<Integer> build()
	{
		handler(new IntegerArithmeticArgument(operation));
		return super.build();
	}
}