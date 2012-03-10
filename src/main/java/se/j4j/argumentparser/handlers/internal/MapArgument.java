package se.j4j.argumentparser.handlers.internal;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.interfaces.ArgumentHandler;
import se.j4j.argumentparser.interfaces.ValueValidator;
import se.j4j.argumentparser.utils.Strings;

public class MapArgument<T> implements ArgumentHandler<Map<String, T>>
{
	private final ArgumentHandler<T> handler;
	private final ValueValidator<T> validator;

	public MapArgument(final ArgumentHandler<T> handler, final ValueValidator<T> validator)
	{
		this.handler = handler;
		this.validator = validator;
	}

	@Override
	public Map<String, T> parse(final ListIterator<String> currentArgument, Map<String, T> map, final Argument<?> argumentDefinition) throws ArgumentException
	{
		map = (map != null) ? map : new HashMap<String, T>();

		String keyValue = currentArgument.next();
		String separator = argumentDefinition.separator();

		List<String> namesToMatch = argumentDefinition.names();
		String argument = keyValue;
		if(argumentDefinition.isIgnoringCase())
		{
			namesToMatch = Strings.toLowerCase(namesToMatch);
			argument = argument.toLowerCase();
		}

		for(String name : namesToMatch)
		{
			if(argument.startsWith(name))
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
				T oldValue = map.get(key);
				T parsedValue = handler.parse(currentArgument, oldValue, argumentDefinition);
				if(validator != null)
				{
					//TODO: test this
					validator.validate(parsedValue);
				}
				//TODO: provide overwrite protection
				Object alreadyExisting = map.put(key, parsedValue);
				if(alreadyExisting != null && !argumentDefinition.isAllowedToRepeat())
				{
					throw InvalidArgument.create(name + key, " was found as a key several times in the input.");
				}
				break;
			}
		}
		return map;
	}

	@Override
	public String descriptionOfValidValues()
	{
		return "key=value where key is an identifier and value is " + handler.descriptionOfValidValues();
	}
}
