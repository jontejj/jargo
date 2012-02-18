package se.j4j.argumentparser.handlers.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import se.j4j.argumentparser.ArgumentHandler;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.handlers.IntegerArgument;

/**
 * Produced by {@link Argument#repeated()} and used by {@link ArgumentParser#parse(String)}
 *
 * @author Jonatan Jönsson <jontejj@gmail.com>
 *
 * @param <T> type of the repeated values (such as {@link Integer} for {@link IntegerArgument}
 */
public class RepeatedArgument<T> implements ArgumentHandler<List<T>>
{
	final ArgumentHandler<T> argumentHandler;

	public RepeatedArgument(final ArgumentHandler<T> argumentHandler)
	{
		this.argumentHandler = argumentHandler;
	}

	public List<T> parseRepeated(final ListIterator<String> currentArgument, final Map<ArgumentHandler<?>, Object> parsedArguments, final Argument<?> argumentDefinition) throws ArgumentException
	{
		//TODO: before parse(...) provide a pre/post validator interface to validate values
		T parsedValue = argumentHandler.parse(currentArgument, argumentDefinition);
		@SuppressWarnings("unchecked") //Safety provided by ArgumentParser
		List<T> listToPutRepeatedValuesIn = (List<T>) parsedArguments.get(this);
		if(listToPutRepeatedValuesIn == null)
		{
			listToPutRepeatedValuesIn = new ArrayList<T>();
		}
		listToPutRepeatedValuesIn.add(parsedValue);
		return listToPutRepeatedValuesIn;
	}

	/**
	 * TODO: find a better way and remove this method
	 */
	public List<T> parse(final ListIterator<String> currentArgument, final Argument<?> argumentDefinition)
	{
		throw new UnsupportedOperationException("use parseRepeated(...) instead");
	}
}