package se.j4j.argumentparser.providers;

import se.j4j.argumentparser.Provider;

public class ChangingProvider implements Provider<Integer>
{
	private int valueToProvide = 0;

	@Override
	public Integer provideValue()
	{
		return valueToProvide++;
	}
}
