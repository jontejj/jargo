package se.j4j.argumentparser.CustomCommands;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.commandArgument;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.CustomCommands.CommitCommand.Commit;
import se.j4j.argumentparser.CustomCommands.CommitCommand.Repository;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.MissingRequiredArgumentException;
import se.j4j.argumentparser.exceptions.UnexpectedArgumentException;

public class TestCustomCommands
{
	static final Argument<String> COMMIT = commandArgument(new CommitCommand(new Repository())).build();
	static final Argument<String> INIT = commandArgument(new InitCommand()).build();

	@Test(expected = MissingRequiredArgumentException.class)
	public void testInterfaceBasedCommandExecutingMissingRequiredArgument() throws ArgumentException
	{
		String[] args = {"commit", "--amend", "A.java", "B.java"}; // No author

		ArgumentParser.forArguments(COMMIT).parse(args);
	}

	@Test(expected = UnexpectedArgumentException.class)
	public void testMultipleInterfaceBasedCommandParsersWrongInput() throws ArgumentException
	{
		String[] args = {"-v", "init", "commit", "--amend", "A.java", "B.java"};

		ArgumentParser.forArguments(COMMIT, INIT).parse(args);
	}

	@Test
	public void testMultipleCommandParsers() throws ArgumentException
	{
		String[] initArgs = {"init"};
		String[] commitArgs = {"commit", "--amend", "--author=jjonsson", "A.java", "B.java"};
		CommitCommand commitCommand = new CommitCommand(new Repository());
		Argument<String> commit = commandArgument(commitCommand).build();
		Argument<String> init = commandArgument(new InitCommand()).build();
		ArgumentParser parser = ArgumentParser.forArguments(commit, init);

		ParsedArguments parsed = parser.parse(initArgs);
		assertNotNull(parsed.get(init));
		assertNull(parsed.get(commit));

		parsed = parser.parse(commitArgs);
		assertNotNull(parsed.get(commit));

		Commit c = commitCommand.repository.commits.get(0);
		assertThat(c.amend).isTrue();
		assertThat(c.author).isEqualTo("jjonsson");
		assertThat(c.files).isEqualTo(Arrays.asList(new File("A.java"), new File("B.java")));

		assertNull(parsed.get(init));
	}

	@Test
	public void testThatRequiredArgumentsAreResetBetweenParsings() throws ArgumentException
	{
		String[] invalidArgs = {"commit", "--amend", "A.java", "B.java"};
		String[] validArgs = {"commit", "--amend", "--author=jjonsson", "A.java", "B.java"};

		ArgumentParser parser = ArgumentParser.forArguments(COMMIT);
		// First make a successful parsing
		parser.parse(validArgs);
		for(int i = 0; i < 2; i++)
		{
			try
			{
				parser.parse(invalidArgs);
				fail("--author=??? wasn't given in the input and it should have been required");
			}
			catch(MissingRequiredArgumentException expected)
			{
			}
		}
	}

	@Test
	public void testThatRepeatedParsingsWithACommandParserWorks() throws ArgumentException
	{
		String[] args = {"commit", "--amend", "--author=jjonsson", "A.java", "B.java"};

		Repository repo = new Repository();
		Argument<String> commitCommand = commandArgument(new CommitCommand(repo)).build();

		ArgumentParser parser = ArgumentParser.forArguments(commitCommand);

		for(int i = 0; i < 2; i++)
		{
			parser.parse(args);
			Commit commit = repo.commits.get(i);
			assertThat(commit.amend).isTrue();
			assertThat(commit.author).isEqualTo("jjonsson");
			assertThat(commit.files).isEqualTo(Arrays.asList(new File("A.java"), new File("B.java")));
		}
	}
}
