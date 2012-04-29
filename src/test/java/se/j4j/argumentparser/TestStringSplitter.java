package se.j4j.argumentparser;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.stringsplitters.Comma;

public class TestStringSplitter
{
	@Test
	public void testSplittingWithComma() throws ArgumentException
	{
		List<Integer> numbers = integerArgument("-n").splitWith(new Comma()).parse("-n", "1,2");

		assertThat(numbers).isEqualTo(asList(1, 2));
	}

	/**
	 * <pre>
	 * "-n", "1,2", "3,4"
	 * isn't allowed
	 * </pre>
	 */
	@SuppressWarnings("deprecation")
	// This is what's tested
	@Test(expected = IllegalStateException.class)
	public void testArityCombinedWithSplitting()
	{
		integerArgument("-n").splitWith(new Comma()).arity(2);
	}

	@Test
	public void testArityCombinedWithPropertyMap() throws ArgumentException
	{
		Map<String, List<Integer>> numbers = integerArgument("-n").splitWith(new Comma()).asPropertyMap().parse("-nsmall=1,2", "-nbig=3,4");

		Map<String, List<Integer>> expected = new HashMap<String, List<Integer>>();
		expected.put("small", asList(1, 2));
		expected.put("big", asList(3, 4));
		assertThat(numbers).isEqualTo(expected);
	}

	@Test
	public void testSplittingCombinedWithRepeating() throws ArgumentException
	{
		List<List<Integer>> numbers = integerArgument("-n").separator("=").splitWith(new Comma()).repeated().parse("-n=1,2", "-n=3,4");

		List<List<Integer>> expected = new ArrayList<List<Integer>>();
		expected.add(asList(1, 2));
		expected.add(asList(3, 4));
		assertThat(numbers).isEqualTo(expected);
	}

	@Test(expected = ArgumentException.class)
	public void testSplittingWithNoArg() throws ArgumentException
	{
		integerArgument("-n").splitWith(new Comma()).parse("-n");
	}

	@Test
	public void testThatListsWithSplitValuesAreUnmodifiable() throws ArgumentException
	{
		List<Integer> numbers = integerArgument("-N").splitWith(new Comma()).parse("-N", "1,2");
		try
		{
			numbers.add(3);
			fail("a list of split values should be unmodifiable");
		}
		catch(UnsupportedOperationException expected)
		{

		}
	}

	@SuppressWarnings("deprecation")
	@Test(expected = UnsupportedOperationException.class)
	public void testArityCombinedWithPropertyMapWrongCallOrder()
	{
		// The intent is to guide the user of the API to how he should have used
		// it
		integerArgument("-n").asPropertyMap().splitWith(new Comma());
	}
}
