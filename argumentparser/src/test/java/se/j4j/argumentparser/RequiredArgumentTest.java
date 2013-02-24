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
package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.utils.ExpectedTexts.expected;

import org.junit.Test;

import se.j4j.testlib.Explanation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Tests for {@link ArgumentBuilder#required()}
 */
public class RequiredArgumentTest
{
	@Test
	public void testMissingRequiredNamedArgument()
	{
		Argument<Integer> number = integerArgument("--number").required().build();
		Argument<Integer> number2 = integerArgument("--number2").required().build();

		try
		{
			CommandLineParser.withArguments(number, number2).parse();
			fail("Required argument silently ignored");
		}
		catch(ArgumentException e)
		{
			assertThat(e.getMessageAndUsage()).isEqualTo(expected("missingRequiredArguments"));
		}
	}

	@Test
	public void testMissingRequiredIndexedArgument()
	{
		Argument<Integer> number = integerArgument().metaDescription("numberOne").required().build();
		Argument<Integer> number2 = integerArgument().metaDescription("numberTwo").required().build();

		try
		{
			CommandLineParser.withArguments(number, number2).parse();
			fail("Required argument silently ignored");
		}
		catch(ArgumentException e)
		{
			assertThat(e).hasMessage("Missing required arguments: [numberOne, numberTwo]");
		}
	}

	@Test(expected = ArgumentException.class)
	public void testThatRequiredArgumentsIsResetBetweenParsings() throws ArgumentException
	{
		Argument<Integer> required = integerArgument("-n").required().build();
		try
		{
			required.parse("-n", "1");
		}
		catch(ArgumentException e)
		{
			fail("Parser failed to handle required argument");
		}
		required.parse(); // The second time shouldn't be affected by the first
	}

	@SuppressWarnings("deprecation")
	// This is what's tested
	@Test(expected = IllegalStateException.class)
	public void testMakingAnOptionalArgumentRequired()
	{
		optionArgument("-l").required();
	}

	@Test(expected = IllegalStateException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testSettingADefaultValueForARequiredArgument()
	{
		integerArgument("-l").required().defaultValue(42);
	}

	@Test(expected = IllegalStateException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testMakingARequiredArgumentWithDefaultValue()
	{
		integerArgument("-l").defaultValue(42).required();
	}
}
