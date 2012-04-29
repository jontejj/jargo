package se.j4j.argumentparser;

import java.io.File;
import java.math.BigInteger;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import se.j4j.argumentparser.ArgumentBuilder.OptionArgumentBuilder;
import se.j4j.argumentparser.builders.DefaultArgumentBuilder;
import se.j4j.argumentparser.builders.RadixiableArgumentBuilder;
import se.j4j.argumentparser.handlers.BigIntegerArgument;
import se.j4j.argumentparser.handlers.BooleanArgument;
import se.j4j.argumentparser.handlers.ByteArgument;
import se.j4j.argumentparser.handlers.CharArgument;
import se.j4j.argumentparser.handlers.CommandArgument;
import se.j4j.argumentparser.handlers.DoubleArgument;
import se.j4j.argumentparser.handlers.EnumArgument;
import se.j4j.argumentparser.handlers.FileArgument;
import se.j4j.argumentparser.handlers.FloatArgument;
import se.j4j.argumentparser.handlers.IntegerArgument;
import se.j4j.argumentparser.handlers.LongArgument;
import se.j4j.argumentparser.handlers.ShortArgument;
import se.j4j.argumentparser.handlers.StringArgument;
import se.j4j.argumentparser.handlers.internal.StringConverterWrapper;
import se.j4j.argumentparser.interfaces.StringConverter;

/**
 * <pre>
 * Used as a starting point to create {@link Argument} instances through
 * different {@link ArgumentBuilder}s.
 * 
 * The produced arguments can either be passed to
 * {@link ArgumentParser#forArguments(Argument...)} to group several
 * {@link Argument}s together (the common use case) or if 
 * only one argument should be parsed {@link Argument#parse(String...)}
 * can be used instead.
 * </pre>
 */
public final class ArgumentFactory
{
	private ArgumentFactory()
	{
	}

	// TODO: rename to make it more clear and document behavior
	// TODO: verify parsing of invalid values for each argument type (and their
	// string output when given such values)
	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Boolean> booleanArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<Boolean>(new BooleanArgument()).metaDescription("boolean").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Integer> integerArgument(final @Nonnull String ... names)
	{
		return new RadixiableArgumentBuilder<Integer>(){
			@Override
			protected IntegerArgument handler()
			{
				return new IntegerArgument(radix());
			}
		}.metaDescription("integer").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Short> shortArgument(final @Nonnull String ... names)
	{
		return new RadixiableArgumentBuilder<Short>(){
			@Override
			protected ShortArgument handler()
			{
				return new ShortArgument(radix());
			}
		}.metaDescription("short").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Byte> byteArgument(final @Nonnull String ... names)
	{
		return new RadixiableArgumentBuilder<Byte>(){
			@Override
			protected ByteArgument handler()
			{
				return new ByteArgument(radix());
			}
		}.metaDescription("byte").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Long> longArgument(final @Nonnull String ... names)
	{
		return new RadixiableArgumentBuilder<Long>(){
			@Override
			protected LongArgument handler()
			{
				return new LongArgument(radix());
			}
		}.metaDescription("long").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<BigInteger> bigIntegerArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<BigInteger>(new BigIntegerArgument()).names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Character> charArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<Character>(new CharArgument()).metaDescription("character").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Double> doubleArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<Double>(new DoubleArgument()).metaDescription("double").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Float> floatArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<Float>(new FloatArgument()).metaDescription("float").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<String> stringArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<String>(new StringArgument()).metaDescription("string").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<File> fileArgument(final @Nonnull String ... names)
	{
		return new DefaultArgumentBuilder<File>(new FileArgument()).metaDescription("path").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static OptionArgumentBuilder optionArgument(final @Nonnull String ... names)
	{
		// TODO: check names length, either enforce it by having the first
		// argument a String and not part of the varargs or
		// checkArgument(names.length > 0)
		return new OptionArgumentBuilder().names(names).defaultValue(false);
	}

	@CheckReturnValue
	@Nonnull
	public static <T extends Enum<T>> DefaultArgumentBuilder<T> enumArgument(final @Nonnull Class<T> enumToHandle)
	{
		return new DefaultArgumentBuilder<T>(new EnumArgument<T>(enumToHandle)).metaDescription("value");
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<String> commandArgument(final @Nonnull CommandArgument commandParser)
	{
		return new DefaultArgumentBuilder<String>(commandParser).names(commandParser.commandName());
	}

	@CheckReturnValue
	@Nonnull
	public static <T> DefaultArgumentBuilder<T> customArgument(final @Nonnull StringConverter<T> converter)
	{
		return new DefaultArgumentBuilder<T>(new StringConverterWrapper<T>(converter));
	}
}
