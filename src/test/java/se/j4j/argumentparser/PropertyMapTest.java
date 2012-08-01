package se.j4j.argumentparser;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.byteArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.Limiters.range;
import static se.j4j.argumentparser.StringParsers.byteParser;
import static se.j4j.argumentparser.StringParsers.integerParser;
import static se.j4j.argumentparser.StringParsers.lowerCaseParser;
import static se.j4j.argumentparser.StringParsers.Radix.BINARY;
import static se.j4j.argumentparser.StringParsers.Radix.HEX;
import static se.j4j.argumentparser.internal.Platform.NEWLINE;
import static se.j4j.argumentparser.limiters.FooLimiter.foos;
import static se.j4j.argumentparser.utils.UsageTexts.expected;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.CommandLineParser.ArgumentIterator;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.StringParsers.KeyValueParser;
import se.j4j.argumentparser.stringparsers.custom.LimitedKeyParser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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

		ParsedArguments parsed = CommandLineParser.forArguments(numberMap, number).parse("-None=1", "-Ntwo=2", "-N", "3");

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

	@Test(expected = ArgumentException.class)
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

	@Test(expected = ArgumentException.class)
	public void testLimitationOfPropertyValues() throws ArgumentException
	{
		Argument<Map<String, String>> fooArgument = stringArgument("-N").limitTo(foos()).asPropertyMap().build();

		CommandLineParser parser = CommandLineParser.forArguments(fooArgument);
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
		Argument<Map<String, Integer>> argument = integerArgument("-I").limitTo(range(0, 10))
				.asKeyValuesWithKeyParser(new LimitedKeyParser("foo", "bar")).build();

		CommandLineParser parser = CommandLineParser.forArguments(argument);

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
			assertThat(invalidBar).hasMessage("'-1' is not in the range 0 to 10 (decimal)");
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

	@Test
	@SuppressFBWarnings(value = "NP_NONNULL_PARAM_VIOLATION", justification = "unreferenced in code path")
	public void testThatKeyValueParserBehaveCivilizedWhenNoNamesMatchTheArgument() throws ArgumentException
	{
		// In all honesty, this is for code coverage:)
		Argument<String> badArgument = stringArgument().build();
		KeyValueParser<String, String> parser = new StringParsers.KeyValueParser<String, String>(null, null, Limiters.<String>noLimits());

		ArgumentIterator arguments = ArgumentIterator.forSingleArgument("-Nfoo=bar");
		Map<String, String> parsedResult = parser.parse(arguments, null, badArgument);

		assertThat(parsedResult).isEmpty();
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
		defaults.put((byte) 0x0F, (byte) 0b1001);
		defaults.put((byte) 0x0E, (byte) 0b0110);

		String usage = byteArgument("-N").radix(BINARY).asKeyValuesWithKeyParser(byteParser(HEX)).separator(":").defaultValue(defaults).usage("");
		assertThat(usage).isEqualTo(expected("defaultValuePropertyMap"));
	}

	@Test
	public void testThatSeparatorsWithSeveralCharactersArePossible() throws ArgumentException
	{
		String value = stringArgument("-N").asPropertyMap().separator("==").parse("-Nkey==value").get("key");
		assertThat(value).isEqualTo("value");
	}

	@Test(expected = IllegalStateException.class)
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
	public void testThatDefaultValuesInPropertyMapIsDescribable()
	{
		Map<String, Integer> defaults = newLinkedHashMap();
		defaults.put("population", 42);
		defaults.put("hello", 1);

		String usage = integerArgument("-N").asPropertyMap().defaultValue(defaults).defaultValueDescription(new KeyDescriber()).usage("");
		assertThat(usage).isEqualTo(expected("defaultValuesDescribedInPropertyMap"));
	}

	private static final class KeyDescriber implements Describer<Map<String, Integer>>
	{

		@Override
		public String describe(Map<String, Integer> values)
		{
			StringBuilder result = new StringBuilder();
			for(String key : values.keySet())
			{
				result.append(key);
				result.append("=");
				result.append(values.get(key));
				switch(key)
				{
					case "population":
						result.append(NEWLINE + " The number of citizens in the world" + NEWLINE);
						break;
					case "hello":
						result.append(NEWLINE + " The number of times to say hello" + NEWLINE);
						break;
					default:
						fail("Undescribed property");
						break;
				}
			}
			return result.toString();
		}
	}
}
