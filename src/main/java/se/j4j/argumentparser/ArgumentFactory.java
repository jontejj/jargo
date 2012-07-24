package se.j4j.argumentparser;

import static com.google.common.collect.Lists.asList;
import static se.j4j.argumentparser.ArgumentBuilder.RadixiableArgumentBuilder.radixiableArgument;
import static se.j4j.argumentparser.Describers.booleanAsEnabledDisabled;
import static se.j4j.argumentparser.Describers.characterDescriber;
import static se.j4j.argumentparser.Describers.fileDescriber;
import static se.j4j.argumentparser.StringParsers.bigIntegerParser;
import static se.j4j.argumentparser.StringParsers.booleanParser;
import static se.j4j.argumentparser.StringParsers.charParser;
import static se.j4j.argumentparser.StringParsers.doubleParser;
import static se.j4j.argumentparser.StringParsers.enumParser;
import static se.j4j.argumentparser.StringParsers.fileParser;
import static se.j4j.argumentparser.StringParsers.floatParser;
import static se.j4j.argumentparser.StringParsers.stringParser;

import java.io.File;
import java.math.BigInteger;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import se.j4j.argumentparser.ArgumentBuilder.CommandBuilder;
import se.j4j.argumentparser.ArgumentBuilder.DefaultArgumentBuilder;
import se.j4j.argumentparser.ArgumentBuilder.OptionArgumentBuilder;
import se.j4j.argumentparser.ArgumentBuilder.RadixiableArgumentBuilder;
import se.j4j.argumentparser.internal.NumberType;

/**
 * <pre>
 * Used as a starting point to create {@link Argument} instances.
 * 
 * The produced arguments can either be passed to
 * {@link CommandLineParser#forArguments(Argument...)} to group several
 * {@link Argument}s together or if only one argument should be
 * parsed {@link ArgumentBuilder#parse(String...)} can be called instead.
 * </pre>
 */
public final class ArgumentFactory
{
	private ArgumentFactory()
	{
	}

	// TODO: add helpArgument(), a command with an optional argument that specifies a specific
	// argument one needs help with. names("-h", "--help", "?", "help")
	// program help build, should print the usage for the build argument/command

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Boolean> booleanArgument(@Nonnull final String ... names)
	{
		return withParser(booleanParser()).names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Integer> integerArgument(@Nonnull final String ... names)
	{
		return radixiableArgument(NumberType.INTEGER).names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Short> shortArgument(@Nonnull final String ... names)
	{
		return radixiableArgument(NumberType.SHORT).names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Byte> byteArgument(@Nonnull final String ... names)
	{
		return radixiableArgument(NumberType.BYTE).names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Long> longArgument(@Nonnull final String ... names)
	{
		return radixiableArgument(NumberType.LONG).names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<BigInteger> bigIntegerArgument(@Nonnull final String ... names)
	{
		return withParser(bigIntegerParser()).names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Character> charArgument(@Nonnull final String ... names)
	{
		return withParser(charParser()).defaultValueDescription(characterDescriber()).names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Double> doubleArgument(@Nonnull final String ... names)
	{
		return withParser(doubleParser()).names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Float> floatArgument(@Nonnull final String ... names)
	{
		return withParser(floatParser()).names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<String> stringArgument(@Nonnull final String ... names)
	{
		return withParser(stringParser()).names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<File> fileArgument(@Nonnull final String ... names)
	{
		return withParser(fileParser()).defaultValueDescription(fileDescriber()).names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static OptionArgumentBuilder optionArgument(@Nonnull final String mandatoryName, @Nonnull final String ... optionalNames)
	{
		// TODO: booleanAsEnabledDisabled really be called here?
		return new OptionArgumentBuilder().defaultValueDescription(booleanAsEnabledDisabled()).names(asList(mandatoryName, optionalNames));
	}

	@CheckReturnValue
	@Nonnull
	public static <T extends Enum<T>> DefaultArgumentBuilder<T> enumArgument(@Nonnull final Class<T> enumToHandle, @Nonnull final String ... names)
	{
		return withParser(enumParser(enumToHandle)).names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static CommandBuilder command(@Nonnull final Command command)
	{
		return new CommandBuilder(command).names(command.commandName()).description(command);
	}

	@CheckReturnValue
	@Nonnull
	public static <T> DefaultArgumentBuilder<T> withParser(@Nonnull final StringParser<T> parser)
	{
		return new DefaultArgumentBuilder<T>(parser);
	}
}
