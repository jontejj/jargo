package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentHandler;

public class OptionArgument implements ArgumentHandler<Boolean>
{
	final Boolean defaultValue;
	public OptionArgument(final boolean defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public Boolean parse(final ListIterator<String> currentArgument)
	{
		return !defaultValue;
	}

	/**
	 * TODO: these methods needs to be replaced in the builder with something else
	 * @deprecated an optional flag can't be required
	 */
	/*
	@Deprecated
	@Override
	public Argument<Boolean> required()
	{
		throw new UnsupportedOperationException("An optional flag can't be requried");
	}

	/**
	 * @deprecated since an optional flag can't be assigned a value so a separator is useless
	 */
	/*
	@Deprecated
	@Override
	public Argument<Boolean> separator(final String separator)
	{
		throw new UnsupportedOperationException("A seperator for an optional flag isn't supported as " +
				"an optional flag can't be assigned a value");
	}
	 */
}
