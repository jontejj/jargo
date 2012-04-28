package se.j4j.argumentparser.handlers;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.interfaces.ArgumentHandler;

public class EnumArgument<T extends Enum<T>> extends OneParameterArgument<T> implements ArgumentHandler<T>
{
	private final Class<T> enumType;

	public EnumArgument(final Class<T> enumToHandle)
	{
		enumType = enumToHandle;
	}

	@Override
	public T parse(final String value) throws ArgumentException
	{
		try
		{
			return Enum.valueOf(enumType, value);
		}
		catch(IllegalArgumentException noEnumFound)
		{
			List<T> validValues = Arrays.asList(enumType.getEnumConstants());
			throw InvalidArgument.create(value, " is not a valid Option, Expecting one of " + validValues);
		}
	}

	@Override
	public String descriptionOfValidValues()
	{
		return EnumSet.allOf(enumType).toString();
	}

	@Override
	public T defaultValue()
	{
		return null;
	}

}
