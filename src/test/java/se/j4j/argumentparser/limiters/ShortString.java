package se.j4j.argumentparser.limiters;

import se.j4j.argumentparser.Limit;
import se.j4j.argumentparser.Limiter;

public class ShortString implements Limiter<String>
{
	@Override
	public Limit withinLimits(final String value)
	{
		if(value.length() < 10)
			return Limit.OK;
		return Limit.notOk(value + " is longer than 10 characters");
	}

	@Override
	public String descriptionOfValidValues()
	{
		return "a string of max 10 characters";
	}
}
