package se.j4j.argumentparser.limiters;

import static java.util.concurrent.TimeUnit.DAYS;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.fileArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.Limiters.existingFile;
import static se.j4j.argumentparser.Limiters.positiveInteger;
import static se.j4j.argumentparser.StringSplitters.comma;

import java.io.File;

import org.fest.assertions.Fail;
import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.Description;
import se.j4j.argumentparser.Limit;
import se.j4j.argumentparser.Limiter;
import se.j4j.argumentparser.Limiters;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.LimitException;

import com.google.common.collect.ImmutableList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class TestLimiters
{
	@Test
	public void testPositiveInteger() throws ArgumentException
	{
		Argument<Integer> positiveArgument = integerArgument("-i", "--index").limitTo(positiveInteger()).build();

		ArgumentParser parser = ArgumentParser.forArguments(positiveArgument);
		try
		{
			parser.parse("-i", "-5");
			Fail.fail("-5 shouldn't be a valid positive integer");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected.getMessage()).isEqualTo("-5 is not a positive integer");
		}
		ParsedArguments parsed = parser.parse("-i", "10");
		assertThat(parsed.get(positiveArgument)).isEqualTo(10);
	}

	@Test
	public void testExistingFile()
	{
		Argument<File> file = fileArgument("--file").limitTo(existingFile()).build();

		ArgumentParser parser = ArgumentParser.forArguments(file);
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
	public void testRepeatedPositiveIntegers() throws ArgumentException
	{
		integerArgument("-i", "--index").limitTo(positiveInteger()).repeated().parse("-i", "10", "-i", "-5");
	}

	@Test(expected = LimitException.class)
	public void testArityOfPositiveIntegers() throws ArgumentException
	{
		integerArgument("-i", "--indices").limitTo(positiveInteger()).arity(2).parse("-i", "10", "-5");
	}

	@Test(expected = LimitException.class)
	public void testSplittingAndLimiting() throws ArgumentException
	{
		integerArgument("-n").separator("=").limitTo(positiveInteger()).splitWith(comma()).parse("-n=1,-2");
	}

	// This is what's tested
	@SuppressWarnings("unchecked")
	@Test(expected = ClassCastException.class)
	public void testInvalidLimiterType() throws ArgumentException
	{
		Object limiter = new ShortString();
		integerArgument("-n").limitTo((Limiter<Integer>) limiter).parse("-n", "1");
	}

	@Test(expected = RuntimeException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "fail-fast during build phase")
	public void testThatDefaultValuesAreLimitedDuringBuild()
	{
		integerArgument("-n").defaultValue(-1).limitTo(positiveInteger()).build();
	}

	@Test(expected = RuntimeException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "fail-fast during build phase")
	public void testThatDefaultValuesAreLimited()
	{
		integerArgument("-n").limitTo(positiveInteger()).defaultValue(-1).build();
	}

	private static final long ONE_SECOND_IN_MILLIS = 1000;

	@Test(expected = LimitException.class, timeout = ONE_SECOND_IN_MILLIS)
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
						try
						{
							// As this should never be run it shouldn't
							// matter how long it takes to compute the
							// description
							Thread.sleep(DAYS.toNanos(1));
						}
						catch(InterruptedException e)
						{
							Thread.interrupted();
							throw new IllegalStateException("Was interrupted while sleeping.", e);
						}
						return "";
					}
				});
			}
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

		Limiter<Integer> limitors = Limiters.compound(profiler, Limiters.<Integer>noLimits());

		integerArgument("-n").limitTo(limitors).consumeAll().parse("-n", "1", "2");

		assertThat(profiler.limitationsMade).isEqualTo(2);
		profiler = new ProfilingLimiter<Integer>();

		limitors = Limiters.compound(profiler, profiler);
		integerArgument("-n").limitTo(limitors).consumeAll().parse("-n", "1", "2");

		assertThat(profiler.limitationsMade).isEqualTo(4);
		profiler = new ProfilingLimiter<Integer>();

		integerArgument("-n").limitTo(Limiters.compound(ImmutableList.of(profiler))).consumeAll().parse("-n", "1", "2");

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
	}
}
