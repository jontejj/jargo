package se.j4j.argumentparser;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;

public interface CommandExecutor
{
	void execute(ParsedArguments arguments);

	void failed(ArgumentException ex) throws ArgumentException;
}
