package se.j4j.argumentparser.utils;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.CommandLineParser;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;

import com.google.common.collect.Lists;

/**
 * Can be used for testing purposes.
 */
public final class ArgumentExpector
{
	List<Expectation<?>> expectations = Lists.newArrayList();
	List<String> actualArguments = Lists.newArrayList();

	public <T> Expectation<T> expectThat(Argument<T> definition)
	{
		return new Expectation<T>(definition);
	}

	/**
	 * <pre>
	 * Useful for concurrency testing as you can reuse the same parser
	 * instance.
	 * 
	 * Note that {@link Assertion#given(String)} should not be used in
	 * conjunction with this method
	 * 
	 * @param arguments a complete set of command line arguments
	 * @param parser the configured parser instance, it should contain all
	 *            {@link Argument} instances that {@link #expectThat(Argument)}
	 *            has received
	 * @throws ArgumentException
	 */
	public void assertForArguments(String arguments, CommandLineParser parser) throws ArgumentException
	{
		ParsedArguments parsedArguments = parser.parse(Arrays.asList(arguments.split(" ")));
		for(Expectation<?> expectation : expectations)
		{
			assertExpectation(expectation, parsedArguments);
		}
	}

	public class Expectation<T>
	{
		Argument<T> definition;
		T expect;

		public Expectation(Argument<T> definition)
		{
			this.definition = definition;
		}

		public Assertion receives(T expectation)
		{
			this.expect = expectation;
			expectations.add(this);
			return new Assertion();
		}

		@Override
		public String toString()
		{
			return definition.toString() + ", expecting: " + expect;
		}
	}

	public class Assertion
	{
		/**
		 * Creates a parser for all the definitions previously given to this
		 * expectation and asserts that they are all correct.
		 * 
		 * @param arguments
		 * @throws ArgumentException
		 */
		public void given(String arguments) throws ArgumentException
		{
			actualArguments.addAll(Arrays.asList(arguments.split(" ")));
			CommandLineParser parser = CommandLineParser.forArguments(arguments());
			ParsedArguments parsedArguments = parser.parse(actualArguments);
			for(Expectation<?> expectation : expectations)
			{
				assertExpectation(expectation, parsedArguments);
			}
		}
	}

	private <T> void assertExpectation(Expectation<T> expectation, ParsedArguments arguments)
	{
		assertThat(arguments.get(expectation.definition)).isEqualTo(expectation.expect);
	}

	private Argument<?>[] arguments()
	{
		List<Argument<?>> argumentDefinitions = new ArrayList<Argument<?>>(expectations.size());
		for(Expectation<?> expectation : expectations)
		{
			argumentDefinitions.add(expectation.definition);
		}
		return argumentDefinitions.toArray(new Argument<?>[argumentDefinitions.size()]);
	}
}
