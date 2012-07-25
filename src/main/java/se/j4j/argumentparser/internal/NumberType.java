package se.j4j.argumentparser.internal;

import java.util.Locale;

import se.j4j.argumentparser.StringParsers.Radix;

import com.google.common.annotations.Beta;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.primitives.UnsignedLongs;

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
	 * @return Number of bits needed to represent <code>T</code>
	 */
	public abstract int bitSize();

	/**
	 * @return one bit where the sign for the type <code>T</code> is
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

	public String toString(T value, Radix radix)
	{
		// TODO: use strategy pattern instead
		switch(radix)
		{
			case BINARY:
				return toBinaryString(value);
			default:
				return describeNonBinaryValueWithRadix(value, radix);
		}
	}

	private String toBinaryString(T tValue)
	{
		long value = toLong(tValue);

		final int size = bitSize();
		char[] binaryString = new char[size];
		for(int bitPosition = 0; bitPosition < size; bitPosition++)
		{
			boolean bitIsSet = (value & (1L << bitPosition)) != 0L;
			int index = size - 1 - bitPosition;
			binaryString[index] = bitIsSet ? '1' : '0';
		}
		return new String(binaryString);
	}

	public String describeNonBinaryValueWithRadix(T value, Radix radix)
	{
		// TODO: Make Locale.ENGLISH settable
		return String.format(Locale.ENGLISH, "%" + radix.formattingIdentifier(), value);
	}

	/**
	 * <b>Note:</b>May be removed in the future if Guava is removed as a dependency
	 */
	@Beta
	public Range<T> asRange()
	{
		return Ranges.closed(minValue(), maxValue());
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

		/**
		 * Long is unsupported by {@link String#format(String, Object...)} so here we turn to Guava.
		 */
		@Override
		public String describeNonBinaryValueWithRadix(Long value, Radix radix)
		{
			String result = null;
			if(radix.shouldBePrintedAsUnsigned())
			{
				result = UnsignedLongs.toString(value, radix.radix()).toUpperCase(Locale.ENGLISH);
			}
			else
			{
				// TODO: is it better to use java.text.NumberFormat?
				result = Long.toString(value, radix.radix());
			}
			return result;
		}
	}
}
