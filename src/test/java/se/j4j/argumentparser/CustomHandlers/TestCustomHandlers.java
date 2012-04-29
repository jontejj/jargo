package se.j4j.argumentparser.CustomHandlers;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.customArgument;
import static se.j4j.argumentparser.CustomHandlers.DateTimeHandler.dateArgument;

import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.builders.DefaultArgumentBuilder;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.internal.Usage;

public class TestCustomHandlers
{
	@Test
	public void testHostPort() throws ArgumentException
	{
		HostPort hostPort = new DefaultArgumentBuilder<HostPort>(new HostPortArgument()).names("-target").parse("-target", "example.com:8080");
		assertThat(hostPort.host).isEqualTo("example.com");
		assertThat(hostPort.port).isEqualTo(8080);
	}

	@Test
	public void testStringConverter() throws ArgumentException
	{
		Set<Character> letters = customArgument(new UniqueLetters()).names("-l").parse("-l", "abc");

		assertThat(letters).containsOnly('c', 'a', 'b');
	}

	@Test
	public void testDateArgument() throws ArgumentException
	{
		Argument<DateTime> startTime = dateArgument("--start").build();

		ArgumentParser parser = ArgumentParser.forArguments(startTime);
		parser.usage("").print();
		ParsedArguments parsed = parser.parse("--start", "2011-03-30");

		assertThat(parsed.get(startTime)).isEqualTo(new DateTime("2011-03-30"));

		String usage = Usage.forSingleArgument(startTime);
		assertThat(usage).startsWith("--start <date>     <date>: An ISO8601 date, such as 2011-02-28");
		assertThat(usage).contains("Default: Current time");
	}
}
