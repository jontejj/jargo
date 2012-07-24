package se.j4j.argumentparser.defaultvalues;

import com.google.common.base.Supplier;

public class ChangingSupplier implements Supplier<Integer>
{
	private int valueToProvide = 0;

	@Override
	public Integer get()
	{
		return valueToProvide++;
	}
}
