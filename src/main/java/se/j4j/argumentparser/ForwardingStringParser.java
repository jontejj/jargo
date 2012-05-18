package se.j4j.argumentparser;

import javax.annotation.Nonnull;

import se.j4j.argumentparser.StringParsers.OneParameterParser;

/**
 * A <a href="http://en.wikipedia.org/wiki/Decorator_pattern">Decorator</a>
 * class. It allows you to subclass it and override individual methods
 * that you want to customize for an existing {@link StringParser}.
 * 
 * @param <T> the type the decorated {@link StringParser} handles
 */
public class ForwardingStringParser<T> extends OneParameterParser<T>
{
	@Nonnull private final StringParser<T> parser;

	protected ForwardingStringParser(@Nonnull final StringParser<T> parser)
	{
		this.parser = parser;
	}

	@Override
	public String descriptionOfValidValues()
	{
		return parser.descriptionOfValidValues();
	}

	@Override
	public T parse(final String value) throws ArgumentException
	{
		return parser.parse(value);
	}

	@Override
	public T defaultValue()
	{
		return parser.defaultValue();
	}
}