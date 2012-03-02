package se.j4j.argumentparser.CustomCommands;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static se.j4j.argumentparser.ArgumentFactory.fileArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.handlers.CommandArgument;

public class CommitCommand extends CommandArgument
{
	private static Argument<Boolean> amend = optionArgument("--amend").build();
	private static Argument<String> author = stringArgument("--author").required().separator("=").build();
	private static Argument<List<File>> files = fileArgument().consumeAll().build();

	static final ArgumentParser PARSER_INSTANCE = ArgumentParser.forArguments(amend, author, files);

	@Override
	public ArgumentParser getParserInstance()
	{
		return PARSER_INSTANCE;
	}

	@Override
	public void handle(final ParsedArguments parsedArguments)
	{
		assertTrue(parsedArguments.get(amend));
		String authorValue = parsedArguments.get(author);
		assertEquals("jjonsson", authorValue);

		//TODO: extract these values and make it possible to test Immutable'ness

		assertEquals(Arrays.asList(new File("A.java"), new File("B.java")), parsedArguments.get(files));
	}
}
