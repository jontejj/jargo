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
package se.j4j.argumentparser.exceptions;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;
import static se.j4j.argumentparser.ArgumentExceptions.withMessage;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentExceptions;
import se.j4j.argumentparser.CommandLineParser;
import se.j4j.strings.Description;
import se.j4j.strings.Descriptions;
import se.j4j.testlib.Serializer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Tests for {@link ArgumentException} and {@link ArgumentExceptions}
 */
public class ArgumentExceptionTest
{
	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Testing that invocation doesn't call description")
	public void testThatDescriptionIsNotCalledWhenNotNeeded()
	{
		withMessage(new Description(){
			@Override
			public String description()
			{
				fail("description Should not be called as no getMessage is called");
				return "";
			}
		});
	}

	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Testing that argument exceptions lazily loads the description")
	public void testThatToStringIsNotRunWhenItIsNotNeeded()
	{
		withMessage(Descriptions.format("%s", new FailingToString()));
	}

	@Test
	public void testThatCauseIsSetWithMessage()
	{
		Throwable cause = new Error();
		assertThat(withMessage("", cause).getCause()).isEqualTo(cause);
	}

	@Test
	public void testThatToStringIsUsedByGetMessage()
	{
		assertThat(withMessage(new Object(){
			@Override
			public String toString()
			{
				return "foobar";
			}
		})).hasMessage("foobar");
	}

	@Test
	public void testThatUsageIsAvailableAfterSerialization()
	{
		Argument<Integer> number = integerArgument("-n").build();
		Argument<String> string = stringArgument("-s").build();

		try
		{
			CommandLineParser.withArguments(number, string).parse("-n");
			fail("-n argument should require an integer parameter");
		}
		catch(ArgumentException expected)
		{
			String usageBeforeSerialization = expected.getMessageAndUsage();
			ArgumentException revivedException = Serializer.clone(expected);
			assertThat(revivedException.getMessageAndUsage()).isEqualTo(usageBeforeSerialization);
		}
	}

	@Test
	public void testThatProgramNameIsSerialized()
	{
		Argument<Integer> number = integerArgument("-n").build();
		try
		{
			CommandLineParser.withArguments(number).programName("MyProgram").parse("-n");
			fail("-n argument should require an integer parameter");
		}
		catch(ArgumentException expected)
		{
			ArgumentException revivedException = Serializer.clone(expected);
			assertThat(revivedException.getMessageAndUsage()).contains("MyProgram");
		}
	}

	@Test
	public void testThatProgramDescriptionIsSerialized()
	{
		Argument<Integer> number = integerArgument("-n").build();
		try
		{
			CommandLineParser.withArguments(number).programDescription("MyDescription").parse("-n");
			fail("-n argument should require an integer parameter");
		}
		catch(ArgumentException expected)
		{
			ArgumentException revivedException = Serializer.clone(expected);
			assertThat(revivedException.getMessageAndUsage()).contains("MyDescription");
		}
	}

	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Testing that argument exceptions lazily loads the toString")
	public void testThatWithMessageDoesNotRunToStringWhenItIsNotNeeded()
	{
		withMessage(new FailingToString());
	}

	private static final class FailingToString
	{
		@Override
		public String toString()
		{
			fail("toString Should not be called as no getMessage is called");
			return "";
		}
	}
}
