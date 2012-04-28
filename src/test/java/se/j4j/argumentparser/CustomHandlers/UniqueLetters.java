package se.j4j.argumentparser.CustomHandlers;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.Set;

import se.j4j.argumentparser.interfaces.StringConverter;

public class UniqueLetters implements StringConverter<Set<Character>>
{
	@Override
	public Set<Character> convert(final String argument)
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
		return "Any number of letters";
	}

	@Override
	public Set<Character> defaultValue()
	{
		return emptySet();
	}

}
