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
package se.j4j.argumentparser.stringparsers;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.bigIntegerArgument;
import static se.j4j.strings.StringsUtil.NEWLINE;

import java.math.BigInteger;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.StringParsers;
import se.j4j.testlib.Locales;

/**
 * Tests for {@link ArgumentFactory#bigIntegerArgument(String...)} and
 * {@link StringParsers#bigIntegerParser()}
 */
public class BigIntegerArgumentTest
{
	@Test
	public void testValidInteger() throws ArgumentException
	{
		BigInteger b = bigIntegerArgument("-n").parse("-n", "123456789123456789");

		assertThat(b).isEqualTo(new BigInteger("123456789123456789"));
	}

	@Test
	public void testInvalidInteger()
	{
		try
		{
			bigIntegerArgument("-n").locale(Locales.SWEDISH).parse("-n", "1a");
		}
		catch(ArgumentException e)
		{
			/**
			 * @formatter.off
			 */
			assertThat(e).hasMessage("'1a' is not a valid big-integer (Localization: svenska (Sverige))" + NEWLINE +
			                         "  ^");
			/**
			 * @formatter.on
			 */
		}
	}

	@Test
	public void testDescription()
	{
		String usage = bigIntegerArgument("-b").usage();
		assertThat(usage).contains("<big-integer>: an arbitrary integer number (practically no limits)");
	}

	@Test
	public void testThatBigIntegerDefaultsToZero() throws ArgumentException
	{
		BigInteger b = bigIntegerArgument("-b").parse();
		assertThat(b).isEqualTo(BigInteger.ZERO);
	}

	@Test
	public void testThatDefaultValueForBigIntegerIsFormattedInTheChosenLocale()
	{
		String b = bigIntegerArgument("-b").locale(Locales.TURKISH).defaultValue(BigInteger.valueOf(Long.MAX_VALUE)).usage();
		assertThat(b).contains("Default: 9.223.372.036.854.775.807" + NEWLINE);
	}
}
