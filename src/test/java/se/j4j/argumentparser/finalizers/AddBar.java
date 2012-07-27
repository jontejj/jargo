package se.j4j.argumentparser.finalizers;

import se.j4j.argumentparser.internal.Finalizer;

public class AddBar implements Finalizer<String>
{
	@Override
	public String finalizeValue(String value)
	{
		return value + "bar";
	}

}
