package se.j4j.argumentparser;

/**
 * Understands how to only create values (lazily) when they're needed.
 */
public interface DefaultValueProvider<T>
{
	T defaultValue();
}
