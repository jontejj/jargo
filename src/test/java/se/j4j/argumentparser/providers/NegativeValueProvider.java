package se.j4j.argumentparser.providers;

import se.j4j.argumentparser.Provider;

public class NegativeValueProvider implements Provider<Integer>
{
	@Override
	public Integer provideValue()
	{
		return -1;
	}
}
