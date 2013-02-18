package se.j4j.argumentparser.commands;

import static org.fest.assertions.Assertions.assertThat;
import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.ParsedArguments;

public class Build extends Command
{
	final BuildTarget target;

	public Build(BuildTarget target)
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

	public static class BuildTarget
	{
		private boolean cleaned;
		private boolean built;

		void build()
		{
			// The clean command should be run before the build command, this verifies that commands
			// are executed in the order they are given on the command line
			assertThat(isClean()).as("Target should be clean before being built, hasn't the clean command been executed?").isTrue();
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
