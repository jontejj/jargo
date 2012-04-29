package se.j4j.argumentparser.stringsplitters;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import se.j4j.argumentparser.interfaces.StringSplitter;

public class Comma implements StringSplitter
{
	@Override
	@Nonnull
	public List<String> split(final @Nonnull String input)
	{
		return Arrays.asList(input.split(","));
	}

}
