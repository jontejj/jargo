package se.j4j.argumentparser.validators;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentHandler;
import se.j4j.argumentparser.exceptions.InvalidArgument;

public interface ValueValidator<T>
{
	/**<pre>
	 * Validates a value parsed by {@link ArgumentHandler#parse(ListIterator, Argument)}.
	 *
	 * A common usage would be to only allow a subset of the parsable values by the {@link ArgumentHandler}
	 * @param value to validate
	 * @throws InvalidArgument if the value is invalid
	 */
	void validate(T value) throws InvalidArgument;

	//TODO: String validValuesDescription();
}
