package se.j4j.argumentparser.handlers.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentHandler;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.handlers.IntegerArgument;
import se.j4j.argumentparser.validators.ValueValidator;

/**
 * Produced by {@link Argument#repeated()} and used by {@link ArgumentParser#parse(String)}
 *
 * @author Jonatan Jönsson <jontejj@gmail.com>
 *
 * @param <T> type of the repeated values (such as {@link Integer} for {@link IntegerArgument}
 */
public class RepeatedArgument<T> implements ArgumentHandler<List<T>>, RepeatableArgument<List<T>>
{
	final ArgumentHandler<T> argumentHandler;
	final ValueValidator<T> validator;

	public RepeatedArgument(final ArgumentHandler<T> argumentHandler, final ValueValidator<T> validator)
	{
		this.argumentHandler = argumentHandler;
		this.validator = validator;
	}

	public List<T> parseRepeated(final ListIterator<String> currentArgument, List<T> list, final Argument<?> argumentDefinition) throws ArgumentException
	{
		T parsedValue = argumentHandler.parse(currentArgument, argumentDefinition);
		if(validator != null)
		{
			validator.validate(parsedValue);
		}
		list = (list != null) ? list : new ArrayList<T>();
		list.add(parsedValue);
		return list;
	}

	/**
	 * TODO: find a better way and remove this method
	 */
	public List<T> parse(final ListIterator<String> currentArgument, final Argument<?> argumentDefinition)
	{
		throw new UnsupportedOperationException("use parseRepeated(...) instead");
	}
}