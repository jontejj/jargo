package se.j4j.argumentparser.Validators;

import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.validators.ValueValidator;

public class ShortString implements ValueValidator<String>
{

	public void validate(final String value) throws InvalidArgument
	{
		if(value.length() > 10)
		{
			throw InvalidArgument.create(value, " is longer than 10 characters");
		}
	}

}
