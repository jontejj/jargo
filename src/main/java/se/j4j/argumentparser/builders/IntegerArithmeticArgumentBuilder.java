package se.j4j.argumentparser.builders;

import javax.annotation.concurrent.NotThreadSafe;

import se.j4j.argumentparser.handlers.IntegerArithmeticArgument;

@NotThreadSafe
public class IntegerArithmeticArgumentBuilder extends ArgumentBuilder<IntegerArithmeticArgumentBuilder, Integer>
{
	private char	operation;

	public IntegerArithmeticArgumentBuilder()
	{
		super(null);
	}

	public IntegerArithmeticArgumentBuilder operation(final char anOperation)
	{
		operation = anOperation;
		return this;
	}

	@Override
	public Argument<Integer> build()
	{
		handler(new IntegerArithmeticArgument(operation));
		return super.build();
	}
}