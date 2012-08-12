package se.j4j.argumentparser.limiters;

import static com.google.common.collect.Collections2.filter;
import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.ArgumentFactory.withParser;
import static se.j4j.argumentparser.Limiters.asPredicate;
import static se.j4j.argumentparser.Limiters.range;
import static se.j4j.argumentparser.limiters.FooLimiter.foos;
import static se.j4j.argumentparser.utils.UsageTexts.expected;

import java.util.Collection;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentBuilder;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.Description;
import se.j4j.argumentparser.Limit;
import se.j4j.argumentparser.Limiter;
import se.j4j.argumentparser.stringparsers.custom.Port;
import se.j4j.argumentparser.stringparsers.custom.PortParser;
import se.j4j.argumentparser.utils.Explanation;

import com.google.common.collect.ImmutableList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Test for {@link Limiter} and {@link ArgumentBuilder#limitTo(Limiter)}
 */
public class LimiterTest
{
	@Test
	public void testRangeLimiter() throws ArgumentException
	{
		Argument<Integer> limitedNumber = integerArgument().limitTo(range(0, 4)).build();
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

		// TODO: how should an invalid default value be handled?
		assertThat(integerArgument().limitTo(range(1, 3)).defaultValue(1).parse("2")).isEqualTo(2);
	}

	@Test(expected = ArgumentException.class)
	public void testRepeatedWithLimiter() throws ArgumentException
	{
		stringArgument("-i", "--index").limitTo(foos()).repeated().parse("-i", "foo", "--index", "bar");
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

	@Test(expected = ArgumentException.class)
	public void testThatLimitersAreDescribable() throws ArgumentException
	{
		integerArgument("-n").limitTo(new Limiter<Integer>(){
			@Override
			public Limit withinLimits(Integer value)
			{
				if(value.equals(1))
					return Limit.notOk(new Description(){
						@Override
						public String description()
						{
							fail("Description should not be called when it's not needed");
							return "";
						}
					});
				return Limit.OK;
			}

			@Override
			public String descriptionOfValidValues()
			{
				return "Not used";
			}
		}).parse("-n", "1");
	}

	@Test
	public void testThatLimitersCanBeUsedAsPredicates()
	{
		Collection<Integer> filteredNumbers = filter(asList(10, 20, 30), asPredicate(range(15, 60)));
		// FilteredCollection in Guava doesn't implement equals
		assertThat(ImmutableList.copyOf(filteredNumbers)).isEqualTo(asList(20, 30));
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
		Argument<Port> portArgument = withParser(new PortParser()).limitTo(range(min, Port.MAX)).build();
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
}
