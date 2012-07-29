package se.j4j.argumentparser;

import static com.google.common.collect.ImmutableList.of;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.limiters.FooLimiter.foos;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentExceptions.UnhandledRepeatedArgument;

import com.google.common.collect.ImmutableList;

public class RepeatedArgumentTest
{
	@Test
	public void testRepeatedIntegerArgument() throws ArgumentException
	{
		String[] args = {"--number", "1", "--number", "2"};

		List<Integer> numbers = integerArgument("--number").repeated().parse(args);

		assertThat(numbers).isEqualTo(Arrays.asList(1, 2));
	}

	@Test
	public void testRepeatedArityArgument() throws ArgumentException
	{
		String[] args = {"--numbers", "5", "6", "--numbers", "3", "4"};
		List<List<Integer>> numbers = integerArgument("--numbers").arity(2).repeated().parse(args);
		assertThat(numbers).isEqualTo(ImmutableList.of(of(5, 6), of(3, 4)));
	}

	@Test(expected = UnhandledRepeatedArgument.class)
	public void testUnhandledRepition() throws ArgumentException
	{
		integerArgument("-number").parse("-number", "5", "-number", "3");
	}

	@Test(expected = UnhandledRepeatedArgument.class)
	public void testUnhandledRepitionForArityArgument() throws ArgumentException
	{
		integerArgument("--numbers").arity(2).parse("--numbers", "5", "6", "--numbers", "3", "4");
	}

	@Test
	public void testRepeatedPropertyValues() throws ArgumentException
	{
		Map<String, List<Integer>> numberMap = integerArgument("-N").repeated().asPropertyMap().parse("-Nnumber=1", "-Nnumber=2");
		assertThat(numberMap.get("number")).isEqualTo(Arrays.asList(1, 2));
	}

	@Test
	public void testRepeatedAndSplitPropertyValues() throws ArgumentException
	{
		Map<String, List<List<Integer>>> numberMap = integerArgument("-N").splitWith(",").repeated().asPropertyMap()
				.parse("-Nnumber=1,2", "-Nnumber=3,4");

		List<ImmutableList<Integer>> expected = ImmutableList.of(of(1, 2), of(3, 4));

		assertThat(numberMap.get("number")).isEqualTo(expected);
	}

	@Test(expected = UnhandledRepeatedArgument.class)
	public void testRepeatedPropertyValuesWithoutHandling() throws ArgumentException
	{
		integerArgument("-N").asPropertyMap().parse("-Nnumber=1", "-Nnumber=2");
	}

	@Test(expected = UnhandledRepeatedArgument.class)
	public void testInvalidValuesShouldNotBeParsedIfRepeatedArgumentsAreNotAllowed() throws ArgumentException
	{
		stringArgument("-n").limitTo(foos()).parse("-n", "foo", "-n", "bar");
	}

	@Test
	public void testThatListsWithRepeatedValuesAreUnmodifiable() throws ArgumentException
	{
		List<Integer> numberList = integerArgument("-N").repeated().parse("-N", "1", "-N", "-2");
		try
		{
			numberList.add(3);
			fail("a list of repeated values should be unmodifiable");
		}
		catch(UnsupportedOperationException expected)
		{
		}
	}

	@SuppressWarnings("deprecation")
	// This is what's tested
	@Test(expected = IllegalStateException.class)
	public void testCallingRepeatedBeforeArity()
	{
		integerArgument("--number").repeated().arity(2);
	}

	@SuppressWarnings("deprecation")
	// This is what's tested
	@Test(expected = IllegalStateException.class)
	public void testCallingRepeatedBeforeVariableArity()
	{
		integerArgument("--number").repeated().variableArity();
	}

	@SuppressWarnings("deprecation")
	// This is what's tested
	@Test(expected = IllegalStateException.class)
	public void testCallingSplitWithAfterRepeated()
	{
		integerArgument().repeated().splitWith(",");
	}
}
