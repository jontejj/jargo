package se.j4j.argumentparser;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Produced by {@link Argument#repeated()} and used by {@link ArgumentParser#parse(String)}
 *
 * @author Jonatan Jönsson <jontejj@gmail.com>
 *
 * @param <T> type of the repeated values
 */
public class RepeatedArgument<T> extends Argument<List<T>>
{
	Argument<T> argumentHandler;

	RepeatedArgument(final Argument<T> argumentHandler)
	{
		super(argumentHandler.names());
		description(argumentHandler.description());
		this.argumentHandler = argumentHandler;
	}

	/**
	 * This method should be called before repeated()
	 */
	@Override
	@Deprecated
	public ListArgument<List<T>> arity(final int numberOfParameters)
	{
		throw new UnsupportedOperationException("Programmer Error. Call arity(...) before repeated()");
	}

	/**
	 * This method should be called before repeated()
	 */
	@Override
	@Deprecated
	public ListArgument<List<T>> consumeAll()
	{
		throw new UnsupportedOperationException("Programmer Error. Call consumeAll(...) before repeated()");
	}

	List<T> parseRepeated(final ListIterator<String> currentArgument, final Map<Argument<?>, Object> parsedArguments) throws ArgumentException
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

	@Override
	List<T> parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		throw new UnsupportedOperationException("use parseRepeated(...) instead");
	}
}
