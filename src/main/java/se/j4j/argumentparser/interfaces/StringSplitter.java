package se.j4j.argumentparser.interfaces;

import java.util.List;

import javax.annotation.Nonnull;

public interface StringSplitter
{
	@Nonnull
	List<String> split(@Nonnull String input);
}
