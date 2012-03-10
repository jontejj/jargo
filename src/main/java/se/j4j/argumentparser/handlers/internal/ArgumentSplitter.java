package se.j4j.argumentparser.handlers.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.ArgumentExceptionCodes;
import se.j4j.argumentparser.interfaces.ArgumentHandler;
import se.j4j.argumentparser.interfaces.StringSplitter;
import se.j4j.argumentparser.interfaces.ValueValidator;

public class ArgumentSplitter<T> implements ArgumentHandler<List<T>>
{
	private final @Nonnull StringSplitter splitter;
	private final @Nonnull ArgumentHandler<T> handler;
	private final @Nullable ValueValidator<T> validator;

	public ArgumentSplitter(final @Nonnull StringSplitter splitter, final @Nonnull ArgumentHandler<T> handler, final @Nullable ValueValidator<T> validator)
	{
		this.splitter = splitter;
		this.handler = handler;
		this.validator = validator;
	}

	public String descriptionOfValidValues()
	{
		//TODO: this requires some kind of description in the StringSplitter interface
		return null;
	}

	public List<T> parse(final ListIterator<String> currentArgument, final List<T> oldValue, final Argument<?> argumentDefinition) throws ArgumentException
	{
		if(!currentArgument.hasNext())
		{
			throw ArgumentException.create(ArgumentExceptionCodes.MISSING_PARAMETER);
		}
		String values = currentArgument.next();

		List<String> inputs = splitter.split(values);
		List<T> result = (oldValue == null) ? new ArrayList<T>(inputs.size()) : oldValue;

		for(String value : inputs)
		{
			//TODO: null here?
			T parsedValue = handler.parse(Arrays.asList(value).listIterator(), null, argumentDefinition);
			if(validator != null)
			{
				validator.validate(parsedValue);
			}
			result.add(parsedValue);
		}
		return result;
	}
}
