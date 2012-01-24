package se.j4j.argumentparser;

import java.util.ListIterator;

public class OptionArgument extends Argument<Boolean>
{
	protected OptionArgument(final String ...names)
	{
		super(names);
		defaultValue(Boolean.FALSE);
	}

	@Override
	Boolean parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		return !defaultValue();
	}

	/**
	 * @deprecated an optional flag can't be required
	 */
	@Deprecated
	@Override
	public Argument<Boolean> required()
	{
		throw new UnsupportedOperationException("An optional flag can't be requried");
	}

	/**
	 * @deprecated since an optional flag can't be assigned a value so a separator is useless
	 */
	@Deprecated
	@Override
	public Argument<Boolean> separator(final String separator)
	{
		throw new UnsupportedOperationException("A seperator for an optional flag isn't supported as " +
				"an optional flag can't be assigned a value");
	}
}
