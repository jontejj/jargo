package se.j4j.numbers;

import static se.j4j.strings.Descriptions.format;
import static se.j4j.strings.Descriptions.illegalArgument;

import java.util.List;

import se.j4j.texts.Texts;

import com.google.common.collect.ImmutableList;

/**
 * A class that exposes static fields (and functions), such as {@link Integer#MAX_VALUE} and
 * {@link Integer#decode(String)}, for subclasses of {@link Number} in an object oriented way
 * 
 * @param <T> the subclass of {@link Number}
 */
public abstract class NumberType<T extends Number>
{
	// Only allow classes in this package to inherit, for now
	NumberType()
	{
	}

	/**
	 * Exposes static fields/methods in {@link Byte} in a {@link NumberType}
	 */
	public static final NumberType<Byte> BYTE = new ByteType();

	/**
	 * Exposes static fields/methods in {@link Short} in a {@link NumberType}
	 */
	public static final NumberType<Short> SHORT = new ShortType();

	/**
	 * Exposes static fields/methods in {@link Integer} in a {@link NumberType}
	 */
	public static final NumberType<Integer> INTEGER = new IntegerType();

	/**
	 * Exposes static fields/methods in {@link Long} in a {@link NumberType}
	 */
	public static final NumberType<Long> LONG = new LongType();

	/**
	 * Returns an ordered (by data size) {@link List} of {@link NumberType}s.
	 */
	public static final ImmutableList<NumberType<?>> TYPES = ImmutableList.<NumberType<?>>of(BYTE, SHORT, INTEGER, LONG);

	/**
	 * @return the static {@code MIN_VALUE} field of {@code T}
	 */
	public abstract T minValue();

	/**
	 * @return the static {@code MAX_VALUE} field of {@code T}
	 */
	public abstract T maxValue();

	/**
	 * @return the simple class name of {@code T} in lower case
	 */
	public abstract String name();

	/**
	 * @return zero as a {@code T} type
	 */
	public final T defaultValue()
	{
		return fromLong(0L);
	}

	/**
	 * Casts {@code value} to a {@code T}, as {@link Long} is the biggest
	 * data type supported by {@link NumberType} this method can guarantee
	 * that no bits are lost.
	 */
	public abstract T fromLong(Long value);

	/**
	 * <pre>
	 * Converts {@code value} into a {@link Number} of the type {@code T}
	 * 
	 * For instance:
	 * {@code Integer fortyTwo = NumberType.INTEGER.decode("42");}
	 * 
	 * @throws IllegalArgumentException if the value isn't convertable to a number of type {@code T}
	 * </pre>
	 */
	public final T decode(String value)
	{
		try
		{
			Long result = Long.decode(value);

			if(result.compareTo(minValue().longValue()) < 0 || result.compareTo(maxValue().longValue()) > 0)
				throw illegalArgument(format(Texts.OUT_OF_RANGE, value, minValue(), maxValue()));
			return fromLong(result);
		}
		catch(NumberFormatException nfe)
		{
			throw illegalArgument(format(Texts.INVALID_NUMBER, value), nfe);
		}

	}

	/**
	 * @return {@link #name()}
	 */
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
		public String name()
		{
			return "long";
		}
	}
}
