package se.j4j.argumentparser.providers;

import se.j4j.argumentparser.Provider;

public class BarProvider implements Provider<String>
{
	@Override
	public String provideValue()
	{
		return "bar";
	}
}
