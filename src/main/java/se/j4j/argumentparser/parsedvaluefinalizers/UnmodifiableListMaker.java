package se.j4j.argumentparser.parsedvaluefinalizers;

import static java.util.Collections.unmodifiableList;

import java.util.List;

import se.j4j.argumentparser.interfaces.ParsedValueFinalizer;

public final class UnmodifiableListMaker<T> implements ParsedValueFinalizer<List<T>>
{
	public static <T> UnmodifiableListMaker<T> create()
	{
		return new UnmodifiableListMaker<T>();
	}

	@Override
	public List<T> finalizeValue(List<T> value)
	{
		return unmodifiableList(value);
	}
}
