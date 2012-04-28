package se.j4j.argumentparser.handlers;

public class IntegerArgument extends RadixiableArgument<Integer>
{
	public IntegerArgument(final Radix radix)
	{
		super(radix);
	}

	@Override
	public Integer minValue()
	{
		return Integer.MIN_VALUE;
	}

	@Override
	public Integer maxValue()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	protected String toBinaryString(final Integer value)
	{
		return Integer.toBinaryString(value);
	}

	@Override
	public Integer defaultValue()
	{
		return 0;
	}

	@Override
	protected long signBitMask()
	{
		return 1 << (Integer.SIZE - 1);
	}

	@Override
	protected Integer cast(Long value)
	{
		return value.intValue();
	}
}
