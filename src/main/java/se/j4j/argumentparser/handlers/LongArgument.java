package se.j4j.argumentparser.handlers;

public class LongArgument extends RadixiableArgument<Long>
{
	public LongArgument(final Radix radix)
	{
		super(radix);
	}

	@Override
	public Long minValue()
	{
		return Long.MIN_VALUE;
	}

	@Override
	public Long maxValue()
	{
		return Long.MAX_VALUE;
	}

	@Override
	protected String toBinaryString(final Long value)
	{
		return Long.toBinaryString(value);
	}

	@Override
	public Long defaultValue()
	{
		return 0L;
	}

	@Override
	protected long signBitMask()
	{
		return 1 << (Long.SIZE - 1);
	}

	@Override
	protected Long cast(Long value)
	{
		return value;
	}

}
