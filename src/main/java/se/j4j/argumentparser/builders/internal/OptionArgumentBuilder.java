package se.j4j.argumentparser.builders.internal;

import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.builders.ArgumentBuilder;
import se.j4j.argumentparser.handlers.OptionArgument;

public class OptionArgumentBuilder extends ArgumentBuilder<OptionArgumentBuilder, Boolean>
{
	public OptionArgumentBuilder()
	{
		super(null);
		defaultValue(Boolean.FALSE);
	}

	@Override
	public Argument<Boolean> build()
	{
		handler(new OptionArgument(defaultValue));
		return super.build();
	}
	/**
	 * @deprecated an optional flag can't be required
	 */
	@Deprecated
	@Override
	public OptionArgumentBuilder required()
	{
		throw new IllegalStateException("An optional flag can't be requried");
	}

	/**
	 * @deprecated a separator is useless since an optional flag can't be assigned a value
	 */
	@Deprecated
	@Override
	public OptionArgumentBuilder separator(final String separator)
	{
		throw new IllegalStateException("A seperator for an optional flag isn't supported as " +
				"an optional flag can't be assigned a value");
	}
}