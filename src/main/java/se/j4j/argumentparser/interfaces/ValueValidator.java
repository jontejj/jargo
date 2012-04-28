package se.j4j.argumentparser.interfaces;

import java.util.ListIterator;

import javax.annotation.Nullable;

import se.j4j.argumentparser.exceptions.InvalidArgument;

public interface ValueValidator<T>
{
	/**
	 * <pre>
	 * Validates a value parsed by {@link ArgumentHandler#parse(ListIterator)}.
	 * 
	 * A common usage would be to only allow a subset of the parsable values by a {@link ArgumentHandler}
	 * @param value to validate
	 * @throws InvalidArgument if the value is invalid
	 */
	void validate(@Nullable T value) throws InvalidArgument;

	// TODO: String validValuesDescription();
}
