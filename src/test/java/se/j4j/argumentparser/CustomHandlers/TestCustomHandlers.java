package se.j4j.argumentparser.CustomHandlers;

import static junit.framework.Assert.assertEquals;
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
		String[] args = {"-target", "example.com:8080"};

		Argument<HostPort> hostPort = new DefaultArgumentBuilder<HostPort>(new HostPortArgument()).names("-target").build();

		ParsedArguments parsed = ArgumentParser.forArguments(hostPort).parse(args);

		assertEquals("example.com", parsed.get(hostPort).host);
		assertEquals(8080, parsed.get(hostPort).port);
	}

	@Test
	public void testStringConverter() throws ArgumentException
	{
		Argument<Set<Character>> letters = customArgument(new UniqueLetters()).names("-l").build();

		ParsedArguments parsed = ArgumentParser.forArguments(letters).parse("-l", "abc");

		assertThat(parsed.get(letters)).containsOnly('c', 'a', 'b');
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
