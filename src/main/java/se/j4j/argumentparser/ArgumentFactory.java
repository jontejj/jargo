package se.j4j.argumentparser;

public final class ArgumentFactory
{
	private ArgumentFactory(){}

	//TODO: add DateArgument
	//TODO: rename to make it more clear and document behavior

	public static BooleanArgument booleanArgument(final String ... names)
	{
		return new BooleanArgument(names);
	}

	public static IntegerArgument integerArgument(final String ... names)
	{
		return new IntegerArgument(names);
	}

	public static ShortArgument shortArgument(final String ... names)
	{
		return new ShortArgument(names);
	}

	public static ByteArgument byteArgument(final String ... names)
	{
		return new ByteArgument(names);
	}

	public static CharArgument charArgument(final String ... names)
	{
		return new CharArgument(names);
	}

	public static DoubleArgument doubleArgument(final String ... names)
	{
		return new DoubleArgument(names);
	}

	public static FloatArgument floatArgument(final String ... names)
	{
		return new FloatArgument(names);
	}

	public static StringArgument stringArgument(final String ... names)
	{
		return new StringArgument(names);
	}

	public static OptionArgument optionArgument(final String ... names)
	{
		return new OptionArgument(names);
	}

	public static FileArgument fileArgument(final String ... names)
	{
		return new FileArgument(names);
	}

	public static IntegerArithmeticArgument integerArithmeticArgument(final String ... names)
	{
		return new IntegerArithmeticArgument(names);
	}


	public static CommandParser commandArgument(final String ... names)
	{
		return new CommandParser(names);
	}
}
