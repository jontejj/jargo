package se.j4j.argumentparser.limiters;

import static com.google.common.collect.Collections2.filter;
import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.fileArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.Limiters.asPredicate;
import static se.j4j.argumentparser.Limiters.existingFiles;
import static se.j4j.argumentparser.Limiters.range;
import static se.j4j.argumentparser.limiters.FooLimiter.foos;
import static se.j4j.argumentparser.utils.UsageTexts.expected;

import java.io.File;
import java.util.Collection;

import org.fest.assertions.Fail;
import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.CommandLineParser;
import se.j4j.argumentparser.Description;
import se.j4j.argumentparser.Limit;
import se.j4j.argumentparser.Limiter;

import com.google.common.collect.ImmutableList;

public class LimiterTest
{
	@Test
	public void testExistingFile()
	{
		Argument<File> file = fileArgument("--file").limitTo(existingFiles()).defaultValueDescription("Current working directory").build();

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
			String usage = expected.getMessageAndUsage("OnlyAllowsExistingFiles");
			assertThat(usage).contains("--file <path>    <path>: an existing file");
			assertThat(expected.getMessage()).endsWith("non_existing.file isn't an existing file");
		}
	}

	@Test(expected = ArgumentException.class)
	public void testRepeatedWithLimiter() throws ArgumentException
	{
		stringArgument("-i", "--index").limitTo(foos()).repeated().parse("-i", "foo", "-i", "bar");
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

	@Test
	public void testRangeLimiter()
	{
		try
		{
			integerArgument().limitTo(range(1, 5)).parse("6");
		}
		catch(ArgumentException e)
		{
			assertThat(e).hasMessage("'6' is not in the range 1 to 5 (decimal)");
		}
	}

	@Test(expected = IllegalStateException.class)
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
				return Limit.notOk(new Description(){
					@Override
					public String description()
					{
						fail("Description should not be called when it's not needed");
						return "";
					}
				});
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

	private static final class ProfilingLimiter<T> implements Limiter<T>
	{
		int limitationsMade;

		@Override
		public Limit withinLimits(T value)
		{
			limitationsMade++;
			return Limit.OK;
		}

		@Override
		public String descriptionOfValidValues()
		{
			return "any value";
		}
	}
}
