package se.j4j.argumentparser;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public interface ArgumentHandler<T>
{
	/**
	 * @param currentArgument an iterator where {@link Iterator#next()} points to the parameter for a named argument,
	 * 							for an indexed argument it points to the single unnamed argument
	 * @return the parsed value
	 * @throws ArgumentException if an error occurred while parsing the value
	 * @throws NoSuchElementException when an argument expects a parameter and it's not found
	 */
	T parse(ListIterator<String> currentArgument) throws ArgumentException;
}
