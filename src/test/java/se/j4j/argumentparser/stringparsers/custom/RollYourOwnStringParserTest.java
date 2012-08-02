package se.j4j.argumentparser.stringparsers.custom;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.withParser;
import static se.j4j.argumentparser.stringparsers.custom.DateTimeParser.dateArgument;
import static se.j4j.argumentparser.utils.UsageTexts.expected;

import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;

public class RollYourOwnStringParserTest
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
}
