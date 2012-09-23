package se.j4j.argumentparser.stringparsers.custom;

import static se.j4j.argumentparser.StringParsers.lowerCaseParser;

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
		super(lowerCaseParser());
		this.validKeys = ImmutableSet.copyOf(validKeys);
	}

	@Override
	public String parse(String value) throws ArgumentException
	{
		String result = super.parse(value);
		if(!validKeys.contains(result))
			throw ArgumentExceptions.withMessage("'" + value + "' didn't match any of: " + validKeys);
		return result;
	}

	@Override
	public String descriptionOfValidValues()
	{
		return "any of " + validKeys;
	}
}
