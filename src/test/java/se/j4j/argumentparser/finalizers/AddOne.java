package se.j4j.argumentparser.finalizers;

import se.j4j.argumentparser.Finalizer;

public class AddOne implements Finalizer<Integer>
{
	@Override
	public Integer finalizeValue(Integer value)
	{
		return value + 1;
	}

}
