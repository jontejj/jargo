package se.j4j.argumentparser.CustomCommands;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static se.j4j.argumentparser.ArgumentFactory.commandArgument;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.MissingRequiredArgumentException;
import se.j4j.argumentparser.exceptions.UnexpectedArgumentException;

public class TestCustomCommands
{

	@Test(expected = MissingRequiredArgumentException.class)
	public void testInterfaceBasedCommandExecutingMissingRequiredArgument() throws ArgumentException
	{
		String[] args = {"commit", "--amend", "A.java", "B.java"}; //No author

		Argument<String> commitCommand = commandArgument(new CommitCommand()).names("commit").build();

		ArgumentParser.forArguments(commitCommand).parse(args);
	}

	@Test(expected = UnexpectedArgumentException.class)
	public void testMultipleInterfaceBasedCommandParsersWrongInput() throws ArgumentException
	{
		String[] args = {"-v", "init", "commit", "--amend", "A.java", "B.java"};

		Argument<String> commitCommand = commandArgument(new CommitCommand()).names("commit").build();
		Argument<String> initCommand = commandArgument(new InitCommand()).names("init").build();

		ArgumentParser.forArguments(commitCommand, initCommand).parse(args);
	}

	@Test
	public void testMultipleInterfaceBasedCommandParsers() throws ArgumentException
	{
		String[] initArgs = {"init"};
		String[] commitArgs = {"commit", "--amend", "--author=jjonsson", "A.java", "B.java"};

		Argument<String> commitCommand = commandArgument(new CommitCommand()).names("commit").build();
		Argument<String> initCommand = commandArgument(new InitCommand()).names("init").build();

		ArgumentParser parser = ArgumentParser.forArguments(commitCommand, initCommand);

		ParsedArguments parsed = parser.parse(initArgs);
		assertNotNull(parsed.get(initCommand));
		assertNull(parsed.get(commitCommand));

		parsed = parser.parse(commitArgs);
		assertNotNull(parsed.get(commitCommand));
		assertNull(parsed.get(initCommand));
	}

	@Test
	public void testInterfaceBasedCommandExecutingFailed() throws ArgumentException
	{
		String[] args = {"commit", "--amend", "A.java", "B.java"};

		for(int i = 0; i < 2; i++)
		{
			Argument<String> commitCommand = commandArgument(new CommitCommand()).names("commit").build();
			try
			{
				ArgumentParser.forArguments(commitCommand).parse(args);
				fail("--author=??? wasn't given in the input and it should have been required");
			}
			catch (MissingRequiredArgumentException expected)
			{
			}
		}
	}

	@Test
	public void testInterfaceBasedCommandExecuting() throws ArgumentException
	{
		String[] args = {"commit", "--amend", "--author=jjonsson", "A.java", "B.java"};

		for(int i = 0; i < 2; i++)
		{
			Argument<String> commitCommand = commandArgument(new CommitCommand()).names("commit").build();
			ArgumentParser.forArguments(commitCommand).parse(args);
		}
	}
}
