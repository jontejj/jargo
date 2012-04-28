package se.j4j.argumentparser.Validators;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.interfaces.ValueValidator;

public class KeyValidator<T> implements ValueValidator<Map<String, T>>
{
	private final Set<String> validKeys;

	public KeyValidator(String ... validKeys)
	{
		this.validKeys = new HashSet<String>(Arrays.asList(validKeys));
	}

	@Override
	public void validate(Map<String, T> keyValueMap) throws InvalidArgument
	{
		if(!validKeys.containsAll(keyValueMap.keySet()))
		{
			// Find first offending key
			for(String possiblyInvalidKey : keyValueMap.keySet())
			{
				if(!validKeys.contains(possiblyInvalidKey))
					throw InvalidArgument.create(possiblyInvalidKey, " is not a valid key. Valid keys are: " + validKeys);
			}
		}
	}

}
