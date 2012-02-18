package se.j4j.argumentparser;

import java.io.File;

import se.j4j.argumentparser.builders.ArgumentBuilder;
import se.j4j.argumentparser.builders.DefaultArgumentBuilder;
import se.j4j.argumentparser.builders.IntegerArithmeticArgumentBuilder;
import se.j4j.argumentparser.builders.internal.OptionArgumentBuilder;
import se.j4j.argumentparser.handlers.BooleanArgument;
import se.j4j.argumentparser.handlers.ByteArgument;
import se.j4j.argumentparser.handlers.CharArgument;
import se.j4j.argumentparser.handlers.CommandArgument;
import se.j4j.argumentparser.handlers.DoubleArgument;
import se.j4j.argumentparser.handlers.FileArgument;
import se.j4j.argumentparser.handlers.FloatArgument;
import se.j4j.argumentparser.handlers.IntegerArgument;
import se.j4j.argumentparser.handlers.ShortArgument;
import se.j4j.argumentparser.handlers.StringArgument;

public final class ArgumentFactory
{
	private ArgumentFactory(){}

	//TODO: add DateArgument
	//TODO: rename to make it more clear and document behavior
	//TODO: verify parsing of invalid values for each argument type (and their string output)

	public static ArgumentBuilder<?, Boolean> booleanArgument(final String ... names)
	{
		return new DefaultArgumentBuilder<Boolean>(new BooleanArgument()).names(names);
	}

	public static ArgumentBuilder<?, Integer> integerArgument(final String ... names)
	{
		return new DefaultArgumentBuilder<Integer>(new IntegerArgument()).names(names);
	}

	public static ArgumentBuilder<?, Short> shortArgument(final String ... names)
	{
		return new DefaultArgumentBuilder<Short>(new ShortArgument()).names(names);
	}

	public static ArgumentBuilder<?, Byte> byteArgument(final String ... names)
	{
		return new DefaultArgumentBuilder<Byte>(new ByteArgument()).names(names);
	}

	public static ArgumentBuilder<?, Character> charArgument(final String ... names)
	{
		return new DefaultArgumentBuilder<Character>(new CharArgument()).names(names);
	}

	public static ArgumentBuilder<?, Double> doubleArgument(final String ... names)
	{
		return new DefaultArgumentBuilder<Double>(new DoubleArgument()).names(names);
	}

	public static ArgumentBuilder<?, Float> floatArgument(final String ... names)
	{
		return new DefaultArgumentBuilder<Float>(new FloatArgument()).names(names);
	}

	public static ArgumentBuilder<?, String> stringArgument(final String ... names)
	{
		return new DefaultArgumentBuilder<String>(new StringArgument()).names(names);
	}

	public static ArgumentBuilder<?, File> fileArgument(final String ... names)
	{
		return new DefaultArgumentBuilder<File>(new FileArgument()).names(names);
	}

	public static ArgumentBuilder<?, Boolean> optionArgument(final String ... names)
	{
		return new OptionArgumentBuilder().names(names);
	}

	public static IntegerArithmeticArgumentBuilder integerArithmeticArgument(final String ... names)
	{
		return new IntegerArithmeticArgumentBuilder(names);
	}

	public static ArgumentBuilder<?, String> commandArgument(final CommandArgument commandParser)
	{
		return new DefaultArgumentBuilder<String>(commandParser);
	}
}
