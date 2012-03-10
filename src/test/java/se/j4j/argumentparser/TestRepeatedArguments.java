package se.j4j.argumentparser;

import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.exceptions.UnhandledRepeatedArgument;
import se.j4j.argumentparser.internal.Comma;

public class TestRepeatedArguments
{

	@SuppressWarnings("deprecation") //This is what's tested
	@Test(expected = IllegalStateException.class)
	public void testCallingRepeatedBeforeArity()
	{
		integerArgument("--number").repeated().arity(2);
	}

	@SuppressWarnings("deprecation") //This is what's tested
	@Test(expected = IllegalStateException.class)
	public void testCallingRepeatedBeforeConsumeAll()
	{
		integerArgument("--number").repeated().consumeAll();
	}

	@Test
	public void testTwoParametersForNamedArgumentRepeated() throws ArgumentException
	{
		String[] args = {"--numbers", "5", "6", "--numbers", "3", "4"};

		Argument<List<List<Integer>>> numbers = integerArgument("--numbers").arity(2).repeated().build();

		ParsedArguments parsed = ArgumentParser.forArguments(numbers).parse(args);

		List<List<Integer>> numberLists = new ArrayList<List<Integer>>();
		numberLists.add(Arrays.asList(5, 6));
		numberLists.add(Arrays.asList(3, 4));
		List<List<Integer>> actual = parsed.get(numbers);
		assertEquals("", numberLists, actual);
	}

	@Test
	public void testTwoParametersForNamedArgumentRepeatedSingle() throws ArgumentException
	{
		String[] args = {"--number", "1", "--number", "2"};

		Argument<List<Integer>> number = integerArgument("--number").repeated().build();

		ParsedArguments parsed = ArgumentParser.forArguments(number).parse(Arrays.asList(args));

		assertEquals(Arrays.asList(1, 2), parsed.get(number));
	}

	@Test(expected = UnhandledRepeatedArgument.class)
	public void testNamedArgumentRepeatedNotAllowed() throws ArgumentException
	{
		String[] args = {"-number", "5", "-number", "3"};

		Argument<Integer> numbers = integerArgument("-number").build();

		ArgumentParser.forArguments(numbers).parse(args);
	}

	@Test(expected = UnhandledRepeatedArgument.class)
	public void testTwoParametersForNamedArgumentRepeatedNotAllowed() throws ArgumentException
	{
		String[] args = {"--numbers", "5", "6", "--numbers", "3", "4"};

		Argument<List<Integer>> numbers = integerArgument("--numbers").arity(2).build();

		ArgumentParser.forArguments(numbers).parse(args);
	}

	@Test
	public void testRepeatedPropertyValues() throws ArgumentException
	{
		Argument<Map<String, List<Integer>>> numberMap = integerArgument("-N").repeated().asPropertyMap().build();

		ParsedArguments parsed = ArgumentParser.forArguments(numberMap).parse("-Nnumber=1", "-Nnumber=2");

		assertThat(parsed.get(numberMap).get("number")).isEqualTo(Arrays.asList(1, 2));
	}

	@Test
	public void testRepeatedAndSplitPropertyValues() throws ArgumentException
	{
		Argument<Map<String, List<List<Integer>>>> numberMap = integerArgument("-N").splitWith(new Comma()).repeated().asPropertyMap().build();

		ParsedArguments parsed = ArgumentParser.forArguments(numberMap).parse("-Nnumber=1,2", "-Nnumber=3,4");

		List<List<Integer>> expected =	new ArrayList<List<Integer>>();
		expected.add(Arrays.asList(1, 2));
		expected.add(Arrays.asList(3, 4));

		List<List<Integer>> actual = parsed.get(numberMap).get("number");

		assertThat(actual).isEqualTo(expected);
	}

	@Test(expected = InvalidArgument.class)
	public void testRepeatedPropertyValuesWithoutHandling() throws ArgumentException
	{
		Argument<Map<String, Integer>> numberMap = integerArgument("-N").asPropertyMap().build();

		ArgumentParser.forArguments(numberMap).parse("-Nnumber=1", "-Nnumber=2");
	}
}
