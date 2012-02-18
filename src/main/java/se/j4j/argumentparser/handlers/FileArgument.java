package se.j4j.argumentparser.handlers;

import java.io.File;

public class FileArgument extends OneParameterArgument<File>
{
	@Override
	public File parse(final String value)
	{
		return new File(value);
	}
}
