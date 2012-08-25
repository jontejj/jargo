package se.j4j.argumentparser.limiters;

import com.google.common.base.Predicate;

public class FooLimiter implements Predicate<String>
{
	public static Predicate<String> foos()
	{
		return new FooLimiter();
	}

	@Override
	public boolean apply(String value)
	{
		return "foo".equals(value);
	}

	@Override
	public String toString()
	{
		return "foo";
	}
}
