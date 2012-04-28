package se.j4j.argumentparser.defaultproviders;

import se.j4j.argumentparser.interfaces.DefaultValueProvider;

public class NegativeValueProvider implements DefaultValueProvider<Integer>
{
	@Override
	public Integer defaultValue()
	{
		return -1;
	}
}
