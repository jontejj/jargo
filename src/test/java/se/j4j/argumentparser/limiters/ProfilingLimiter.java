package se.j4j.argumentparser.limiters;

import com.google.common.base.Predicate;

final class ProfilingLimiter<T> implements Predicate<T>
{
	int limitationsMade;

	@Override
	public boolean apply(T value)
	{
		limitationsMade++;
		return true;
	}
}
