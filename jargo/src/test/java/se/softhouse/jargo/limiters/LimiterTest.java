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
package se.softhouse.jargo.limiters;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.softhouse.jargo.Arguments.integerArgument;
import static se.softhouse.jargo.Arguments.stringArgument;
import static se.softhouse.jargo.Arguments.withParser;
import static se.softhouse.jargo.limiters.FooLimiter.foos;
import static se.softhouse.jargo.utils.ExpectedTexts.expected;

import org.junit.Test;

import se.softhouse.comeon.testlib.Explanation;
import se.softhouse.jargo.Argument;
import se.softhouse.jargo.ArgumentBuilder;
import se.softhouse.jargo.ArgumentException;
import se.softhouse.jargo.stringparsers.custom.Port;
import se.softhouse.jargo.stringparsers.custom.PortParser;

import com.google.common.base.Predicate;
import com.google.common.collect.Ranges;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Test for {@link ArgumentBuilder#limitTo(Predicate)}
 */
public class LimiterTest
{
	@Test
	public void testRangeLimiter() throws ArgumentException
	{
		Argument<Integer> limitedNumber = integerArgument().limitTo(Ranges.closed(0, 4)).build();
		try
		{
			limitedNumber.parse("5");
			fail("5 shouldn't be valid since it's higher than 4");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected.getMessageAndUsage()).isEqualTo(expected("limiterOutOfRange"));
		}
		assertThat(limitedNumber.parse("4")).isEqualTo(4);

		assertThat(integerArgument().limitTo(Ranges.closed(1, 3)).defaultValue(1).parse("2")).isEqualTo(2);
	}

	@Test
	public void testRepeatedWithLimiter()
	{
		try
		{
			stringArgument("-i", "--index").limitTo(foos()).repeated().parse("-i", "foo", "--index", "bar");
			fail("bar shouldn't be allowed");
		}
		catch(ArgumentException expected)
		{
			// This message is from ListPredicate that throws IllegalArgumentException so
			// this verifies that the message of the wrapped exception is used.
			assertThat(expected).hasMessage("'bar' is not foo");
		}
	}

	@Test
	public void testArityOfWithLimiter()
	{
		try
		{
			stringArgument("-f", "--foos").required().limitTo(foos()).arity(2).parse("-f", "foo", "bar");
		}
		catch(ArgumentException expected)
		{
			String usage = expected.getMessageAndUsage();
			assertThat(usage).isEqualTo(expected("arityWithLimitedValues"));
		}
	}

	@Test(expected = ArgumentException.class)
	public void testSplittingAndLimiting() throws ArgumentException
	{
		stringArgument("-n").separator("=").limitTo(foos()).splitWith(",").parse("-n=foo,bar");
	}

	@Test(expected = IllegalStateException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testThatDefaultValuesAreLimited()
	{
		stringArgument("-n").limitTo(foos()).defaultValue("bar").build();
	}

	@Test
	public void testThatLimiterIsNotCalledTooOften() throws ArgumentException
	{
		ProfilingLimiter<Integer> profiler = new ProfilingLimiter<Integer>();
		integerArgument("-n").limitTo(profiler).repeated().parse("-n", "1", "-n", "-2");
		assertThat(profiler.limitationsMade).isEqualTo(2);
	}

	@Test
	public void testThatRangeLimiterDoesNotCallToStringOnComparedObjectsInVain() throws ArgumentException
	{
		Port min = new Port(1);
		Argument<Port> portArgument = withParser(new PortParser()).limitTo(Ranges.closed(min, Port.MAX)).build();
		try
		{
			portArgument.parse("-1");
			fail("-1 shouldn't be valid since it's not within the range of valid port numbers");
		}
		catch(ArgumentException expected)
		{
			assertThat(min.toStringCallCount).isZero();
		}
		// Also make sure the range accepts valid ports
		assertThat(portArgument.parse("2")).isEqualTo(new Port(2));
	}

	@Test
	public void testThatLimiterForSuperClassWorks()
	{
		try
		{
			integerArgument("-n").limitTo(new LowNumbers()).parse("-n", "5");
			fail("5 should not be allowed");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected).hasMessage("'5' is not Any number between 0 and 4 (inclusive)");
		}
	}

	@Test
	public void testThatCauseIsSetForLimitedValue() throws Exception
	{
		final IllegalArgumentException originalException = new IllegalArgumentException();
		try
		{
			integerArgument().limitTo(new Predicate<Integer>(){

				@Override
				public boolean apply(Integer input)
				{
					throw originalException;
				}
			}).parse("1");
			fail("Limiter should have propagated originalException");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected.getCause()).isSameAs(originalException);
		}
	}
}
