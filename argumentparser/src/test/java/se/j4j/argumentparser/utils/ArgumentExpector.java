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
		 */
		public void given(String arguments) throws ArgumentException
		{
			actualArguments.addAll(Arrays.asList(arguments.split(" ")));
			CommandLineParser parser = CommandLineParser.withArguments(arguments());
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

	private List<Argument<?>> arguments()
	{
		List<Argument<?>> argumentDefinitions = new ArrayList<Argument<?>>(expectations.size());
		for(Expectation<?> expectation : expectations)
		{
			argumentDefinitions.add(expectation.definition);
		}
		return argumentDefinitions;
	}
}
