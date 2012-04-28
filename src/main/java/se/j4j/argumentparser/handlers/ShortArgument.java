package se.j4j.argumentparser.handlers;

public class ShortArgument extends RadixiableArgument<Short>
{
	public ShortArgument(final Radix radix)
	{
		super(radix);
	}

	@Override
	public Short minValue()
	{
		return Short.MIN_VALUE;
	}

	@Override
	public Short maxValue()
	{
		return Short.MAX_VALUE;
	}

	@Override
	protected String toBinaryString(final Short value)
	{
		return Integer.toBinaryString(value);
	}

	@Override
	public Short defaultValue()
	{
		return 0;
	}

	@Override
	protected long signBitMask()
	{
		return 1 << (Short.SIZE - 1);
	}

	@Override
	protected Short cast(Long value)
	{
		return value.shortValue();
	}
}
