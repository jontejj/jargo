package se.j4j.argumentparser.limiters;

import se.j4j.argumentparser.Limit;
import se.j4j.argumentparser.Limiter;

final class ProfilingLimiter<T> implements Limiter<T>
{
	int limitationsMade;

	@Override
	public Limit withinLimits(T value)
	{
		limitationsMade++;
		return Limit.OK;
	}

	@Override
	public String descriptionOfValidValues()
	{
		return "any value";
	}
}