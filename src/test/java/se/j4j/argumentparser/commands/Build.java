package se.j4j.argumentparser.commands;

import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.CommandLineParser;
import se.j4j.argumentparser.CommandLineParsers;
import se.j4j.argumentparser.CommandLineParsers.ParsedArguments;

public class Build extends Command
{
	final BuildTarget target;

	Build(BuildTarget target)
	{
		this.target = target;
	}

	@Override
	protected CommandLineParser createParserForCommandArguments()
	{
		return CommandLineParsers.forAnyArguments();
	}

	@Override
	public String commandName()
	{
		return "build";
	}

	@Override
	protected void execute(ParsedArguments parsedArguments)
	{
		target.build();
	}

	static class BuildTarget
	{
		private boolean cleaned;
		private boolean built;

		void build()
		{
			built = true;
		}

		void clean()
		{
			cleaned = true;
		}

		boolean isClean()
		{
			return cleaned;
		}

		boolean isBuilt()
		{
			return built;
		}

		void reset()
		{
			cleaned = false;
			built = false;
		}
	}
}
