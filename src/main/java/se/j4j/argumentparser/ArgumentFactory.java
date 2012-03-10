package se.j4j.argumentparser;

import java.io.File;

import javax.annotation.Nonnull;

import se.j4j.argumentparser.builders.ArgumentBuilder;
import se.j4j.argumentparser.builders.ArgumentBuilder.OptionArgumentBuilder;
import se.j4j.argumentparser.builders.DefaultArgumentBuilder;
import se.j4j.argumentparser.builders.IntegerArithmeticArgumentBuilder;
import se.j4j.argumentparser.handlers.BooleanArgument;
import se.j4j.argumentparser.handlers.ByteArgument;
import se.j4j.argumentparser.handlers.CharArgument;
import se.j4j.argumentparser.handlers.CommandArgument;
import se.j4j.argumentparser.handlers.DoubleArgument;
import se.j4j.argumentparser.handlers.EnumArgument;
import se.j4j.argumentparser.handlers.FileArgument;
import se.j4j.argumentparser.handlers.FloatArgument;
import se.j4j.argumentparser.handlers.IntegerArgument;
import se.j4j.argumentparser.handlers.ShortArgument;
import se.j4j.argumentparser.handlers.StringArgument;
import se.j4j.argumentparser.handlers.internal.StringConverterWrapper;
import se.j4j.argumentparser.interfaces.StringConverter;

public final class ArgumentFactory
{
	private ArgumentFactory(){}

	//TODO: add DateArgument
	//TODO: rename to make it more clear and document behavior
	//TODO: verify parsing of invalid values for each argument type (and their string output)

	@Nonnull
	public static ArgumentBuilder<?, Boolean> booleanArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<Boolean>(new BooleanArgument()).names(names);
	}

	@Nonnull
	public static ArgumentBuilder<?, Integer> integerArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<Integer>(new IntegerArgument()).names(names);
	}

	@Nonnull
	public static ArgumentBuilder<?, Short> shortArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<Short>(new ShortArgument()).names(names);
	}

	@Nonnull
	public static ArgumentBuilder<?, Byte> byteArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<Byte>(new ByteArgument()).names(names);
	}

	@Nonnull
	public static ArgumentBuilder<?, Character> charArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<Character>(new CharArgument()).names(names);
	}

	@Nonnull
	public static ArgumentBuilder<?, Double> doubleArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<Double>(new DoubleArgument()).names(names);
	}

	@Nonnull
	public static ArgumentBuilder<?, Float> floatArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<Float>(new FloatArgument()).names(names);
	}

	@Nonnull
	public static ArgumentBuilder<?, String> stringArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<String>(new StringArgument()).names(names);
	}

	@Nonnull
	public static ArgumentBuilder<?, File> fileArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<File>(new FileArgument()).names(names);
	}

	@Nonnull
	public static OptionArgumentBuilder optionArgument(final @Nonnull String ... names)
	{
		return new OptionArgumentBuilder().names(names).defaultValue(false);
	}

	@Nonnull
	public static <T extends Enum<T>> ArgumentBuilder<?, T> enumArgument(final @Nonnull Class<T> enumToHandle)
	{
		return new DefaultArgumentBuilder<T>(new EnumArgument<T>(enumToHandle));
	}

	@Nonnull
	public static IntegerArithmeticArgumentBuilder integerArithmeticArgument(final @Nonnull String ... names)
	{
		return new IntegerArithmeticArgumentBuilder().names(names);
	}

	@Nonnull
	public static ArgumentBuilder<?, String> commandArgument(final @Nonnull CommandArgument commandParser)
	{
		return new DefaultArgumentBuilder<String>(commandParser).names(commandParser.commandName());
	}

	public static <T> ArgumentBuilder<?, T> customArgument(final @Nonnull StringConverter<T> converter)
	{
		return new DefaultArgumentBuilder<>(new StringConverterWrapper<>(converter));
	}
}
