package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.internal.Comma;

public class TestStringSplitter
{
	@Test
	public void testSplittingWithComma() throws ArgumentException
	{
		Argument<List<Integer>> numbers = integerArgument("-n").splitWith(new Comma()).build();

		ParsedArguments parsed = ArgumentParser.forArguments(numbers).parse("-n", "1,2");

		assertThat(parsed.get(numbers)).isEqualTo(Arrays.asList(1, 2));
	}

	/**
	 * "-n", "1,2", "3,4"
	 * isn't allowed
	 */
	@SuppressWarnings("deprecation") //This is what's tested
	@Test(expected = IllegalStateException.class)
	public void testArityCombinedWithSplitting()
	{
		integerArgument("-n").splitWith(new Comma()).arity(2);
	}

	@Test
	public void testArityCombinedWithPropertyMap() throws ArgumentException
	{
		Argument<Map<String, List<Integer>>> numbers = integerArgument("-n")
				.splitWith(new Comma())
				.asPropertyMap()
				.ignoreCase()
				.build();

		ParsedArguments parsed = ArgumentParser.forArguments(numbers).parse("-nsmall=1,2", "-Nbig=3,4");

		Map<String, List<Integer>> expected = new HashMap<String, List<Integer>>();
		expected.put("small", Arrays.asList(1, 2));
		expected.put("big", Arrays.asList(3, 4));
		assertThat(parsed.get(numbers)).isEqualTo(expected);
	}

	@SuppressWarnings("deprecation") //This is what's tested
	@Test(expected = UnsupportedOperationException.class)
	public void testArityCombinedWithPropertyMapWrongCallOrder()
	{
		//The intent is to guide the user of the API to how he should have used it
		integerArgument("-n").asPropertyMap().splitWith(new Comma());
	}
}
