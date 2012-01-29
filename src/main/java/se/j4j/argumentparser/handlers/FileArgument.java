package se.j4j.argumentparser.handlers;

import java.io.File;
import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentHandler;

public class FileArgument implements ArgumentHandler<File>
{
	public File parse(final ListIterator<String> currentArgument)
	{
		return new File(currentArgument.next());
	}
}
