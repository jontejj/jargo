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
	static final Argument<String> COMMIT = commandArgument(new CommitCommand()).build();
	static final Argument<String> INIT = commandArgument(new InitCommand()).build();

	@Test(expected = MissingRequiredArgumentException.class)
	public void testInterfaceBasedCommandExecutingMissingRequiredArgument() throws ArgumentException
	{
		String[] args = {"commit", "--amend", "A.java", "B.java"}; //No author

		ArgumentParser.forArguments(COMMIT).parse(args);
	}

	@Test(expected = UnexpectedArgumentException.class)
	public void testMultipleInterfaceBasedCommandParsersWrongInput() throws ArgumentException
	{
		String[] args = {"-v", "init", "commit", "--amend", "A.java", "B.java"};

		ArgumentParser.forArguments(COMMIT, INIT).parse(args);
	}

	@Test
	public void testMultipleInterfaceBasedCommandParsers() throws ArgumentException
	{
		String[] initArgs = {"init"};
		String[] commitArgs = {"commit", "--amend", "--author=jjonsson", "A.java", "B.java"};

		ArgumentParser parser = ArgumentParser.forArguments(COMMIT, INIT);

		ParsedArguments parsed = parser.parse(initArgs);
		assertNotNull(parsed.get(INIT));
		assertNull(parsed.get(COMMIT));

		parsed = parser.parse(commitArgs);
		assertNotNull(parsed.get(COMMIT));
		assertNull(parsed.get(INIT));
	}

	@Test
	public void testInterfaceBasedCommandExecutingFailed() throws ArgumentException
	{
		String[] args = {"commit", "--amend", "A.java", "B.java"};

		for(int i = 0; i < 2; i++)
		{
			try
			{
				ArgumentParser.forArguments(COMMIT).parse(args);
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
			ArgumentParser.forArguments(COMMIT).parse(args);
		}
	}
}
