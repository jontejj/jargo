package se.j4j.argumentparser.stringparsers.custom;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.withParser;
import static se.j4j.argumentparser.StringParsers.stringParser;
import static se.j4j.argumentparser.stringparsers.custom.DateTimeParser.dateArgument;
import static se.j4j.argumentparser.utils.UsageTexts.expected;

import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ForwardingStringParser;
import se.j4j.argumentparser.StringParser;

public class CustomStringParserTest
{
	@Test
	public void testHostPort() throws ArgumentException
	{
		HostPort hostPort = withParser(new HostPortParser()).names("-target").parse("-target", "example.com:8080");
		assertThat(hostPort.host).isEqualTo("example.com");
		assertThat(hostPort.port).isEqualTo(8080);
	}

	@Test
	public void testUniqueLetters() throws ArgumentException
	{
		Set<Character> letters = withParser(new UniqueLetters()).names("-l").parse("-l", "aabc");

		assertThat(letters).containsOnly('c', 'a', 'b');
	}

	@Test
	public void testDateArgument() throws ArgumentException
	{
		assertThat(dateArgument("--start").parse("--start", "2011-03-30")).isEqualTo(new DateTime("2011-03-30"));

		String usage = dateArgument("--start").usage("");
		assertThat(usage).isEqualTo(expected("dateTime"));
	}

	@Test
	public void testForwaringStringParser() throws ArgumentException
	{
		String argumentValue = "bar";
		String result = withParser(new InterningStringParser()).parse(argumentValue);
		assertThat(result).isSameAs(argumentValue.intern());

		String usage = withParser(new InterningStringParser()).names("-f").usage("ForwardingStringParser");
		assertThat(usage).contains("-f <interned_string>    <interned_string>: some string");
		assertThat(usage).contains("Default: foo");
	}

	private static final class InterningStringParser extends ForwardingStringParser<String>
	{
		@Override
		public String parse(String value) throws ArgumentException
		{
			return value.intern();
		}

		@Override
		public String defaultValue()
		{
			return "foo";
		}

		@Override
		public String metaDescription()
		{
			return "interned_string";
		}

		@Override
		public String descriptionOfValidValues()
		{
			return "some string";
		}

		@Override
		protected StringParser<String> delegate()
		{
			return stringParser();
		}
	}
}
