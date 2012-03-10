package se.j4j.argumentparser.handlers;

public abstract class RadixiableArgument<T extends Number> extends OneParameterArgument<T>
{
	private int radix = 10;

	/**
	 * TODO: make this available easily
	 * @param theRadix
	 * @return
	 */
	public RadixiableArgument<T> radix(final int theRadix)
	{
		radix = theRadix;
		return this;
	}

	protected int radix()
	{
		return radix;
	}

}
