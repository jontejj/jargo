package se.j4j.argumentparser;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

public final class DefaultValueProviders
{
	private DefaultValueProviders()
	{
	}

	public static <T> DefaultValueProvider<T> nonLazyProvider(T valueToProvide)
	{
		return new NonLazyValueProvider<T>(valueToProvide);
	}

	public static <T> DefaultValueProvider<List<T>> listWithOneValue(DefaultValueProvider<T> elementProvider)
	{
		return new ListProvider<T>(elementProvider);
	}

	static final class NonLazyValueProvider<T> implements DefaultValueProvider<T>
	{
		private final T valueToProvide;

		private NonLazyValueProvider(final T valueToProvide)
		{
			this.valueToProvide = valueToProvide;
		}

		@Override
		public T defaultValue()
		{
			return valueToProvide;
		}

		@Override
		public String toString()
		{
			return String.valueOf(valueToProvide);
		}
	}

	private static final class ListProvider<T> implements DefaultValueProvider<List<T>>
	{
		private final DefaultValueProvider<T> elementProvider;

		private ListProvider(DefaultValueProvider<T> elementProvider)
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
}
