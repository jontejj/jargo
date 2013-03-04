/* Copyright 2013 Jonatan Jönsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
*/
package se.softhouse.jargo.stringparsers;

import static java.util.Locale.US;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.softhouse.comeon.strings.StringsUtil.NEWLINE;
import static se.softhouse.comeon.testlib.Locales.SWEDISH;
import static se.softhouse.comeon.testlib.Locales.TURKISH;
import static se.softhouse.jargo.ArgumentFactory.bigDecimalArgument;
import static se.softhouse.jargo.ArgumentFactory.byteArgument;
import static se.softhouse.jargo.ArgumentFactory.integerArgument;
import static se.softhouse.jargo.ArgumentFactory.longArgument;
import static se.softhouse.jargo.ArgumentFactory.shortArgument;
import static se.softhouse.jargo.utils.ExpectedTexts.expected;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.softhouse.jargo.ArgumentException;
import se.softhouse.jargo.ArgumentFactory;
import se.softhouse.jargo.StringParsers;

/**
 * <pre>
 * Tests for
 * {@link ArgumentFactory#byteArgument(String...)},
 * {@link ArgumentFactory#shortArgument(String...)},
 * {@link ArgumentFactory#integerArgument(String...)},
 * {@link ArgumentFactory#longArgument(String...)}
 * and
 * {@link StringParsers#byteParser()},
 * {@link StringParsers#shortParser()},
 * {@link StringParsers#integerParser()},
 * {@link StringParsers#longParser()}
 */
public class NumberArgumentTest
{
	@Test
	public void testUsage()
	{
		String validIntegers = "<integer>: -2,147,483,648 to 2,147,483,647" + NEWLINE;
		String usage = integerArgument().locale(US).usage();
		assertThat(usage).contains(validIntegers);
		assertThat(usage).contains("Default: 0" + NEWLINE);
	}

	@Test
	public void testInvalidNumberArguments()
	{
		List<Integer> invalidInput = Arrays.asList(Byte.MIN_VALUE - 1, Byte.MAX_VALUE + 1);
		for(Integer input : invalidInput)
		{
			try
			{
				byteArgument("-b").parse("-b", input.toString());
				fail("Invalid byte input not detected: " + input);
			}
			catch(ArgumentException e)
			{
				assertThat(e.getMessageAndUsage()).isEqualTo(expected("InvalidByte" + input));
			}
		}

		try
		{
			byteArgument("-b").locale(SWEDISH).parse("-b", "NaN");
			fail("Not a number not detected");
		}
		catch(ArgumentException e)
		{
			assertThat(e.getMessageAndUsage()).isEqualTo(expected("ByteNaN"));
		}
	}

	@Test
	public void testThatDefaultValueForShortIsFormattedInTheChosenLocale()
	{
		String b = shortArgument().locale(TURKISH).defaultValue(Short.MAX_VALUE).usage();
		assertThat(b).contains("Default: 32.767" + NEWLINE);
	}

	@Test
	public void testThatDefaultValueForIntegerIsFormattedInTheChosenLocale()
	{
		String b = integerArgument().locale(TURKISH).defaultValue(Integer.MAX_VALUE).usage();
		assertThat(b).contains("Default: 2.147.483.647" + NEWLINE);
	}

	@Test
	public void testThatDefaultValueForLongIsFormattedInTheChosenLocale()
	{
		String b = longArgument().locale(TURKISH).defaultValue(Long.MAX_VALUE).usage();
		assertThat(b).contains("Default: 9.223.372.036.854.775.807" + NEWLINE);
	}

	@Test
	public void testThatDefaultValueForBigDecimalIsFormattedInTheChosenLocale()
	{
		String b = bigDecimalArgument().locale(TURKISH).defaultValue(BigDecimal.valueOf(Long.MAX_VALUE)).usage();
		assertThat(b).contains("Default: 9.223.372.036.854.775.807" + NEWLINE);
	}
}
