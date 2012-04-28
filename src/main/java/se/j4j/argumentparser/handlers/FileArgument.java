package se.j4j.argumentparser.handlers;

import java.io.File;

public class FileArgument extends OneParameterArgument<File>
{
	@Override
	public File parse(final String value)
	{
		return new File(value);
	}

	@Override
	public String descriptionOfValidValues()
	{
		return "a file path";
	}

	@Override
	public File defaultValue()
	{
		return new File("");
	}

	@Override
	public String describeValue(File value)
	{
		return value.getAbsolutePath();
	}
}
