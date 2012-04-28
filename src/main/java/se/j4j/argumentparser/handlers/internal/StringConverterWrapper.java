package se.j4j.argumentparser.handlers.internal;

import javax.annotation.Nonnull;

import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.handlers.OneParameterArgument;
import se.j4j.argumentparser.interfaces.ArgumentHandler;
import se.j4j.argumentparser.interfaces.StringConverter;

/**
 * A wrapper for {@link StringConverter}s. I.e it works like a bridge between
 * the {@link StringConverter} and {@link ArgumentHandler} interfaces.
 * 
 * @param <T>
 */
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
		return converter.descriptionOfValidValues();
	}

	@Override
	public T parse(final String value) throws ArgumentException
	{
		return converter.convert(value);
	}

	@Override
	public T defaultValue()
	{
		return converter.defaultValue();
	}

}
