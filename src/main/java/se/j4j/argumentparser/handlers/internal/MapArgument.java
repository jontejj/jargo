package se.j4j.argumentparser.handlers.internal;

import static java.util.Collections.emptyMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.annotation.Nonnull;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.exceptions.UnhandledRepeatedArgument;
import se.j4j.argumentparser.interfaces.ArgumentHandler;
import se.j4j.argumentparser.interfaces.ParsedValueCallback;
import se.j4j.argumentparser.interfaces.ValueValidator;
import se.j4j.argumentparser.utils.Strings;

import com.google.common.base.Joiner;
import com.google.common.collect.UnmodifiableIterator;

public class MapArgument<T> implements ArgumentHandler<Map<String, T>>
{
	public static final String DEFAULT_SEPARATOR = "=";

	private final @Nonnull ArgumentHandler<T> handler;
	private final @Nonnull ValueValidator<T> validator;
	private final @Nonnull ParsedValueCallback<T> parsedValueCallback;

	public MapArgument(final @Nonnull ArgumentHandler<T> handler, final @Nonnull ValueValidator<T> validator,
			final @Nonnull ParsedValueCallback<T> callback)
	{
		this.handler = handler;
		this.validator = validator;
		this.parsedValueCallback = callback;
	}

	@Override
	public Map<String, T> parse(final ListIterator<String> currentArgument, Map<String, T> map, final Argument<?> argumentDefinition)
			throws ArgumentException
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
				// Fetch key and value from "-Dkey=value"
				int keyStartIndex = name.length();
				int keyEndIndex = keyValue.indexOf(separator, keyStartIndex);
				if(keyEndIndex == -1)
					throw InvalidArgument.create(keyValue, " Missing assignment operator(" + separator + ")");

				String key = keyValue.substring(keyStartIndex, keyEndIndex);
				// Remove "-Dkey=" from "-Dkey=value"
				String value = keyValue.substring(keyEndIndex + 1);
				// Hide what we just did to the handler that handles the "value"
				currentArgument.set(value);
				currentArgument.previous();

				T oldValue = map.get(key);
				if(oldValue != null && !argumentDefinition.isAllowedToRepeat())
					throw UnhandledRepeatedArgument.create(name + key + " was found as a key several times in the input.");

				T parsedValue = handler.parse(currentArgument, oldValue, argumentDefinition);
				validator.validate(parsedValue);
				map.put(key, parsedValue);

				parsedValueCallback.parsedValue(parsedValue);
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

	@Override
	public Map<String, T> defaultValue()
	{
		return emptyMap();
	}

	@Override
	public String describeValue(final Map<String, T> values)
	{
		List<String> keys = new ArrayList<String>(values.keySet());
		Collections.sort(keys);

		final Iterator<String> keyIterator = keys.iterator();

		return Joiner.on(", ").join(new UnmodifiableIterator<String>(){
			@Override
			public boolean hasNext()
			{
				return keyIterator.hasNext();
			}

			@Override
			public String next()
			{
				String key = keyIterator.next();
				return key + " -> " + values.get(key);
			}
		});
	}
}
