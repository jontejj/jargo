package se.j4j.argumentparser.stringparsers;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.StringParsers.asFunction;
import static se.j4j.argumentparser.StringParsers.integerParser;

import java.util.List;

import org.junit.Test;

public class StringParserAsFunctionTest
{
	@Test
	public void testAsFunction()
	{
		List<Integer> expected = asList(1, 3, 2);

		List<Integer> result = transform(asList("1", "3", "2"), asFunction(integerParser()));
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void testInvalidInputToFunction()
	{
		try
		{
			asFunction(integerParser()).apply("a1");
			fail("a1 should cause an exception");
		}
		catch(IllegalArgumentException e)
		{
			assertThat(e).hasMessage("'a1' is not a valid number.");
		}
	}
}
