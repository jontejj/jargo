package se.j4j.argumentparser.limiters;

import java.util.Map;
import java.util.Set;

import se.j4j.argumentparser.Limit;
import se.j4j.argumentparser.Limiter;

import com.google.common.collect.Sets;

public class KeyLimiter<T> implements Limiter<Map<String, T>>
{
	private final Set<String> validKeys;

	public KeyLimiter(String ... validKeys)
	{
		this.validKeys = Sets.newHashSet(validKeys);
	}

	@Override
	public Limit withinLimits(Map<String, T> keyValueMap)
	{
		if(!validKeys.containsAll(keyValueMap.keySet()))
		{
			// Find first offending key
			for(String possiblyInvalidKey : keyValueMap.keySet())
			{
				if(!validKeys.contains(possiblyInvalidKey))
					return Limit.notOk(possiblyInvalidKey + " is not a valid key. Valid keys are: " + validKeys);
			}
		}
		return Limit.OK;
	}
}
