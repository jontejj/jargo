package se.j4j.argumentparser.stringparsers.custom;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.withParser;
import static se.j4j.argumentparser.StringParsers.stringParser;

import java.util.Locale;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ForwardingStringParser;
import se.j4j.argumentparser.StringParser;
import se.j4j.argumentparser.StringParsers;

/**
 * Tests for {@link ForwardingStringParser}
 */
public class ForwardingStringParserTest
{
	@Test
	public void testOverridingEachMethodForStringParser() throws ArgumentException
	{
		String argumentValue = "bar";
		String result = withParser(new InterningStringParser()).parse(argumentValue);
		assertThat(result).isSameAs(argumentValue.intern());

		String usage = withParser(new InterningStringParser()).names("-f").usage();
		assertThat(usage).contains("-f <interned_string>    <interned_string>: some string");
		assertThat(usage).contains("Default: foo");
	}

	@Test
	public void testThatForwaringStringParserForwardsToDelegate() throws ArgumentException
	{
		StringParser<Integer> delegatedParser = new ForwardingStringParser<Integer>(){
			@Override
			protected StringParser<Integer> delegate()
			{
				return StringParsers.integerParser();
			}
		};

		StringParser<Integer> regularParser = StringParsers.integerParser();
		Locale locale = Locale.ENGLISH;

		assertThat(delegatedParser.parse("1", locale)).isEqualTo(regularParser.parse("1", locale));
		assertThat(delegatedParser.descriptionOfValidValues(locale)).isEqualTo(regularParser.descriptionOfValidValues(locale));
		assertThat(delegatedParser.defaultValue()).isEqualTo(regularParser.defaultValue());
		assertThat(delegatedParser.metaDescription()).isEqualTo(regularParser.metaDescription());
		assertThat(delegatedParser.toString()).isEqualTo(regularParser.toString());
	}

	private static final class InterningStringParser extends ForwardingStringParser<String>
	{
		@Override
		public String parse(String value, Locale locale) throws ArgumentException
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
			return "<interned_string>";
		}

		@Override
		public String descriptionOfValidValues(Locale locale)
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
