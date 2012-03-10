package se.j4j.argumentparser;

import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;

import org.junit.Test;

import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;

public class TestArgumentParserUsage
{
	public static void main(final String...strings)
	{
		try
		{
			ArgumentParser.forArguments().parse("?");
		}
		catch(ArgumentException exception)
		{
			System.out.println(exception.getMessageAndUsage());
		}
	}

	@Test
	public void testUsageWithRequiredArguments()
	{
		String[] args = {"--enable-logging", "--listen-port", "8090"};

		Argument<Boolean> enableLogging = optionArgument("-l", "--enable-logging").
				description("Output debug information to standard out").build();

		Argument<Integer> port = integerArgument("-p", "--listen-port").required().
				description("The port to start the server on.").build();

		Argument<String> greetingPhrase = stringArgument().required()
				.description("A greeting phrase to greet new connections with").build();

		try
		{
			ArgumentParser.forArguments(greetingPhrase, enableLogging, port).parse(args);
		}
		catch (ArgumentException e)
		{
			//TODO: this looks funny, assert something
			System.out.println(e.getMessageAndUsage());
		}
	}
}
