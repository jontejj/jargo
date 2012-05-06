package se.j4j.argumentparser.valueproviders;

import se.j4j.argumentparser.DefaultValueProvider;

public class NegativeValueProvider implements DefaultValueProvider<Integer>
{
	@Override
	public Integer defaultValue()
	{
		return -1;
	}
}
