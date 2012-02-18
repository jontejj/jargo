package se.j4j.argumentparser.handlers;

public abstract class RadixiableArgument<T extends Number> extends OneParameterArgument<T>
{
	private int radix = 10;

	/**
	 * TODO: make this available easily
	 * @param radix
	 * @return
	 */
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
