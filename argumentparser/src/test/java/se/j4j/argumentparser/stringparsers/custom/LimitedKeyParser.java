package se.j4j.argumentparser.stringparsers.custom;

import static se.j4j.argumentparser.StringParsers.stringParser;

import java.util.Locale;
import java.util.Set;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentExceptions;
import se.j4j.argumentparser.ForwardingStringParser.SimpleForwardingStringParser;

import com.google.common.collect.ImmutableSet;

public class LimitedKeyParser extends SimpleForwardingStringParser<String>
{
	private final Set<String> validKeys;

	public LimitedKeyParser(String ... validKeys)
	{
		super(stringParser());
		this.validKeys = ImmutableSet.copyOf(validKeys);
	}

	@Override
	public String parse(String value, Locale locale) throws ArgumentException
	{
		String result = super.parse(value, locale);
		if(!validKeys.contains(result))
			throw ArgumentExceptions.withMessage("'" + value + "' didn't match any of: " + validKeys);
		return result;
	}

	@Override
	public String descriptionOfValidValues(Locale locale)
	{
		return "any of " + validKeys;
	}
}
