package se.j4j.argumentparser.internal;


public final class OneBitType extends NumberType<Integer>
{
	@Override
	public Integer minValue()
	{
		return 0;
	}

	@Override
	public Integer maxValue()
	{
		return 1;
	}

	@Override
	public Integer cast(Long value)
	{
		return value.intValue();
	}

	@Override
	public Long cast(Integer value)
	{
		return value.longValue();
	}

	@Override
	public int bitSize()
	{
		return 1;
	}

	@Override
	public String name()
	{
		return null;
	}
}
