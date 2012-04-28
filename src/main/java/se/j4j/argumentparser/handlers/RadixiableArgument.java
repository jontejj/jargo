package se.j4j.argumentparser.handlers;

import java.util.HashMap;
import java.util.Map;

import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;

import com.google.common.base.Strings;

public abstract class RadixiableArgument<T extends Number & Comparable<T>> extends OneParameterArgument<T>
{
	public enum Radix
	{
		BINARY(2, "", UnsignedOutput.YES),
		OCTAL(8, "o", UnsignedOutput.YES),
		DECIMAL(10, "d", UnsignedOutput.NO),
		HEX(16, "X", UnsignedOutput.YES);

		private int radix;
		private String formattingIdentifier;

		private UnsignedOutput unsignedOutput;

		static Map<Integer, Radix> radixMap;

		Radix(int radix, String formattingIdentifier, UnsignedOutput unsignedOutput)
		{
			this.radix = radix;
			this.formattingIdentifier = formattingIdentifier;
			this.unsignedOutput = unsignedOutput;
			if(radixMap == null)
			{
				radixMap = new HashMap<Integer, Radix>(6);
			}
			radixMap.put(radix, this);
		}

		public static Radix fromIntegralRadix(int radix)
		{
			return radixMap.get(radix);
		}

		public int radix()
		{
			return radix;
		}

		public String formattingIdentifier()
		{
			return formattingIdentifier;
		}

		public boolean isUnsignedOutput()
		{
			return unsignedOutput == UnsignedOutput.YES;
		}

		/**
		 * Tells whether or not this radix'es output is affected by
		 * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4215269">
		 * Bug ID: 4215269 Some Integer.toHexString(int) results cannot be
		 * decoded back to an int
		 * </a>
		 */
		private enum UnsignedOutput
		{
			YES,
			NO;
		}
	}

	private final Radix radix;

	public RadixiableArgument(final Radix radix)
	{
		this.radix = radix;
	}

	@Override
	public T parse(final String value) throws ArgumentException
	{
		try
		{
			Long result = Long.parseLong(value, radix().radix());
			if(radix().isUnsignedOutput())
			{
				result = inverseNegativeBits(result);
			}

			if(result.compareTo(minValue().longValue()) < 0 || result.compareTo(maxValue().longValue()) > 0)
				throw InvalidArgument.create(value, " in " + radix() + " is not in the range of " + descriptionOfValidValues());

			return cast(result);
		}
		catch(NumberFormatException nfe)
		{
			// TODO: make this look pretty and verify it
			InvalidArgument ex = InvalidArgument.create(value, " is not a valid number.");
			ex.initCause(nfe);
			throw ex;
		}
	}

	/**
	 * @return the radix this handler uses when encoding/decoding to/from
	 *         strings
	 */
	protected Radix radix()
	{
		return radix;
	}

	@Override
	public String descriptionOfValidValues()
	{
		String maxValue;
		String minValue;
		if(radix().isUnsignedOutput())
		{
			// TODO: what if minValue() is something else than Byte.MIN_VALUE
			// etc?
			maxValue = describeValue(cast(-1L));
			minValue = describeValue(cast(0L));
		}
		else
		{
			maxValue = describeValue(maxValue());
			minValue = describeValue(minValue());
		}
		minValue = Strings.padStart(minValue, maxValue.length(), '0');

		return minValue + " to " + maxValue;
	}

	@Override
	public String describeValue(T value)
	{
		switch(radix)
		{
			case BINARY:
				return toBinaryString(value);
			default:
				return String.format("%" + radix.formattingIdentifier(), value);
		}
	}

	public abstract T minValue();

	public abstract T maxValue();

	protected abstract T cast(Long value);

	protected abstract String toBinaryString(T value);

	protected abstract long signBitMask();

	@Override
	public String toString()
	{
		return "Radix: " + radix + ", Default value:" + defaultValue();
	}

	private long inverseNegativeBits(long value)
	{
		if(isSigned(value))
		{
			long signMask = signBitMask();
			long valueBits = signMask - 1;
			// Inverse all bits except the sign bit
			value = signMask | (valueBits ^ ~value);
		}
		return value;
	}

	private boolean isSigned(long value)
	{
		return (value & signBitMask()) > 0;
	}
}
