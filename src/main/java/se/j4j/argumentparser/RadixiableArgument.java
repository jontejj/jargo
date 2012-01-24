package se.j4j.argumentparser;


public abstract class RadixiableArgument<T extends Number> extends Argument<T>
{

	private int radix = 10;

	RadixiableArgument(final String ... names)
	{
		super(names);
	}

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
