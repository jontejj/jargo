package se.j4j.argumentparser.limiters;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.ArgumentFactory.withParser;
import static se.j4j.argumentparser.limiters.FooLimiter.foos;
import static se.j4j.argumentparser.utils.ExpectedTexts.expected;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentBuilder;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.stringparsers.custom.Port;
import se.j4j.argumentparser.stringparsers.custom.PortParser;
import se.j4j.testlib.Explanation;

import com.google.common.base.Predicate;
import com.google.common.collect.Ranges;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Test for {@link Limiter} and {@link ArgumentBuilder#limitTo(Limiter)}
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
			assertThat(expected.getMessageAndUsage("LimiterOutOfRange")).isEqualTo(expected("limiterOutOfRange"));
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
			String usage = expected.getMessageAndUsage("OnlyAllowsFoo");
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
			integerArgument("-n").limitTo(new LowNumberLimiter()).parse("-n", "5");
			fail("5 should not be allowed");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected).hasMessage("'5' is not Any number between 0 and 4 (inclusive)");
		}
	}

	private static final class LowNumberLimiter implements Predicate<Number>
	{
		@Override
		public boolean apply(Number input)
		{
			return input.byteValue() <= 4 && input.byteValue() >= 0;
		}

		@Override
		public String toString()
		{
			return "Any number between 0 and 4 (inclusive)";
		}
	}
}
