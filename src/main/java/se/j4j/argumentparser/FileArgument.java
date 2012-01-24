package se.j4j.argumentparser;

import java.io.File;
import java.util.ListIterator;

public class FileArgument extends Argument<File>
{
	protected FileArgument(final String ...names)
	{
		super(names);
	}

	@Override
	File parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		return new File(currentArgument.next());
	}

}
