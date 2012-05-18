package se.j4j.argumentparser.providers;

import se.j4j.argumentparser.Provider;

public class NullProvider implements Provider<Integer>
{
	int numberOfCalls = 0;

	@Override
	public Integer provideValue()
	{
		numberOfCalls++;
		return null;
	}
}
