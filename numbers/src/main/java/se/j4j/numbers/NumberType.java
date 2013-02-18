package se.j4j.numbers;

import static se.j4j.strings.Describers.numberDescriber;
import static se.j4j.strings.Descriptions.illegalArgument;
import static se.j4j.strings.StringsUtil.NEWLINE;
import static se.j4j.strings.StringsUtil.pointingAtIndex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;
import java.util.Locale;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import se.j4j.strings.Description;
import se.j4j.strings.Descriptions;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

/**
 * A class that exposes static fields (and functions), such as {@link Integer#MAX_VALUE} and
 * {@link Integer#decode(String)}, for subclasses of {@link Number} in an object oriented way
 * 
 * @param <T> the subclass of {@link Number}
 */
public abstract class NumberType<T extends Number>
{
	/**
	 * Only allow classes in this package to inherit, for now
	 */
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
	 * Exposes static fields/methods in {@link Float} in a {@link NumberType}
	 */
	public static final NumberType<Float> FLOAT = new FloatType();

	/**
	 * Exposes static fields/methods in {@link Double} in a {@link NumberType}
	 */
	public static final NumberType<Double> DOUBLE = new DoubleType();

	/**
	 * Exposes static fields/methods in {@link BigInteger} in a {@link NumberType}
	 */
	public static final NumberType<BigInteger> BIG_INTEGER = new BigIntegerType();

	/**
	 * Exposes static fields/methods in {@link BigDecimal} in a {@link NumberType}
	 */
	public static final NumberType<BigDecimal> BIG_DECIMAL = new BigDecimalType();

	/**
	 * An ordered (by data size) {@link List} of {@link NumberType}s
	 */
	public static final ImmutableList<NumberType<?>> TYPES = ImmutableList.<NumberType<?>>of(	BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE,
																								BIG_INTEGER,
																								BIG_DECIMAL);
	/**
	 * {@link NumberType}s that doesn't have any {@link #minValue()} or {@link #maxValue()}
	 */
	public static final ImmutableList<NumberType<?>> UNLIMITED_TYPES = ImmutableList.<NumberType<?>>of(BIG_INTEGER, BIG_DECIMAL);

	/**
	 * <pre>
	 * Parameters
	 * 1st %s = the received value
	 * 2nd %s = the minimum allowed value
	 * 3rd %s = the maximum allowed value
	 */
	@VisibleForTesting static final String OUT_OF_RANGE = "'%s' is not in the range %s to %s";

	/**
	 * @return the static {@code MIN_VALUE} field of {@code T}
	 * @throws UnsupportedOperationException if there isn't such a field for this type
	 */
	@CheckReturnValue
	@Nonnull
	public abstract T minValue();

	/**
	 * @return the static {@code MAX_VALUE} field of {@code T}
	 * @throws UnsupportedOperationException if there isn't such a field for this type
	 */
	@CheckReturnValue
	@Nonnull
	public abstract T maxValue();

	/**
	 * @return a human readable string describing the type {@code T} (in lower case)
	 */
	@CheckReturnValue
	@Nonnull
	public abstract String name();

	/**
	 * @return zero as a {@code T} type
	 */
	@CheckReturnValue
	@Nonnull
	public final T defaultValue()
	{
		return from(0L);
	}

	/**
	 * Casts {@code value} to a {@code T}, losing bits as necessary.
	 */
	@CheckReturnValue
	@Nonnull
	public abstract T from(Number value);

	/**
	 * <pre>
	 * Converts {@code value} into a {@link Number} of the type {@code T} in a {@link Locale}
	 * sensitive way by using {@link NumberFormat}.
	 * 
	 * For instance:
	 * {@code Integer fortyTwo = NumberType.INTEGER.parse("42", Locale.US);}
	 * 
	 * @throws IllegalArgumentException if the value isn't convertable to a number of type {@code T}
	 * </pre>
	 */
	@CheckReturnValue
	@Nonnull
	public final T parse(String value, Locale inLocale)
	{
		ParsePosition parsePosition = new ParsePosition(0);
		// TODO: maybe remove spaces here? refer to
		// http://bugs.sun.com/view_bug.do?bug_id=4510618
		Number result = parser(inLocale).parse(value, parsePosition);

		if(parsePosition.getIndex() != value.length() || result == null)
			throw illegalArgument(formatError(value, inLocale, parsePosition));

		if(!inRange(result))
			throw illegalArgument(Descriptions.format(OUT_OF_RANGE, value, format(minValue(), inLocale), format(maxValue(), inLocale)));

		return from(result);
	}

	/**
	 * <pre>
	 * Converts {@code value} into a {@link Number} of the type {@code T} assuming {@code value}
	 * is expressed in a {@link Locale#US} format.
	 * 
	 * For instance:
	 * {@code Integer fortyTwo = NumberType.INTEGER.parse("42");}
	 * 
	 * @throws IllegalArgumentException if the value isn't convertable to a number of type {@code T}
	 * </pre>
	 */
	@CheckReturnValue
	@Nonnull
	public final T parse(String value)
	{
		return parse(value, Locale.US);
	}

	NumberFormat parser(Locale inLocale)
	{
		return NumberFormat.getInstance(inLocale);
	}

	// TODO: make this public once I figure out how to validate double/float
	boolean inRange(Number number)
	{
		Long value = number.longValue();
		return value >= minValue().longValue() && value <= maxValue().longValue();
	}

	private static final String TEMPLATE = "'%s' is not a valid %s (Localization: %s)" + NEWLINE + " %s";

	private Description formatError(String invalidValue, Locale locale, ParsePosition positionForInvalidCharacter)
	{
		String localeInformation = locale.getDisplayName(locale);
		return Descriptions.format(TEMPLATE, invalidValue, name(), localeInformation, pointingAtIndex(positionForInvalidCharacter.getIndex()));
	}

	/**
	 * Returns a descriptive string of the range this {@link NumberType} can {@link #parse(String)}
	 * 
	 * @param inLocale the locale to format numbers with
	 */
	@CheckReturnValue
	@Nonnull
	public String descriptionOfValidValues(Locale inLocale)
	{

		return format(minValue(), inLocale) + " to " + format(maxValue(), inLocale);
	}

	private String format(T value, Locale inLocale)
	{
		return numberDescriber().describe(value, inLocale);
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
		public Byte from(Number value)
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
		public Integer from(Number value)
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
		public Short from(Number value)
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
		public Long from(Number value)
		{
			return value.longValue();
		}

		@Override
		public String name()
		{
			return "long";
		}

		@Override
		boolean inRange(Number number)
		{
			return number instanceof Long;
		}
	}

	private static final class DoubleType extends NumberType<Double>
	{
		@Override
		public Double minValue()
		{
			return Double.MIN_VALUE;
		}

		@Override
		public Double maxValue()
		{
			return Double.MAX_VALUE;
		}

		@Override
		public String name()
		{
			return "double";
		}

		@Override
		public Double from(Number value)
		{
			return value.doubleValue();
		}

		@Override
		boolean inRange(Number number)
		{
			try
			{
				Double.parseDouble(number.toString());
				return true;
			}
			catch(NumberFormatException e)
			{
				return false;
			}
		}
	}

	private static final class FloatType extends NumberType<Float>
	{
		@Override
		public Float minValue()
		{
			return Float.MIN_VALUE;
		}

		@Override
		public Float maxValue()
		{
			return Float.MAX_VALUE;
		}

		@Override
		public String name()
		{
			return "float";
		}

		@Override
		public Float from(Number value)
		{
			return value.floatValue();
		}

		@Override
		boolean inRange(Number number)
		{
			try
			{
				Float.parseFloat(number.toString());
				return true;
			}
			catch(NumberFormatException e)
			{
				return false;
			}
		}
	}

	private abstract static class UnlimitedNumberType<T extends Number> extends NumberType<T>
	{
		@Override
		public T minValue()
		{
			throw new UnsupportedOperationException(name() + " doesn't have any minValue");
		}

		@Override
		public T maxValue()
		{
			throw new UnsupportedOperationException(name() + " doesn't have any maxValue");
		}

		@Override
		boolean inRange(Number number)
		{
			return true;
		}

		@Override
		NumberFormat parser(Locale inLocale)
		{
			DecimalFormat parser = new DecimalFormat("", new DecimalFormatSymbols(inLocale));
			parser.setParseBigDecimal(true);
			return parser;
		}

		@Override
		public abstract String descriptionOfValidValues(Locale inLocale);
	}

	private static final class BigIntegerType extends UnlimitedNumberType<BigInteger>
	{
		@Override
		public String name()
		{
			return "big-integer";
		}

		@Override
		public BigInteger from(Number value)
		{
			if(value instanceof BigInteger)
				return (BigInteger) value;
			else if(value instanceof BigDecimal)
				return ((BigDecimal) value).toBigInteger();
			return BigInteger.valueOf(value.longValue());
		}

		@Override
		public String descriptionOfValidValues(Locale inLocale)
		{
			return "an arbitrary integer number (practically no limits)";
		}
	}

	private static final class BigDecimalType extends UnlimitedNumberType<BigDecimal>
	{
		@Override
		public String name()
		{
			return "big-decimal";
		}

		@Override
		public BigDecimal from(Number value)
		{
			if(value instanceof BigDecimal)
				return (BigDecimal) value;
			else if(value instanceof BigInteger)
				return new BigDecimal((BigInteger) value);
			return BigDecimal.valueOf(value.doubleValue());
		}

		@Override
		public String descriptionOfValidValues(Locale inLocale)
		{
			// TODO: put in resource bundle?
			return "an arbitrary decimal number (practically no limits)";
		}
	}
}
