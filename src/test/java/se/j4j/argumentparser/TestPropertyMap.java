package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;

import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.Validators.KeyValidator;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.exceptions.UnhandledRepeatedArgument;
import se.j4j.argumentparser.validators.PositiveInteger;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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

	@Test
	public void testStartsWithCollisionsForTwoSimiliarIdentifiers() throws ArgumentException
	{
		Argument<Map<String, Integer>> numberMap = integerArgument("-NS", "-N").asPropertyMap().build();

		ParsedArguments parsed = ArgumentParser.forArguments(numberMap).parse("-None=1", "-NStwo=2");

		assertThat(parsed.get(numberMap).get("one")).isEqualTo(1);
		assertThat(parsed.get(numberMap).get("two")).isEqualTo(2);

		numberMap = integerArgument("-N", "-NS").asPropertyMap().build();

		parsed = ArgumentParser.forArguments(numberMap).parse("-None=1", "-NStwo=2");

		assertThat(parsed.get(numberMap).get("one")).isEqualTo(1);
		assertThat(parsed.get(numberMap).get("Stwo")).isEqualTo(2);

		numberMap = integerArgument("-N", "-D").asPropertyMap().build();

		parsed = ArgumentParser.forArguments(numberMap).parse("-Done=1", "-Ntwo=2");

		assertThat(parsed.get(numberMap).get("one")).isEqualTo(1);
		assertThat(parsed.get(numberMap).get("two")).isEqualTo(2);
	}

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

	@Test(expected = RuntimeException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Expecting fail-fast during construction")
	public void testPropertyMapWithoutLeadingIdentifier()
	{
		integerArgument().asPropertyMap().build();
	}

	@Test(expected = InvalidArgument.class)
	public void testInvalidationOfWrongSeparator() throws ArgumentException
	{
		Argument<Map<String, Integer>> numberMap = integerArgument("-N").asPropertyMap().build();

		ArgumentParser.forArguments(numberMap).parse("-N3");
	}

	@Test
	public void testCustomSeparator() throws ArgumentException
	{
		Argument<Map<String, Integer>> numberMap = integerArgument("-N").separator("/").asPropertyMap().build();

		ParsedArguments parsed = ArgumentParser.forArguments(numberMap).parse("-Nkey/3");
		assertThat(parsed.get(numberMap).get("key")).isEqualTo(3);
	}

	@Test(expected = InvalidArgument.class)
	public void testValidationOfPropertyValues() throws ArgumentException
	{
		Argument<Map<String, Integer>> numberMap = integerArgument("-N").validator(new PositiveInteger()).asPropertyMap().build();

		ArgumentParser parser = ArgumentParser.forArguments(numberMap);
		ParsedArguments parsed = parser.parse("-None=1");

		assertThat(parsed.get(numberMap).get("one")).isEqualTo(1);

		parsed = parser.parse("-Nminus=-1");
	}

	@Test(expected = InvalidArgument.class)
	public void testValidationOfPropertyMapKeys() throws ArgumentException
	{
		Argument<Map<String, Integer>> positiveArguments = integerArgument("-I").asPropertyMap().validator(new KeyValidator<Integer>("foo", "bar"))
				.build();

		ArgumentParser.forArguments(positiveArguments).parse("-Ifoo=10", "-Ibar=5", "-Izoo=9");
	}

	@Test
	public void testThatRepeatedPropertyKeysAreInvalidatedBeforeParsed() throws ArgumentException
	{
		Argument<Map<String, Integer>> positiveArguments = integerArgument("-I").asPropertyMap().build();

		try
		{
			ArgumentParser.forArguments(positiveArguments).parse("-Ifoo=10", "-Ifoo=NotANumber");
			fail("Repeated key (and invalid value) wasn't invalidated");
		}
		catch(UnhandledRepeatedArgument expected)
		{
			// TODO: assert printout
		}
	}

	@Test
	public void testValidationOfPropertyMapKeysAndValues()
	{
		Argument<Map<String, Integer>> positiveArguments = integerArgument("-I").validator(new PositiveInteger()).asPropertyMap()
				.validator(new KeyValidator<Integer>("foo", "bar")).build();

		ArgumentParser parser = ArgumentParser.forArguments(positiveArguments);

		try
		{
			parser.parse("-Ifoo=10", "-Ibar=5", "-Izoo=9");
			fail("Didn't invalidate zoo key");
		}
		catch(ArgumentException expected)
		{

		}

		try
		{
			parser.parse("-Ifoo=10", "-Ibar=-5");
			fail("Didn't invalidate bar with negative value");
		}
		catch(ArgumentException expected)
		{
		}
	}

	@Test
	public void testThatPropertyValuesDefaultToAnUnmodifiableEmptyMap() throws ArgumentException
	{
		Argument<Map<String, Integer>> numberMap = integerArgument("-N").asPropertyMap().build();
		ArgumentParser parser = ArgumentParser.forArguments(numberMap);

		Map<String, Integer> defaultMap = parser.parse().get(numberMap);
		assertThat(defaultMap).isEmpty();

		try
		{

			defaultMap.put("a", 42);
			fail("the defaultMap should be unmodifiable");
		}
		catch(UnsupportedOperationException expected)
		{

		}
	}

	@Test
	public void testThatPropertyMapsAreUnmodifiable() throws ArgumentException
	{
		Argument<Map<String, Integer>> numberMap = integerArgument("-N").asPropertyMap().build();
		ArgumentParser parser = ArgumentParser.forArguments(numberMap);

		Map<String, Integer> map = parser.parse("-None=1").get(numberMap);
		try
		{

			map.put("two", 2);
			fail("a propertyMap should be unmodifiable");
		}
		catch(UnsupportedOperationException expected)
		{

		}
	}
}
