package se.j4j.argumentparser.defaultproviders;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import se.j4j.argumentparser.interfaces.DefaultValueProvider;

public final class ListProvider<T> implements DefaultValueProvider<List<T>>
{
	private final DefaultValueProvider<T> elementProvider;

	public ListProvider(DefaultValueProvider<T> elementProvider)
	{
		this.elementProvider = elementProvider;
	}

	@Override
	public List<T> defaultValue()
	{
		List<T> list = new ArrayList<T>(1);
		list.add(elementProvider.defaultValue());
		return unmodifiableList(list);
	}
}
