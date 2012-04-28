package se.j4j.argumentparser.handlers.internal;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nonnull;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.handlers.IntegerArgument;
import se.j4j.argumentparser.interfaces.ArgumentHandler;
import se.j4j.argumentparser.utils.ListUtil;

/**
 * Produced by {@link Argument#repeated()} and used by
 * {@link ArgumentParser#parse(String)}
 * 
 * @param <T> type of the repeated values (such as {@link Integer} for
 *            {@link IntegerArgument}
 */
public class RepeatedArgument<T> implements ArgumentHandler<List<T>>
{
	final ArgumentHandler<T> argumentHandler;

	public RepeatedArgument(final @Nonnull ArgumentHandler<T> argumentHandler)
	{
		this.argumentHandler = argumentHandler;
	}

	@Override
	public List<T> parse(final ListIterator<String> currentArgument, List<T> list, final Argument<?> argumentDefinition) throws ArgumentException
	{
		T parsedValue = argumentHandler.parse(currentArgument, null, argumentDefinition);
		list = (list != null) ? list : new ArrayList<T>();
		list.add(parsedValue);
		return list;
	}

	@Override
	public String descriptionOfValidValues()
	{
		return argumentHandler.descriptionOfValidValues();
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
