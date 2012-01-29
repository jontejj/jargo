package se.j4j.argumentparser;

import java.io.File;

import se.j4j.argumentparser.handlers.BooleanArgument;
import se.j4j.argumentparser.handlers.ByteArgument;
import se.j4j.argumentparser.handlers.CharArgument;
import se.j4j.argumentparser.handlers.DoubleArgument;
import se.j4j.argumentparser.handlers.FileArgument;
import se.j4j.argumentparser.handlers.FloatArgument;
import se.j4j.argumentparser.handlers.IntegerArgument;
import se.j4j.argumentparser.handlers.IntegerArithmeticArgument;
import se.j4j.argumentparser.handlers.OptionArgument;
import se.j4j.argumentparser.handlers.ShortArgument;
import se.j4j.argumentparser.handlers.StringArgument;

public final class ArgumentFactory
{
	private ArgumentFactory(){}

	//TODO: add DateArgument
	//TODO: rename to make it more clear and document behavior

	public static ArgumentBuilder<Boolean> booleanArgument(final String ... names)
	{
		return new ArgumentBuilder<Boolean>(new BooleanArgument()).names(names);
	}

	public static ArgumentBuilder<Integer> integerArgument(final String ... names)
	{
		return new ArgumentBuilder<Integer>(new IntegerArgument()).names(names);
	}

	public static ArgumentBuilder<Short> shortArgument(final String ... names)
	{
		return new ArgumentBuilder<Short>(new ShortArgument()).names(names);
	}

	public static ArgumentBuilder<Byte> byteArgument(final String ... names)
	{
		return new ArgumentBuilder<Byte>(new ByteArgument()).names(names);
	}

	public static ArgumentBuilder<Character> charArgument(final String ... names)
	{
		return new ArgumentBuilder<Character>(new CharArgument()).names(names);
	}

	public static ArgumentBuilder<Double> doubleArgument(final String ... names)
	{
		return new ArgumentBuilder<Double>(new DoubleArgument()).names(names);
	}

	public static ArgumentBuilder<Float> floatArgument(final String ... names)
	{
		return new ArgumentBuilder<Float>(new FloatArgument()).names(names);
	}

	public static ArgumentBuilder<String> stringArgument(final String ... names)
	{
		return new ArgumentBuilder<String>(new StringArgument()).names(names);
	}

	public static ArgumentBuilder<File> fileArgument(final String ... names)
	{
		return new ArgumentBuilder<File>(new FileArgument()).names(names);
	}

	public static ArgumentBuilder<Boolean> optionArgument(final String ... names)
	{
		return new OptionArgumentBuilder().names(names);
	}

	public static IntegerArithmeticArgumentBuilder integerArithmeticArgument(final String ... names)
	{
		return new IntegerArithmeticArgumentBuilder(names);
	}

	public static CommandArgumentBuilder commandArgument(final String ... names)
	{
		return new CommandArgumentBuilder(names);
	}

	public static class OptionArgumentBuilder extends ArgumentBuilder<Boolean>
	{
		OptionArgumentBuilder()
		{
			super(null);
			defaultValue(Boolean.FALSE);
		}

		@Override
		public Argument<Boolean> build()
		{
			handler(new OptionArgument(defaultValue));
			return super.build();
		}
	}

	public static class IntegerArithmeticArgumentBuilder extends ArgumentBuilder<Integer>
	{
		private char	operation;

		IntegerArithmeticArgumentBuilder(final String ... names)
		{
			super(null);
			names(names);
		}

		public IntegerArithmeticArgumentBuilder operation(final char operation)
		{
			this.operation = operation;
			return this;
		}

		@Override
		public Argument<Integer> build()
		{
			handler(new IntegerArithmeticArgument().operation(operation));
			return super.build();
		}
	}

	public static class CommandArgumentBuilder extends ArgumentBuilder<String>
	{
		CommandArgumentBuilder(final String ... names)
		{
			super(null);
			names(names);
		}

		private CommandExecutor	commandExecutor;
		private Argument<?>[] arguments;

		public CommandArgumentBuilder setCommandExecutor(final CommandExecutor executor)
		{
			commandExecutor = executor;
			return this;
		}

		public CommandArgumentBuilder withArguments(final Argument<?>... arguments)
		{
			this.arguments = arguments;
			return this;
		}

		@Override
		public CommandParser build()
		{
			return new CommandParser(super.build(), arguments, commandExecutor);
		}
	}
}
