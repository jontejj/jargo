package se.j4j.argumentparser.handlers;

import static java.math.BigInteger.ZERO;

import java.math.BigInteger;

import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;

public class BigIntegerArgument extends OneParameterArgument<BigInteger>
{
	@Override
	public String descriptionOfValidValues()
	{
		return "Any integer";
	}

	@Override
	public BigInteger parse(final String value) throws ArgumentException
	{
		try
		{
			return new BigInteger(value);
		}
		catch(NumberFormatException e)
		{
			throw InvalidArgument.create(value, " is not a valid BigInteger. " + e.getLocalizedMessage());
		}
	}

	@Override
	public BigInteger defaultValue()
	{
		return ZERO;
	}
}
