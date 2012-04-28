package se.j4j.argumentparser.parsedvaluecallbacks;

import se.j4j.argumentparser.interfaces.ParsedValueCallback;

/**
 * A null object {@link ParsedValueCallback} for parsed values that doesn't do
 * anything when values have been parsed
 * 
 * @param <T> the type of the parsed value
 */
public final class NullCallback<T> implements ParsedValueCallback<T>
{
	@SuppressWarnings("unchecked")
	// Doesn't modify anything, i.e T is unused here
	public static final <T> ParsedValueCallback<T> instance()
	{
		return (ParsedValueCallback<T>) INSTANCE;
	}

	private static final ParsedValueCallback<?> INSTANCE = new NullCallback<Object>();

	private NullCallback()
	{
	}

	@Override
	public void parsedValue(T parsedValue)
	{
	}
}
