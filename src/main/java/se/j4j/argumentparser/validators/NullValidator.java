package se.j4j.argumentparser.validators;

import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.interfaces.ParsedValueCallback;
import se.j4j.argumentparser.interfaces.ValueValidator;

/**
 * A null object {@link ParsedValueCallback} for parsed values that doesn't do
 * anything when values have been parsed
 * 
 * @param <T> the type of the parsed value
 */
// TODO: Null may be misleading... Empty? Default? Noop?
public final class NullValidator<T> implements ValueValidator<T>
{
	@SuppressWarnings("unchecked")
	// Doesn't modify anything, i.e T is unused here
	public static final <T> ValueValidator<T> instance()
	{
		return (ValueValidator<T>) INSTANCE;
	}

	private static final ValueValidator<?> INSTANCE = new NullValidator<Object>();

	private NullValidator()
	{
	}

	@Override
	public void validate(T value) throws InvalidArgument
	{
	}
}
