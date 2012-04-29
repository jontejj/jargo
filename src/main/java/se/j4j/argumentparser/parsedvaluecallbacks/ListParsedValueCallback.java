package se.j4j.argumentparser.parsedvaluecallbacks;

import java.util.List;

import se.j4j.argumentparser.interfaces.ParsedValueCallback;

public final class ListParsedValueCallback<T> implements ParsedValueCallback<List<T>>
{
	private final ParsedValueCallback<T> elementCallback;

	private ListParsedValueCallback(ParsedValueCallback<T> elementCallback)
	{
		this.elementCallback = elementCallback;
	}

	public static <T> ParsedValueCallback<List<T>> create(ParsedValueCallback<T> elementCallback)
	{
		if(elementCallback == NoCallback.instance())
			return NoCallback.instance();
		return new ListParsedValueCallback<T>(elementCallback);

	}

	@Override
	public void parsedValue(List<T> parsedValues)
	{
		for(T value : parsedValues)
		{
			elementCallback.parsedValue(value);
		}
	}
}
