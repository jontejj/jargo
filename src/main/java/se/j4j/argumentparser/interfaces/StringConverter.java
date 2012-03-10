package se.j4j.argumentparser.interfaces;

import se.j4j.argumentparser.exceptions.ArgumentException;

public interface StringConverter<T>
{
	T convert(String argument) throws ArgumentException;
}
