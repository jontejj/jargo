package se.j4j.numbers;

import static java.lang.String.format;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.numbers.NumberType.BIG_DECIMAL;
import static se.j4j.numbers.NumberType.BIG_INTEGER;
import static se.j4j.numbers.NumberType.BYTE;
import static se.j4j.numbers.NumberType.INTEGER;
import static se.j4j.numbers.NumberType.LONG;
import static se.j4j.numbers.NumberType.OUT_OF_RANGE;
import static se.j4j.numbers.NumberType.SHORT;
import static se.j4j.strings.StringsUtil.NEWLINE;
import static se.j4j.testlib.Locales.SWEDISH;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

public class NumberTypeTest
{
	static final BigInteger biggerThanLong = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);

	@Test
	public void testMappingOfByteFields()
	{
		assertThat(NumberType.BYTE.maxValue()).isEqualTo(Byte.MAX_VALUE);
		assertThat(NumberType.BYTE.minValue()).isEqualTo(Byte.MIN_VALUE);
	}

	@Test
	public void testMappingOfShortFields()
	{
		assertThat(NumberType.SHORT.maxValue()).isEqualTo(Short.MAX_VALUE);
		assertThat(NumberType.SHORT.minValue()).isEqualTo(Short.MIN_VALUE);
	}

	@Test
	public void testMappingOfIntegerFields()
	{
		assertThat(NumberType.INTEGER.maxValue()).isEqualTo(Integer.MAX_VALUE);
		assertThat(NumberType.INTEGER.minValue()).isEqualTo(Integer.MIN_VALUE);
	}

	@Test
	public void testMappingOfLongFields()
	{
		assertThat(NumberType.LONG.maxValue()).isEqualTo(Long.MAX_VALUE);
		assertThat(NumberType.LONG.minValue()).isEqualTo(Long.MIN_VALUE);
	}

	@Test
	public void testMappingOfFloatFields()
	{
		assertThat(NumberType.FLOAT.maxValue()).isEqualTo(Float.MAX_VALUE);
		assertThat(NumberType.FLOAT.minValue()).isEqualTo(Float.MIN_VALUE);
	}

	@Test
	public void testMappingOfDoubleFields()
	{
		assertThat(NumberType.DOUBLE.maxValue()).isEqualTo(Double.MAX_VALUE);
		assertThat(NumberType.DOUBLE.minValue()).isEqualTo(Double.MIN_VALUE);
	}

	@Test
	public void testNumbersAtBounds()
	{
		System.out.println(Float.compare(0, Float.MIN_VALUE));
		System.out.println(0.0f >= Float.MIN_VALUE);
		for(NumberType<?> type : NumberType.TYPES)
		{
			assertThat(type.parse("0").intValue()).isZero();
			assertThat(type.parse("1").intValue()).isEqualTo(1);
			assertThat(type.parse("-1").intValue()).isEqualTo(-1);

			if(NumberType.UNLIMITED_TYPES.contains(type))
			{
				testUnlimitedType(type);
			}
			else
			{
				// Numbers at the bounds
				assertThat(type.parse(type.minValue().toString())).isEqualTo(type.minValue());
				assertThat(type.parse(type.maxValue().toString())).isEqualTo(type.maxValue());
			}

			// Default values
			assertThat(type.defaultValue().intValue()).isZero();
		}
	}

	private void testUnlimitedType(NumberType<?> type)
	{
		try
		{
			type.minValue();
			fail("Unlimited types should not support minValue");
		}
		catch(UnsupportedOperationException expected)
		{

		}
		try
		{
			type.maxValue();
			fail("Unlimited types should not support maxValue");
		}
		catch(UnsupportedOperationException expected)
		{

		}
	}

	@Test
	public void testThatParseCanHandleLargeNumbers() throws Exception
	{

		BigInteger bigInteger = BIG_INTEGER.parse(biggerThanLong.toString());
		assertThat(bigInteger).isEqualTo(biggerThanLong);

		BigDecimal biggerThanLongWithFraction = new BigDecimal(biggerThanLong).add(new BigDecimal(2.5));

		BigDecimal bigDecimal = BIG_DECIMAL.parse(biggerThanLongWithFraction.toString());
		assertThat(bigDecimal).isEqualTo(biggerThanLongWithFraction);
	}

	@Test
	public void testThatDescriptionOfValidValuesReturnsHumanReadableStrings() throws Exception
	{
		assertThat(BYTE.descriptionOfValidValues(SWEDISH)).isEqualTo("-128 to 127");
		assertThat(SHORT.descriptionOfValidValues(SWEDISH)).isEqualTo("-32 768 to 32 767");
		assertThat(INTEGER.descriptionOfValidValues(SWEDISH)).isEqualTo("-2 147 483 648 to 2 147 483 647");
		assertThat(LONG.descriptionOfValidValues(SWEDISH)).isEqualTo("-9 223 372 036 854 775 808 to 9 223 372 036 854 775 807");
		assertThat(BIG_INTEGER.descriptionOfValidValues(SWEDISH)).isEqualTo("an arbitrary integer number (practically no limits)");
		assertThat(BIG_DECIMAL.descriptionOfValidValues(SWEDISH)).isEqualTo("an arbitrary decimal number (practically no limits)");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThatEmptyInputThrows() throws Exception
	{
		BYTE.parse("");
	}

	@Test
	public void testThatNumbersTruncatesFractions() throws Exception
	{
		assertThat(BYTE.from(2.5)).isEqualTo((byte) 2);
		assertThat(SHORT.from(2.5)).isEqualTo((short) 2);
		assertThat(INTEGER.from(2.5)).isEqualTo(2);
		assertThat(LONG.from(2.5)).isEqualTo(2);
		assertThat(BIG_INTEGER.from(BigDecimal.valueOf(2.5))).isEqualTo(BigInteger.valueOf(2));
	}

	@Test
	public void testThatBigDecimalUsesAsHighPrecisionAsPossible() throws Exception
	{
		assertThat(BIG_DECIMAL.from(Double.MAX_VALUE)).isEqualTo(BigDecimal.valueOf(Double.MAX_VALUE));
		assertThat(BIG_DECIMAL.from(2.5)).isEqualTo(BigDecimal.valueOf(2.5));
		assertThat(BIG_DECIMAL.from(Long.MAX_VALUE)).isEqualTo(BigDecimal.valueOf(((Long) Long.MAX_VALUE).doubleValue()));
		assertThat(BIG_DECIMAL.from(biggerThanLong)).isEqualTo(new BigDecimal(biggerThanLong));
	}

	@Test
	public void testThatBigIntegerUsesLongValueOnNumber() throws Exception
	{
		assertThat(BIG_INTEGER.from(Long.MAX_VALUE)).isEqualTo(BigInteger.valueOf(Long.MAX_VALUE));
		assertThat(BIG_INTEGER.from(Double.MAX_VALUE)).isEqualTo(BigInteger.valueOf(((Double) Double.MAX_VALUE).longValue()));
	}

	@Test
	public void testThatFromReturnsTheArgumentIfItsOfTheSameType() throws Exception
	{
		BigInteger integer = new BigInteger("1");
		assertThat(BIG_INTEGER.from(integer)).isSameAs(integer);
		BigDecimal decimal = new BigDecimal("2.5");
		assertThat(BIG_DECIMAL.from(decimal)).isSameAs(decimal);
	}

	@Test
	public void testThatToStringReturnsName()
	{
		for(NumberType<?> type : NumberType.TYPES)
		{
			assertThat(type.toString()).isEqualTo(type.name());
		}
	}

	@Test
	public void testInvalidShortNumbers()
	{
		List<Integer> invalidInput = Arrays.asList(Short.MIN_VALUE - 1, Short.MAX_VALUE + 1);
		for(Integer input : invalidInput)
		{
			try
			{
				NumberType.SHORT.parse(input.toString());
				fail("Invalid short input not detected: " + input);
			}
			catch(IllegalArgumentException expected)
			{
				assertThat(expected).hasMessage(format(OUT_OF_RANGE, input, "-32,768", "32,767"));
			}
		}
	}

	@Test
	public void testInvalidIntegerNumbers()
	{
		List<Long> invalidInput = Arrays.asList((long) Integer.MIN_VALUE - 1, (long) Integer.MAX_VALUE + 1);
		for(Long input : invalidInput)
		{
			try
			{
				NumberType.INTEGER.parse(input.toString());
				fail("Invalid integer input not detected: " + input);
			}
			catch(IllegalArgumentException expected)
			{
				assertThat(expected).hasMessage(format(OUT_OF_RANGE, input, "-2,147,483,648", "2,147,483,647"));
			}
		}
	}

	@Test
	public void testInvalidLongNumbers()
	{
		List<BigInteger> invalidInput = Arrays.asList(BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE), BigInteger.valueOf(Long.MAX_VALUE)
				.add(BigInteger.ONE));
		for(BigInteger input : invalidInput)
		{
			try
			{
				NumberType.LONG.parse(input.toString());
				fail("Invalid long input not detected: " + input);
			}
			catch(IllegalArgumentException expected)
			{
				assertThat(expected).hasMessage(format(OUT_OF_RANGE, input, "-9,223,372,036,854,775,808", "9,223,372,036,854,775,807"));
			}
		}
	}

	@Ignore("Why doesn't Float.parseFloat throw here?")
	@Test
	public void testInvalidFloatNumbers()
	{
		List<BigDecimal> invalidInput = Arrays.asList(	BigDecimal.valueOf(Float.MIN_VALUE).subtract(BigDecimal.ONE),
														BigDecimal.valueOf(Float.MAX_VALUE).add(BigDecimal.ONE));
		for(BigDecimal input : invalidInput)
		{
			try
			{
				NumberType.FLOAT.parse(input.toString());
				fail("Invalid float input not detected: " + input);
			}
			catch(IllegalArgumentException expected)
			{
			}
		}
	}

	@Ignore("Why doesn't Double.parseDouble throw here?")
	@Test
	public void testInvalidDoubleNumbers()
	{
		List<BigDecimal> invalidInput = Arrays.asList(	BigDecimal.valueOf(Double.MIN_VALUE).subtract(BigDecimal.ONE),
														BigDecimal.valueOf(Double.MAX_VALUE).add(BigDecimal.ONE));
		for(BigDecimal input : invalidInput)
		{
			try
			{
				NumberType.DOUBLE.parse(input.toString());
				fail("Invalid double input not detected: " + input);
			}
			catch(IllegalArgumentException expected)
			{
			}
		}
	}

	@Test
	public void testNullContracts()
	{
		new NullPointerTester().testInstanceMethods(NumberType.LONG, Visibility.PUBLIC);
		new NullPointerTester().testStaticMethods(NumberType.class, Visibility.PACKAGE);
	}

	@Test
	public void testThatUnparsableTextGeneratesProperErrorMessage() throws Exception
	{
		try
		{
			NumberType.INTEGER.parse("a", Locale.ENGLISH);
			fail("a should not be a parsable number");
		}
		catch(IllegalArgumentException e)
		{
			/**
			 * @formatter.off
			 */
			assertThat(e).hasMessage("'a' is not a valid integer (Localization: English)" + NEWLINE +
			                         " ^");
			/**
			 * @formatter.on
			 */
		}
	}
}
