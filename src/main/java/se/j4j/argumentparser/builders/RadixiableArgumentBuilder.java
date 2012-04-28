package se.j4j.argumentparser.builders;

import se.j4j.argumentparser.ArgumentBuilder;
import se.j4j.argumentparser.handlers.RadixiableArgument.Radix;

public abstract class RadixiableArgumentBuilder<T extends Number> extends ArgumentBuilder<RadixiableArgumentBuilder<T>, T>
{
	private Radix radix = Radix.DECIMAL;

	protected RadixiableArgumentBuilder()
	{
		super(null);
	}

	public RadixiableArgumentBuilder<T> radix(final Radix aRadix)
	{
		radix = aRadix;
		return this;
	}

	protected Radix radix()
	{
		return radix;
	}
}
