package se.j4j.argumentparser.limiters;

import se.j4j.argumentparser.Limit;
import se.j4j.argumentparser.Limiter;

public class FooLimiter implements Limiter<String>
{
	public static Limiter<String> foos()
	{
		return new FooLimiter();
	}

	@Override
	public Limit withinLimits(String value)
	{
		return "foo".equals(value) ? Limit.OK : Limit.notOk(value + " is not foo");
	}

	@Override
	public String validValuesDescription()
	{
		return "foo";
	}
}
