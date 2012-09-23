package se.j4j.argumentparser;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.byteArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.StringParsers.byteParser;
import static se.j4j.argumentparser.StringParsers.integerParser;
import static se.j4j.argumentparser.StringParsers.lowerCaseParser;
import static se.j4j.argumentparser.limiters.FooLimiter.foos;
import static se.j4j.argumentparser.utils.UsageTexts.expected;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.stringparsers.custom.LimitedKeyParser;
import se.j4j.testlib.Explanation;
import se.j4j.texts.Texts;

import com.google.common.base.Predicate;
import com.google.common.collect.Ranges;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Tests for {@link ArgumentBuilder#asPropertyMap()} and
 * {@link ArgumentBuilder#asKeyValuesWithKeyParser(StringParser)}
 */
public class PropertyMapTest
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
		assertThat(numberMap.get("two")).isEqualTo(2);

		numberMap = integerArgument("-N", "-D").asPropertyMap().parse("-Done=1", "-Ntwo=2");

		assertThat(numberMap.get("one")).isEqualTo(1);
		assertThat(numberMap.get("two")).isEqualTo(2);
	}

	@Test
	public void testNameCollisionWithOrdinaryArgument() throws ArgumentException
	{
		Argument<Map<String, Integer>> numberMap = integerArgument("-N").asPropertyMap().build();
		Argument<Integer> number = integerArgument("-N").ignoreCase().build();

		ParsedArguments parsed = CommandLineParser.withArguments(numberMap, number).parse("-None=1", "-Ntwo=2", "-N", "3");

		assertThat(parsed.get(numberMap).get("one")).isEqualTo(1);
		assertThat(parsed.get(numberMap).get("two")).isEqualTo(2);
		assertThat(parsed.get(number)).isEqualTo(3);
	}

	@Test(expected = RuntimeException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testPropertyMapWithoutLeadingIdentifier()
	{
		integerArgument().asPropertyMap().build();
	}

	@Test
	public void testInvalidationOfWrongSeparator()
	{
		try
		{
			integerArgument("-N").asPropertyMap().parse("-N3");
			fail("-N3 should be missing a =");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected).hasMessage(String.format(Texts.MISSING_KEY_VALUE_SEPARATOR, "-N", "3", "="));
		}
	}

	@Test
	public void testCustomSeparator() throws ArgumentException
	{
		Map<String, Integer> numberMap = integerArgument("-N").separator("/").asPropertyMap().parse("-Nkey/3");

		assertThat(numberMap.get("key")).isEqualTo(3);
	}

	@Test(expected = ArgumentException.class)
	public void testLimitationOfPropertyValues() throws ArgumentException
	{
		Argument<Map<String, String>> fooArgument = stringArgument("-N").limitTo(foos()).asPropertyMap().build();

		CommandLineParser parser = CommandLineParser.withArguments(fooArgument);
		ParsedArguments parsed = parser.parse("-Nbar=foo");

		assertThat(parsed.get(fooArgument).get("bar")).isEqualTo("foo");

		parser.parse("-Nbar=bar");
	}

	@Test(expected = ArgumentException.class)
	public void testLimitationOfPropertyMapKeys() throws ArgumentException
	{
		integerArgument("-I").asKeyValuesWithKeyParser(new LimitedKeyParser("foo", "bar")).parse("-Ifoo=10", "-Ibar=5", "-Izoo=9");
	}

	@Test
	public void testThatRepeatedPropertyKeysAreInvalidatedBeforeParsed()
	{
		try
		{
			integerArgument("-I").asPropertyMap().parse("-Ifoo=10", "-Ifoo=NotANumber");
			fail("Repeated key wasn't invalidated");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected).hasMessage("'-Ifoo' was found as a key several times in the input.");
		}
	}

	@Test
	public void testLimitationOfPropertyMapKeysAndValues()
	{
		Predicate<Integer> zeroToTen = Ranges.closed(0, 10);
		Argument<Map<String, Integer>> argument = integerArgument("-I").limitTo(zeroToTen)
				.asKeyValuesWithKeyParser(new LimitedKeyParser("foo", "bar")).build();

		CommandLineParser parser = CommandLineParser.withArguments(argument);

		try
		{
			parser.parse("-Ifoo=1", "-Ibar=2", "-Izoo=3");
			fail("Didn't invalidate zoo key");
		}
		catch(ArgumentException expected)
		{
			String usage = expected.getMessageAndUsage("LimitedKeysAndLimitedValues");
			assertThat(usage).isEqualTo(expected("limiterUsageForBothValueLimiterAndKeyLimiter"));
		}

		try
		{
			parser.parse("-Ifoo=1", "-Ibar=-1");
			fail("Didn't invalidate bar with negative value");
		}
		catch(ArgumentException invalidBar)
		{
			assertThat(invalidBar).hasMessage(String.format(Texts.UNALLOWED_VALUE, -1, zeroToTen));
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
	public void testCustomKeyParser() throws ArgumentException
	{
		assertThat(integerArgument("-N").asKeyValuesWithKeyParser(integerParser()).parse("-N1=42").get(1)).isEqualTo(42);
	}

	@Test
	public void testInvalidationOfRepeatedValues()
	{
		try
		{
			integerArgument("-N").asKeyValuesWithKeyParser(integerParser()).parse("-N1=42", "-N1=43");
			fail("-N keys should not be allowed to be repeated");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected).hasMessage(String.format(Texts.UNALLOWED_REPETITION_OF_KEY, "-N", "1"));
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

	@SuppressWarnings("deprecation")
	@Test(expected = IllegalStateException.class)
	public void testThatRepeatedMustBeCalledBeforeAsPropertyMap()
	{
		integerArgument("-N").asPropertyMap().repeated();
	}

	@Test
	public void testThatIterationOrderForPropertyKeysIsTheSameAsFromTheCommandLine() throws ArgumentException
	{
		Map<String, Integer> map = integerArgument("-I").asPropertyMap().parse("-Itldr=7", "-Ifoo=10", "-Ibar=5");
		List<String> keys = newArrayList();
		for(String key : map.keySet())
		{
			keys.add(key);
		}
		assertThat(keys).isEqualTo(asList("tldr", "foo", "bar"));
	}

	@Test
	public void testThatPropertyKeysCanBeMadeIntoLowerCase() throws ArgumentException
	{
		Argument<Map<String, Integer>> arg = integerArgument("-I").asKeyValuesWithKeyParser(lowerCaseParser()).build();
		Map<String, Integer> map = arg.parse("-IFOO=1", "-IBar=2", "-Izoo=3");
		assertThat(map.get("foo")).isEqualTo(1);
		assertThat(map.get("bar")).isEqualTo(2);
		assertThat(map.get("zoo")).isEqualTo(3);

		assertThat(arg.usage("LowerCase")).isEqualTo(expected("propertyMaps"));
		assertThat(lowerCaseParser().defaultValue()).isEmpty();
	}

	@Test
	public void testDefaultValuesInUsageForPropertyMap()
	{
		Map<Byte, Byte> defaults = newLinkedHashMap();
		defaults.put((byte) 3, (byte) 4);
		defaults.put((byte) 1, (byte) 2);

		String usage = byteArgument("-N").asKeyValuesWithKeyParser(byteParser()).separator(":").defaultValue(defaults).usage("");
		assertThat(usage).isEqualTo(expected("defaultValuePropertyMap"));
	}

	@Test
	public void testThatSeparatorsWithSeveralCharactersArePossible() throws ArgumentException
	{
		String value = stringArgument("-N").asPropertyMap().separator("==").parse("-Nkey==value").get("key");
		assertThat(value).isEqualTo("value");
	}

	@Test(expected = IllegalStateException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testThatZeroCharacterSeparatorIsForbidden()
	{
		stringArgument("-N").separator("").asPropertyMap().build();
	}

	@Test
	public void testThatUsageTextForRepeatedPropertyValuesLooksGood()
	{
		String usage = integerArgument("-N").repeated().asPropertyMap().description("Some measurement values").usage("");
		assertThat(usage).isEqualTo(expected("repeatedPropertyValues"));
	}

	@Test
	public void testSeparatorInName() throws ArgumentException
	{
		Integer ten = integerArgument("-N;").separator(";").asPropertyMap().parse("-N;foo;10").get("foo");
		assertThat(ten).isEqualTo(10);
	}

	@Test
	public void testLimitWithForRepeatedPropertyValues()
	{
		try
		{
			stringArgument("-N").limitTo(foos()).repeated().asPropertyMap().parse("-Nkey=foo", "-Nkey=bar");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected).hasMessage("'bar' is not foo");
		}
	}
}
