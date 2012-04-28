package se.j4j.argumentparser.handlers.internal;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nonnull;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.ArgumentExceptionCodes;
import se.j4j.argumentparser.interfaces.ArgumentHandler;
import se.j4j.argumentparser.interfaces.StringSplitter;
import se.j4j.argumentparser.utils.ListUtil;

public class ArgumentSplitter<T> implements ArgumentHandler<List<T>>
{
	private final @Nonnull StringSplitter splitter;
	private final @Nonnull ArgumentHandler<T> handler;

	public ArgumentSplitter(final @Nonnull StringSplitter splitter, final @Nonnull ArgumentHandler<T> handler)
	{
		this.splitter = splitter;
		this.handler = handler;
	}

	@Override
	public String descriptionOfValidValues()
	{
		// TODO: this requires some kind of description in the StringSplitter
		// interface
		return "";
	}

	@Override
	public List<T> parse(final ListIterator<String> currentArgument, final List<T> oldValue, final Argument<?> argumentDefinition)
			throws ArgumentException
	{
		if(!currentArgument.hasNext())
			throw ArgumentException.create(ArgumentExceptionCodes.MISSING_PARAMETER);

		String values = currentArgument.next();

		List<String> inputs = splitter.split(values);
		List<T> result = new ArrayList<T>(inputs.size());

		for(String value : inputs)
		{
			T parsedValue = handler.parse(Arrays.asList(value).listIterator(), null, argumentDefinition);
			result.add(parsedValue);
		}
		return result;
	}

	@Override
	public List<T> defaultValue()
	{
		return emptyList();
	}

	@Override
	public String describeValue(List<T> value)
	{
		return ListUtil.describeList(value);
	}
}
