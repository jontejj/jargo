package se.j4j.argumentparser.validators;

import se.j4j.argumentparser.exceptions.InvalidArgument;


public class PositiveInteger implements ValueValidator<Integer>
{
	public void validate(final Integer value) throws InvalidArgument
	{
		if(value < 0)
		{
			throw InvalidArgument.create(value, " is not a positive integer");
		}
	}

}
