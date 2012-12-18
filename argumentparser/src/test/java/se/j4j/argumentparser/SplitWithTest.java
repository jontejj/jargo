package se.j4j.argumentparser;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.utils.ExpectedTexts.expected;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.j4j.testlib.Explanation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Tests for {@link ArgumentBuilder#splitWith(String)}
 */
public class SplitWithTest
{
	@Test
	public void testSplittingWithComma() throws ArgumentException
	{
		List<Integer> numbers = integerArgument("-n").splitWith(",").parse("-n", "1,2");

		assertThat(numbers).isEqualTo(asList(1, 2));
	}

	@Test
	public void testThatUsageTextForSplittingWithCommaLooksGood()
	{
		String usage = integerArgument("-n").splitWith(",").usage("Split");
		assertThat(usage).isEqualTo(expected("splitArgument"));
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testThatSplittingWithEmptyStringIsForbidden()
	{
		integerArgument("-n").splitWith("");
	}

	@Test
	public void testThatSeparatorIsPrintedBetweenArgumentNameAndMetaDescription()
	{
		String usage = integerArgument("-N").separator("=").usage("SeparatorBetweenNameAndMeta");

		assertThat(usage).isEqualTo(expected("separatorBetweenNameAndMeta"));
	}

	@Test
	public void testThatMissingParameterForSplitArgumentLooksGoodInUsage()
	{
		try
		{
			integerArgument("--numbers", "-n").splitWith(",").parse("-n");
		}
		catch(ArgumentException expected)
		{
			// As -n was given from the command line, that is what should be displayed to the user
			assertThat(expected).hasMessage("Missing <integer> parameter for -n");
		}
	}

	@Test
	public void testThatSeparatorCombinedWithSplitterLooksGoodInUsage()
	{
		String usage = integerArgument("-N").separator("=").splitWith(",").usage("SeparatorCombinedWithSplitter");
		assertThat(usage).isEqualTo(expected("separatorCombinedWithSplitter"));
	}

	@Test
	public void testSplitWithCombinedWithPropertyMap() throws ArgumentException
	{
		Map<String, List<Integer>> numbers = integerArgument("-n").splitWith(",").asPropertyMap().parse("-nsmall=1,2", "-nbig=3,4");

		Map<String, List<Integer>> expected = new HashMap<String, List<Integer>>();
		expected.put("small", asList(1, 2));
		expected.put("big", asList(3, 4));
		assertThat(numbers).isEqualTo(expected);
	}

	@Test
	public void testSplittingCombinedWithRepeating() throws ArgumentException
	{
		List<List<Integer>> numbers = integerArgument("-n").separator("=").splitWith(",").repeated().parse("-n=1,2", "-n=3,4");

		List<List<Integer>> expected = new ArrayList<List<Integer>>();
		expected.add(asList(1, 2));
		expected.add(asList(3, 4));
		assertThat(numbers).isEqualTo(expected);
	}

	@Test(expected = ArgumentException.class)
	public void testSplittingWithNoArg() throws ArgumentException
	{
		integerArgument("-n").splitWith(",").parse("-n");
	}

	@Test
	public void testThatListsWithSplitValuesAreUnmodifiable() throws ArgumentException
	{
		List<Integer> numbers = integerArgument("-N").splitWith(",").parse("-N", "1,2");
		try
		{
			numbers.add(3);
			fail("a list of split values should be unmodifiable");
		}
		catch(UnsupportedOperationException expected)
		{

		}
	}

	// This is what's tested
	@SuppressWarnings("deprecation")
	@Test(expected = IllegalStateException.class)
	public void testArityCombinedWithPropertyMapWrongCallOrder()
	{
		// The intent is to guide the user of the API to how he should have used
		// it
		integerArgument("-n").asPropertyMap().splitWith(",");
	}

	/**
	 * <pre>
	 * "-n", "1,2", "3,4"
	 * isn't allowed
	 * </pre>
	 */
	// This is what's tested
	@SuppressWarnings("deprecation")
	@Test(expected = IllegalStateException.class)
	public void testArityCombinedWithSplitting()
	{
		integerArgument("-n").splitWith(",").arity(2);
	}

	@SuppressWarnings("deprecation")
	// This is what's tested
	@Test(expected = IllegalStateException.class)
	public void testVariableArityCombinedWithSplitting()
	{
		integerArgument("-n").splitWith(",").variableArity();
	}
}
