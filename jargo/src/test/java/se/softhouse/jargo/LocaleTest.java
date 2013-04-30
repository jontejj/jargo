/* Copyright 2013 Jonatan Jönsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package se.softhouse.jargo;

import static java.util.Locale.US;
import static org.fest.assertions.Assertions.assertThat;
import static se.softhouse.common.testlib.Locales.SWEDISH;
import static se.softhouse.common.testlib.Locales.TURKISH;
import static se.softhouse.jargo.Arguments.bigDecimalArgument;
import static se.softhouse.jargo.Arguments.integerArgument;
import static se.softhouse.jargo.StringParsers.integerParser;
import static se.softhouse.jargo.utils.Assertions2.assertThat;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import se.softhouse.common.strings.Describer;
import se.softhouse.common.testlib.Locales;

/**
 * Tests for {@link CommandLineParser#locale(Locale)} and {@link ArgumentBuilder#locale(Locale)}
 */
public class LocaleTest
{
	@Test
	public void testThatTheUSLocaleIsUsedByDefault() throws Exception
	{
		// Set the locale to something other than US so that when americans run the unit test, it
		// still verifies what it should
		Locales.setDefault(SWEDISH);
		Argument<Integer> n = Arguments.withParser(new ForwardingStringParser.SimpleForwardingStringParser<Integer>(integerParser()){
			@Override
			public Integer parse(String value, Locale locale) throws ArgumentException
			{
				assertThat(locale).isEqualTo(Locale.US);
				return super.parse(value, locale);
			}

			@Override
			public String descriptionOfValidValues(Locale locale)
			{
				assertThat(locale).isEqualTo(Locale.US);
				return super.descriptionOfValidValues(locale);
			}
		}).build();
		assertThat(n.parse("1")).isEqualTo(1);
		assertThat(CommandLineParser.withArguments(n).parse("1").get(n)).isEqualTo(1);
		assertThat(n.usage()).contains("1");
		Locales.resetDefaultLocale();
	}

	@Test
	public void testThatDefaultLocaleIsNotUsed() throws Exception
	{
		BigDecimal d = bigDecimalArgument().locale(SWEDISH).parse("123400,987");
		assertThat(d).isEqualTo(BigDecimal.valueOf(123400.987));

		d = bigDecimalArgument().locale(US).parse("123400,987");
		assertThat(d).isEqualTo(BigDecimal.valueOf(123400987));
	}

	@Test
	public void testThatArgumentNameIsNotLocaleDependentDuringSetup() throws InterruptedException, ArgumentException
	{
		Locales.setDefault(TURKISH);
		Argument<Integer> integer = integerArgument("-I").ignoreCase().build();
		Argument<Map<String, Integer>> asPropertyMap = integerArgument("I").ignoreCase().asPropertyMap().build();
		// If CommandLineParser.withArguments were implemented by using Locale.getDefault()
		// it would have lowercase'd "I" to "ı" making the call to parse
		// with "i" failing to find the right argument resulting in a ArgumentException
		CommandLineParser parser = CommandLineParser.withArguments(integer, asPropertyMap);
		Locales.setDefault(US);

		ParsedArguments results = parser.parse("-i", "1", "ii=2");
		assertThat(results.get(integer)).isEqualTo(1);

		assertThat(results.get(asPropertyMap).get("i")).isEqualTo(2);
		Locales.resetDefaultLocale();
	}

	@Test
	public void testThatArgumentNameIsNotLocaleDependentDuringParsing() throws InterruptedException, ArgumentException
	{
		Locales.setDefault(US);
		Argument<Integer> integer = integerArgument("-i").ignoreCase().build();
		Argument<Map<String, Integer>> asPropertyMap = integerArgument("i").ignoreCase().asPropertyMap().build();
		CommandLineParser parser = CommandLineParser.withArguments(integer, asPropertyMap);
		Locales.setDefault(TURKISH);
		// If CommandLineParser.parse were implemented by using Locale.getDefault() it
		// would not find the lowercase'd "i" because "i" in the turkish locale in lower case is "ı"
		ParsedArguments results = parser.parse("-I", "1", "Ii=2");
		assertThat(results.get(integer)).isEqualTo(1);

		assertThat(results.get(asPropertyMap).get("i")).isEqualTo(2);
		Locales.resetDefaultLocale();
	}

	@Test
	public void testThatLocaleOverrideForSpecificArgumentDoesNotAffectOthers() throws Exception
	{
		Argument<BigDecimal> usNumber = bigDecimalArgument().locale(US).build();
		Argument<BigDecimal> swedishNumber = bigDecimalArgument().build();

		ParsedArguments result = CommandLineParser.withArguments(swedishNumber, usNumber).locale(SWEDISH).parse("1,000", "1,000");

		assertThat(result.get(usNumber)).isEqualTo(BigDecimal.valueOf(1000));
		assertThat(result.get(swedishNumber)).isEqualTo(new BigDecimal("1.000"));
	}

	@Test
	public void testThatCustomDefaultValueDescriberUsesLocaleOverrideForSpecificArgument() throws Exception
	{
		Argument<BigDecimal> usNumber = bigDecimalArgument().locale(US).defaultValueDescriber(new Describer<BigDecimal>(){
			@Override
			public String describe(BigDecimal value, Locale inLocale)
			{
				assertThat(inLocale).as("default values should be described by argument specific locale if specified").isEqualTo(US);
				return "did run";
			}
		}).build();
		Argument<BigDecimal> swedishNumber = bigDecimalArgument().build();

		Usage result = CommandLineParser.withArguments(swedishNumber, usNumber).locale(SWEDISH).usage();
		assertThat(result).contains("did run");
	}
}
