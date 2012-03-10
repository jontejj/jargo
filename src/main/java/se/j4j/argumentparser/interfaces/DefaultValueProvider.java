package se.j4j.argumentparser.interfaces;

/**
 * Understands how to only create default values (lazily) when they're needed
 */
public interface DefaultValueProvider<T>
{
	T defaultValue();
}

//TODO: use this in Argument
