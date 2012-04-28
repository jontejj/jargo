package se.j4j.argumentparser.parsedvaluefinalizers;

import static java.util.Collections.unmodifiableMap;

import java.util.Map;

import se.j4j.argumentparser.interfaces.ParsedValueFinalizer;

public final class UnmodifiableMapMaker<T> implements ParsedValueFinalizer<Map<String, T>>
{
	public static <T> UnmodifiableMapMaker<T> create()
	{
		return new UnmodifiableMapMaker<T>();
	}

	@Override
	public Map<String, T> finalizeValue(Map<String, T> value)
	{
		return unmodifiableMap(value);
	}
}
