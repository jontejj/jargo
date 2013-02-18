package se.j4j.argumentparser;

import static java.util.Locale.US;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.bigDecimalArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.testlib.Locales.SWEDISH;
import static se.j4j.testlib.Locales.TURKISH;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Test;

import se.j4j.testlib.Locales;

/**
 * Tests for {@link CommandLineParser#locale(Locale)} and {@link ArgumentBuilder#locale(Locale)}
 */
public class LocaleTest
{
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
	}

	@Test
	public void testThatLocaleOverrideForSpecificArgumentDoesNotAffectOthers() throws Exception
	{
		Argument<BigDecimal> swedishNumber = bigDecimalArgument().locale(Locales.SWEDISH).build();
		Argument<BigDecimal> usNumber = bigDecimalArgument().build();
		ParsedArguments result = CommandLineParser.withArguments(swedishNumber, usNumber).locale(Locale.US).parse("1,000", "1,000");
		assertThat(result.get(swedishNumber)).isEqualTo(new BigDecimal("1.000"));
		assertThat(result.get(usNumber)).isEqualTo(BigDecimal.valueOf(1000));
	}

	@AfterClass
	public static void resetDefaultLocale()
	{
		Locales.resetDefaultLocale();
	}
}
