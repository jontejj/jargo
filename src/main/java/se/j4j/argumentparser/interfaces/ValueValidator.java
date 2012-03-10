package se.j4j.argumentparser.interfaces;

import java.util.ListIterator;

import se.j4j.argumentparser.exceptions.InvalidArgument;

public interface ValueValidator<T>
{
	//TODO: should it be possible to limit to the inverse of a ValueValidator? I.e. PositiveInteger.inverse
	/**<pre>
	 * Validates a value parsed by {@link ArgumentHandler#parse(ListIterator)}.
	 *
	 * A common usage would be to only allow a subset of the parsable values by the {@link ArgumentHandler}
	 * @param value to validate
	 * @throws InvalidArgument if the value is invalid
	 */
	void validate(T value) throws InvalidArgument;

	//TODO: String validValuesDescription();
}
