package se.j4j.argumentparser.internal;

/**
 * To cover the edge case where a type has a {@link #bitSize()} of 1
 */
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
	public Integer fromLong(Long value)
	{
		return value.intValue();
	}

	@Override
	public Long toLong(Integer value)
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
