package se.j4j.argumentparser.internal;

import static se.j4j.argumentparser.ArgumentExceptions.forInvalidValue;
import se.j4j.argumentparser.ArgumentException;

/**
 * A class that exposes static fields (and functions), such as {@link Integer#MAX_VALUE} and
 * {@link Integer#decode(String)}, for subclasses of {@link Number} in an object oriented way
 * 
 * @param <T> the subclass of {@link Number}
 */
public abstract class NumberType<T extends Number>
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

	public abstract String name();

	public final T defaultValue()
	{
		return fromLong(0L);
	}

	public abstract T fromLong(Long value);

	/**
	 * <pre>
	 * Converts {@code value} into a {@link Number} of the type {@code T}
	 * 
	 * For instance:
	 * {@code Integer fortyTwo = NumberType.INTEGER.parse("42");}
	 * 
	 * @throws NumberFormatException if the value isn't convertable to a number of type {@code T}
	 * </pre>
	 */
	public final T parse(String value) throws ArgumentException
	{
		try
		{
			Long result = Long.decode(value);
			if(result.compareTo(minValue().longValue()) < 0 || result.compareTo(maxValue().longValue()) > 0)
				throw forInvalidValue(value, "is not in the range " + minValue() + " to " + maxValue());
			return fromLong(result);
		}
		catch(NumberFormatException nfe)
		{
			// TODO: specify which argument that failed
			ArgumentException ex = forInvalidValue(value, "is not a valid number.");
			ex.initCause(nfe);
			throw ex;
		}

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
