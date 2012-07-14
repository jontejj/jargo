package se.j4j.argumentparser.stringparsers.custom;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.Set;

import se.j4j.argumentparser.StringParser;

public class UniqueLetters implements StringParser<Set<Character>>
{
	@Override
	public Set<Character> parse(final String argument)
	{
		Set<Character> unique = new HashSet<Character>();
		for(Character c : argument.toCharArray())
		{
			unique.add(c);
		}
		return unmodifiableSet(unique);
	}

	@Override
	public String descriptionOfValidValues()
	{
		return "any number of letters (duplicates will be filtered)";
	}

	@Override
	public Set<Character> defaultValue()
	{
		return emptySet();
	}

	@Override
	public String metaDescription()
	{
		return "letters";
	}

}
