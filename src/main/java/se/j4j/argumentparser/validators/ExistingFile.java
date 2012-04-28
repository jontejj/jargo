package se.j4j.argumentparser.validators;

import java.io.File;

import javax.annotation.Nonnull;

import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.interfaces.ValueValidator;

public class ExistingFile implements ValueValidator<File>
{
	// TODO: add support for a compound ValueValidator

	@Override
	public void validate(final @Nonnull File file) throws InvalidArgument
	{
		if(!file.exists())
			throw InvalidArgument.create(file, " doesn't exist");
	}
}
