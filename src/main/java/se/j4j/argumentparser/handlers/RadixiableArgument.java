package se.j4j.argumentparser.handlers;

import se.j4j.argumentparser.ArgumentHandler;


public abstract class RadixiableArgument<T extends Number> implements ArgumentHandler<T>
{
	private int radix = 10;

	public RadixiableArgument<T> radix(final int radix)
	{
		this.radix = radix;
		return this;
	}

	protected int radix()
	{
		return radix;
	}

}
