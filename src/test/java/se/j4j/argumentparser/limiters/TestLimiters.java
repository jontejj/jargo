package se.j4j.argumentparser.limiters;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.fileArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.Limiters.existingFiles;
import static se.j4j.argumentparser.Limiters.range;
import static se.j4j.argumentparser.limiters.FooLimiter.foos;

import java.io.File;

import org.fest.assertions.Fail;
import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentExceptions.LimitException;
import se.j4j.argumentparser.CommandLineParser;
import se.j4j.argumentparser.Description;
import se.j4j.argumentparser.Limit;
import se.j4j.argumentparser.Limiter;
import se.j4j.argumentparser.Limiters;

import com.google.common.collect.ImmutableList;

public class TestLimiters
{
	@Test
	public void testExistingFile()
	{
		Argument<File> file = fileArgument("--file").limitTo(existingFiles()).build();

		CommandLineParser parser = CommandLineParser.forArguments(file);
		try
		{
			parser.parse("--file", ".");
		}
		catch(ArgumentException e)
		{
			fail(". should be an existing file", e);
		}
		try
		{
			parser.parse("--file", "non_existing.file");
			Fail.fail("non_existing.file should not exist");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected.getMessage()).endsWith("non_existing.file doesn't exist as a file");
		}
	}

	@Test(expected = LimitException.class)
	public void testRepeatedWithLimiter() throws ArgumentException
	{
		stringArgument("-i", "--index").limitTo(foos()).repeated().parse("-i", "foo", "-i", "bar");
	}

	@Test(expected = LimitException.class)
	public void testArityOfWithLimiter() throws ArgumentException
	{
		stringArgument("-i", "--indices").limitTo(foos()).arity(2).parse("-i", "foo", "bar");
	}

	@Test(expected = LimitException.class)
	public void testSplittingAndLimiting() throws ArgumentException
	{
		stringArgument("-n").separator("=").limitTo(foos()).splitWith(",").parse("-n=foo,bar");
	}

	@Test
	public void testRangeLimiter()
	{
		try
		{
			integerArgument().limitTo(range(1, 5)).parse("6");
		}
		catch(ArgumentException e)
		{
			assertThat(e).hasMessage("'6' is not in the range [1‥5]");
		}
	}

	// This is what's tested
	@SuppressWarnings("unchecked")
	@Test(expected = ClassCastException.class)
	public void testInvalidLimiterType() throws ArgumentException
	{
		Object limiter = new ShortString();
		integerArgument("-n").limitTo((Limiter<Integer>) limiter).parse("-n", "1");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThatDefaultValuesAreLimited() throws ArgumentException
	{
		stringArgument("-n").limitTo(foos()).defaultValue("bar").parse();
	}

	@Test(expected = LimitException.class)
	public void testThatLimitersAreDescribable() throws ArgumentException
	{
		integerArgument("-n").limitTo(new Limiter<Integer>(){
			@Override
			public Limit withinLimits(Integer value)
			{
				return Limit.notOk(new Description(){
					@Override
					public String description()
					{
						fail("Description should not be called when it's not needed");
						return "";
					}
				});
			}

			// @Override
			// public String validValuesDescription()
			// {
			// return "Not used";
			// }
		}).parse("-n", "1");
	}

	@Test
	public void testThatLimiterOkResponseIsEmpty()
	{
		assertThat(Limit.OK.reason()).isEmpty();
	}

	@Test
	public void testThatLimiterIsNotCalledTooOften() throws ArgumentException
	{
		ProfilingLimiter<Integer> profiler = new ProfilingLimiter<Integer>();

		integerArgument("-n").limitTo(profiler).repeated().parse("-n", "1", "-n", "-2");

		assertThat(profiler.limitationsMade).isEqualTo(2);
	}

	@Test
	public void testMultipleLimiters() throws ArgumentException
	{
		ProfilingLimiter<Integer> profiler = new ProfilingLimiter<Integer>();

		Limiter<Integer> limitors = profiler;

		integerArgument("-n").limitTo(limitors).variableArity().parse("-n", "1", "2");

		assertThat(profiler.limitationsMade).isEqualTo(2);
		profiler = new ProfilingLimiter<Integer>();

		limitors = Limiters.compound(profiler, profiler);
		integerArgument("-n").limitTo(limitors).variableArity().parse("-n", "1", "2");

		assertThat(profiler.limitationsMade).isEqualTo(4);
		profiler = new ProfilingLimiter<Integer>();

		integerArgument("-n").limitTo(Limiters.compound(ImmutableList.of(profiler))).variableArity().parse("-n", "1", "2");

		assertThat(profiler.limitationsMade).isEqualTo(2);
	}

	@Test
	public void testThatLimitersAreClearable() throws ArgumentException
	{
		ProfilingLimiter<Integer> profiler = new ProfilingLimiter<Integer>();

		integerArgument("-n").limitTo(profiler).repeated().clearLimiters().parse("-n", "1", "-n", "-2");

		assertThat(profiler.limitationsMade).isEqualTo(0);
	}

	private static final class ProfilingLimiter<T> implements Limiter<T>
	{
		int limitationsMade;

		@Override
		public Limit withinLimits(T value)
		{
			limitationsMade++;
			return Limit.OK;
		}

		// @Override
		// public String validValuesDescription()
		// {
		// return "not used";
		// }
	}
}
