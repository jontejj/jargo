package se.j4j.argumentparser;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

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

	RepeatedArgument(final ArgumentHandler<T> argumentHandler)
	{
		this.argumentHandler = argumentHandler;
	}

	public List<T> parseRepeated(final ListIterator<String> currentArgument, final Map<ArgumentHandler<?>, Object> parsedArguments) throws ArgumentException
	{
		//TODO: before parse(...) provide a pre/post validator interface to validate values
		T parsedValue = argumentHandler.parse(currentArgument);
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
	public List<T> parse(final ListIterator<String> currentArgument)
	{
		throw new UnsupportedOperationException("use parseRepeated(...) instead");
	}
}
