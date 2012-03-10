package se.j4j.argumentparser.handlers.internal;

import javax.annotation.Nonnull;

import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.handlers.OneParameterArgument;
import se.j4j.argumentparser.interfaces.StringConverter;

public class StringConverterWrapper<T> extends OneParameterArgument<T>
{
	private final @Nonnull StringConverter<T> converter;

	public StringConverterWrapper(final @Nonnull StringConverter<T> converter)
	{
		this.converter = converter;
	}

	@Override
	public String descriptionOfValidValues()
	{
		//Extend OneParameterArgument or StringConverterWrapper to declare this for a simple StringConverter
		return "";
	}

	@Override
	public T parse(final String value) throws ArgumentException
	{
		return converter.convert(value);
	}

}
