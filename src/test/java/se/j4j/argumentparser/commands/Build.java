package se.j4j.argumentparser.commands;

import static org.fest.assertions.Assertions.assertThat;
import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;

public class Build extends Command
{
	final BuildTarget target;

	Build(BuildTarget target)
	{
		this.target = target;
	}

	public Build()
	{
		this.target = new BuildTarget();
	}

	@Override
	public String commandName()
	{
		return "build";
	}

	@Override
	public String description()
	{
		return "Builds a target";
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
			// The clean command should be run before the build command, this verifies that commands
			// are executed in the order they are given on the command line
			assertThat(isClean()).isTrue();
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
