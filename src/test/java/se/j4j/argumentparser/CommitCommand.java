package se.j4j.argumentparser;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static se.j4j.argumentparser.ArgumentFactory.commandArgument;
import static se.j4j.argumentparser.ArgumentFactory.fileArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;

public class CommitCommand implements CommandExecutor
{
	private static Argument<Boolean> amend = optionArgument("--amend").build();
	private static Argument<String> author = stringArgument("--author").required().separator("=").build();
	private static Argument<List<File>> files = fileArgument().consumeAll().build();

	private final Argument<String> myCommand;

	public CommitCommand()
	{
		myCommand = commandArgument("commit").setCommandExecutor(this).withArguments(amend, author, files).build();
	}

	public Argument<String> getCommand()
	{
		return myCommand;
	}

	private boolean	executued;
	private boolean failed;
	private ArgumentException exception;

	public void execute(final ParsedArguments arguments)
	{
		assertTrue(arguments.get(amend));
		String authorValue = arguments.get(author);
		assertEquals("jjonsson", authorValue);

		assertEquals(Arrays.asList(new File("A.java"), new File("B.java")), arguments.get(files));
		executued = true;
	}

	public boolean didExecute()
	{
		return executued;
	}

	public void failed(final ArgumentException ex) throws ArgumentException
	{
		failed = true;
		exception = ex;
	}

	public boolean didFail()
	{
		return failed;
	}

	public ArgumentException getExceptionThatCausedTheFailure()
	{
		return exception;
	}
}
