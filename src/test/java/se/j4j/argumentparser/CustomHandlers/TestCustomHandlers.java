package se.j4j.argumentparser.CustomHandlers;

import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.customArgument;

import java.util.Set;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.builders.DefaultArgumentBuilder;
import se.j4j.argumentparser.exceptions.ArgumentException;

public class TestCustomHandlers
{

	@Test
	public void testHostPort() throws ArgumentException
	{
		String[] args = {"-target" , "example.com:8080"};

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

		assertThat(parsed.get(letters)).containsOnly('a', 'b', 'c');
	}
}
