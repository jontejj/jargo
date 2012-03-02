package se.j4j.argumentparser.handlers.internal;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import se.j4j.argumentparser.ArgumentHandler;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.validators.ValueValidator;

public class MapArgument<T> implements ArgumentHandler<Map<String, T>>, RepeatableArgument<Map<String, T>>
{
	private final ArgumentHandler<T> handler;
	private final ValueValidator<T> validator;

	public MapArgument(final ArgumentHandler<T> handler, final ValueValidator<T> validator)
	{
		this.handler = handler;
		this.validator = validator;
	}

	public Map<String, T> parse(final ListIterator<String> currentArgument, final Argument<?> argumentDefinition) throws ArgumentException
	{
		throw new UnsupportedOperationException("use parseRepeated(...) instead");
	}

	public Map<String, T> parseRepeated(final ListIterator<String> currentArgument, Map<String, T> map, final Argument<?> argumentDefinition) throws ArgumentException
	{
		map = (map != null) ? map : new HashMap<String, T>();

		String keyValue = currentArgument.next();
		String separator = argumentDefinition.separator();
		for(String name : argumentDefinition.names())
		{
			if(keyValue.startsWith(name))
			{
				//Fetch key and value from "-Dkey=value"
				int keyStartIndex = name.length();
				int keyEndIndex = keyValue.indexOf(separator, keyStartIndex);
				if(keyEndIndex == -1)
				{
					throw InvalidArgument.create(keyValue, " Missing assignment operator(" + separator + ")");
				}
				String key = keyValue.substring(keyStartIndex, keyEndIndex);
				//Remove "-Dkey=" from "-Dkey=value"
				String value = keyValue.substring(keyEndIndex + 1);
				//Hide what we just did to the handler that handles the "value"
				currentArgument.set(value);
				currentArgument.previous();
				if(handler instanceof RepeatableArgument)
				{
					T oldValue = map.get(key);
					T parsedValue = ((RepeatableArgument<T>) handler).parseRepeated(currentArgument, oldValue, argumentDefinition);
					map.put(key, parsedValue);
				}
				else
				{
					T parsedValue = handler.parse(currentArgument, argumentDefinition);
					if(validator != null)
					{
						validator.validate(parsedValue);
					}
					if(map.put(key, parsedValue) != null)
					{
						//TODO: better error output, maybe provide an overwrite option (as default?)?
						throw InvalidArgument.create(parsedValue, " duplicate values for the key: " + key);
						//throw UnhandledRepeatedArgument.create(argumentDefinition);
					}
				}
				break;
			}
		}
		return map;
	}
}
