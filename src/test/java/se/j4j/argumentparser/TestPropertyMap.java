package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.Limiters.positiveInteger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.exceptions.LimitException;
import se.j4j.argumentparser.exceptions.UnhandledRepeatedArgument;
import se.j4j.argumentparser.limiters.KeyLimiter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class TestPropertyMap
{
	@Test
	public void testSeveralValues() throws ArgumentException
	{
		Map<String, Integer> numberMap = integerArgument("-N").asPropertyMap().parse("-None=1", "-Ntwo=2");

		assertThat(numberMap.get("one")).isEqualTo(1);
		assertThat(numberMap.get("two")).isEqualTo(2);
	}

	@Test
	public void testStartsWithCollisionsForTwoSimiliarIdentifiers() throws ArgumentException
	{
		Map<String, Integer> numberMap = integerArgument("-NS", "-N").asPropertyMap().parse("-None=1", "-NStwo=2");

		assertThat(numberMap.get("one")).isEqualTo(1);
		assertThat(numberMap.get("two")).isEqualTo(2);

		numberMap = integerArgument("-N", "-NS").asPropertyMap().parse("-None=1", "-NStwo=2");

		assertThat(numberMap.get("one")).isEqualTo(1);
		assertThat(numberMap.get("Stwo")).isEqualTo(2);

		numberMap = integerArgument("-N", "-D").asPropertyMap().parse("-Done=1", "-Ntwo=2");

		assertThat(numberMap.get("one")).isEqualTo(1);
		assertThat(numberMap.get("two")).isEqualTo(2);
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
		integerArgument("-N").asPropertyMap().parse("-N3");
	}

	@Test
	public void testCustomSeparator() throws ArgumentException
	{
		Map<String, Integer> numberMap = integerArgument("-N").separator("/").asPropertyMap().parse("-Nkey/3");

		assertThat(numberMap.get("key")).isEqualTo(3);
	}

	@Test(expected = LimitException.class)
	public void testLimitationOfPropertyValues() throws ArgumentException
	{
		Argument<Map<String, Integer>> numberMap = integerArgument("-N").limitTo(positiveInteger()).asPropertyMap().build();

		ArgumentParser parser = ArgumentParser.forArguments(numberMap);
		ParsedArguments parsed = parser.parse("-None=1");

		assertThat(parsed.get(numberMap).get("one")).isEqualTo(1);

		parsed = parser.parse("-Nminus=-1");
	}

	@Test(expected = LimitException.class)
	public void testLimitationOfPropertyMapKeys() throws ArgumentException
	{
		integerArgument("-I").asPropertyMap().limitTo(new KeyLimiter<Integer>("foo", "bar")).parse("-Ifoo=10", "-Ibar=5", "-Izoo=9");
	}

	@Test
	public void testThatRepeatedPropertyKeysAreInvalidatedBeforeParsed() throws ArgumentException
	{
		try
		{
			integerArgument("-I").asPropertyMap().parse("-Ifoo=10", "-Ifoo=NotANumber");
			fail("Repeated key (and limited value) wasn't invalidated");
		}
		catch(UnhandledRepeatedArgument expected)
		{
			// TODO: assert printout
		}
	}

	@Test
	public void testLimitationOfPropertyMapKeysAndValues()
	{
		Argument<Map<String, Integer>> positiveArguments = integerArgument("-I").limitTo(positiveInteger()).asPropertyMap()
				.limitTo(new KeyLimiter<Integer>("foo", "bar")).build();

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
		Map<String, Integer> defaultMap = integerArgument("-N").asPropertyMap().parse();

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
		Map<String, Integer> numberMap = integerArgument("-N").asPropertyMap().parse("-None=1");
		try
		{
			numberMap.put("two", 2);
			fail("a propertyMap should be unmodifiable");
		}
		catch(UnsupportedOperationException expected)
		{
		}
	}

	@Test
	public void testThatPropertyMapsWithRepeatedValuesAreUnmodifiable() throws ArgumentException
	{
		Map<String, List<Integer>> numberMap = integerArgument("-N").repeated().asPropertyMap().parse("-Nfoo=1", "-Nfoo=2");
		try
		{
			numberMap.put("bar", Arrays.asList(1, 2));
			fail("a propertyMap should be unmodifiable");
		}
		catch(UnsupportedOperationException expected)
		{
		}
		try
		{
			numberMap.get("foo").add(3);
			fail("a list inside a propertyMap should be unmodifiable");
		}
		catch(UnsupportedOperationException expected)
		{
		}
	}
}
