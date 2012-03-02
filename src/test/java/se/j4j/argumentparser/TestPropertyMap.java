package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;

import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.validators.PositiveInteger;

public class TestPropertyMap
{
	@Test
	public void testSeveralValues() throws ArgumentException
	{
		Argument<Map<String, Integer>> numberMap = integerArgument("-N").asPropertyMap().build();

		ParsedArguments parsed = ArgumentParser.forArguments(numberMap).parse("-None=1", "-Ntwo=2");

		assertThat(parsed.get(numberMap).get("one")).isEqualTo(1);
		assertThat(parsed.get(numberMap).get("two")).isEqualTo(2);
	}

	//TODO: make sure properties work with ignore case, separator
	//TODO: what should happen when names = -N, -NS for a propertyMap? and -NShej=svejs is given?

	@Test
	public void testNameCollisionWithOrdinaryArgument() throws ArgumentException
	{
		Argument<Map<String, Integer>> numberMap = integerArgument("-N").asPropertyMap().build();
		Argument<Integer> number = integerArgument("-N").ignoreCase().build();

		ParsedArguments parsed = ArgumentParser.forArguments(numberMap, number).parse("-None=1", "-Ntwo=2", "-N", "3");

		assertThat(parsed.get(numberMap).get("one")).isEqualTo(1);
		assertThat(parsed.get(numberMap).get("two")).isEqualTo(2);
		assertThat(parsed.get(number)).isEqualTo(3);
	}

	@Test(expected = InvalidArgument.class)
	public void testInvalidationOfWrongSeparator() throws ArgumentException
	{
		Argument<Map<String, Integer>> numberMap = integerArgument("-N").asPropertyMap().build();

		ArgumentParser.forArguments(numberMap).parse("-N3");
	}

	@Test(expected = InvalidArgument.class)
	public void testValidationOfPropertyValues() throws ArgumentException
	{
		Argument<Map<String, Integer>> numberMap = integerArgument("-N").
				validator(new PositiveInteger())
				.asPropertyMap().build();

		ArgumentParser parser = ArgumentParser.forArguments(numberMap);
		ParsedArguments parsed = parser.parse("-None=1");

		assertThat(parsed.get(numberMap).get("one")).isEqualTo(1);

		parsed = parser.parse("-Nminus=-1");
	}
}
