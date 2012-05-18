package se.j4j.argumentparser.stringparsers;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.fileArgument;

import java.io.File;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;

public class TestFileArguments
{
	@Test
	public void testDescription()
	{
		String usage = fileArgument("-f").usage("FileArgument");
		assertThat(usage).contains("<path>: a file path");
	}

	@Test
	public void testThatFilesAreDescribedByAbsolutePath()
	{
		String usage = fileArgument("-f").usage("AbsolutePath");
		assertThat(usage).contains("Default: " + new File("").getAbsolutePath());
	}

	@Test
	public void testThatFileArgumentsDefaultsToCurrentDirectory() throws ArgumentException
	{
		File defaultFile = fileArgument("-f").parse();
		assertThat(defaultFile).isEqualTo(new File(""));
	}
}
