package se.j4j.argumentparser.validators;

import javax.annotation.Nonnull;

import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.interfaces.ValueValidator;


public class PositiveInteger implements ValueValidator<Integer>
{
	public void validate(final @Nonnull Integer value) throws InvalidArgument
	{
		if(value < 0)
		{
			throw InvalidArgument.create(value, " is not a positive integer");
		}
	}

}
