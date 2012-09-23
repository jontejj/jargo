package se.j4j.argumentparser.defaultvalues;

import com.google.common.base.Supplier;

public class ProfilingSupplier implements Supplier<Integer>
{
	int callsToGet = 0;

	@Override
	public Integer get()
	{
		callsToGet++;
		return 0;
	}

}
