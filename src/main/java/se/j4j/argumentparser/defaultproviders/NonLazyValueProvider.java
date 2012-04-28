package se.j4j.argumentparser.defaultproviders;

import se.j4j.argumentparser.interfaces.DefaultValueProvider;

public final class NonLazyValueProvider<T> implements DefaultValueProvider<T>
{
	private final T valueToProvide;

	public NonLazyValueProvider(final T valueToProvide)
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
