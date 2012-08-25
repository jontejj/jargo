package se.j4j.argumentparser.limiters;

import com.google.common.base.Predicate;

public class ShortString implements Predicate<String>
{
	@Override
	public boolean apply(final String value)
	{
		return value.length() < 10;
	}

	@Override
	public String toString()
	{
		return "a string of max 10 characters";
	}
}
