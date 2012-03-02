package se.j4j.argumentparser.CustomHandlers;

import static junit.framework.Assert.assertEquals;

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
}
