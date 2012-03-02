package se.j4j.argumentparser.utils;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import se.j4j.argumentparser.utils.StringComparison;
import se.j4j.argumentparser.utils.Strings;

import com.google.common.collect.Lists;

public class TestStrings
{
	@Test
	public void testAppendSpaces()
	{
		StringBuilder builder = new StringBuilder();
		Strings.appendSpaces(5, builder);
		assertThat(builder.toString()).isEqualTo("     ");
	}


	@Test
	public void testFuzzyMatching()
	{
		String input = "stats";
		List<String> strings = Lists.newArrayList("logging", "help", "status");

		assertThat(StringComparison.closestMatch(input, strings)).isEqualTo("status");
	}
}
