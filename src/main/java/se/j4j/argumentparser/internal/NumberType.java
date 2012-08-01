package se.j4j.argumentparser.internal;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.StringParsers.Radix;

/**
 * A class that exposes static fields, such as {@link Integer#SIZE}, for subclasses of
 * {@link Number} in an object oriented way
 * 
 * @param <T> the subclass of {@link Number}
 */
public abstract class NumberType<T extends Number & Comparable<T>>
{
	// Only allow classes in this package to inherit
	NumberType()
	{
	}

	public static final NumberType<Long> LONG = new LongType();
	public static final NumberType<Byte> BYTE = new ByteType();
	public static final NumberType<Integer> INTEGER = new IntegerType();
	public static final NumberType<Short> SHORT = new ShortType();

	public abstract T minValue();

	public abstract T maxValue();

	public abstract T fromLong(Long value);

	public abstract Long toLong(T value);

	/**
	 * @return Number of bits needed to represent {@code T}
	 */
	public abstract int bitSize();

	/**
	 * @return a mask where the sign for the type {@code T} is set and nothing else
	 */
	public long signBitMask()
	{
		return (long) 1 << (bitSize() - 1);
	}

	public abstract String name();

	public final T defaultValue()
	{
		return fromLong(0L);
	}

	/**
	 * <pre>
	 * Converts {@code value} (assuming that it's in the given {@code radix}) into a
	 * {@code T} value.
	 * 
	 * For instance:
	 * {@code Integer fortyTwo = NumberType.INTEGER.parse("42", Radix.DECIMAL);}
	 * 
	 * @throws InvalidArgument if the value is too big or in the wrong radix
	 * </pre>
	 */
	public T parse(String value, Radix radix) throws ArgumentException
	{
		return radix.parse(value, this);
	}

	public String toString(T value, Radix radix)
	{
		return radix.toString(value, this);
	}

	/**
	 * @return true if {@code value} can be represented with this type without overflowing
	 */
	public boolean inRange(Long value)
	{
		long minValue = minValue().longValue();
		long maxValue = maxValue().longValue();

		return value.compareTo(minValue) >= 0 && value.compareTo(maxValue) <= 0;
	}

	@Override
	public String toString()
	{
		return name();
	}

	private static final class ByteType extends NumberType<Byte>
	{
		@Override
		public Byte minValue()
		{
			return Byte.MIN_VALUE;
		}

		@Override
		public Byte maxValue()
		{
			return Byte.MAX_VALUE;
		}

		@Override
		public Byte fromLong(Long value)
		{
			return value.byteValue();
		}

		@Override
		public Long toLong(Byte value)
		{
			return value.longValue();
		}

		@Override
		public int bitSize()
		{
			return Byte.SIZE;
		}

		@Override
		public String name()
		{
			return "byte";
		}
	}

	private static final class IntegerType extends NumberType<Integer>
	{
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
			return Integer.SIZE;
		}

		@Override
		public String name()
		{
			return "integer";
		}
	}

	private static final class ShortType extends NumberType<Short>
	{
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
		public Short fromLong(Long value)
		{
			return value.shortValue();
		}

		@Override
		public Long toLong(Short value)
		{
			return value.longValue();
		}

		@Override
		public int bitSize()
		{
			return Short.SIZE;
		}

		@Override
		public String name()
		{
			return "short";
		}
	}

	private static final class LongType extends NumberType<Long>
	{
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
		public Long fromLong(Long value)
		{
			return value;
		}

		@Override
		public Long toLong(Long value)
		{
			return value;
		}

		@Override
		public int bitSize()
		{
			return Long.SIZE;
		}

		@Override
		public String name()
		{
			return "long";
		}

		@Override
		public String toString(Long value, Radix radix)
		{
			return radix.toString(value);
		}
	}
}
