package se.j4j.argumentparser.finalizers;

import se.j4j.argumentparser.Finalizer;

public class AddFoo implements Finalizer<String>
{
	@Override
	public String finalizeValue(String value)
	{
		return value + "foo";
	}
}