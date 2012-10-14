package se.j4j.argumentparser;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.asList;
import static se.j4j.argumentparser.StringParsers.bigIntegerParser;
import static se.j4j.argumentparser.StringParsers.booleanParser;
import static se.j4j.argumentparser.StringParsers.byteParser;
import static se.j4j.argumentparser.StringParsers.charParser;
import static se.j4j.argumentparser.StringParsers.doubleParser;
import static se.j4j.argumentparser.StringParsers.enumParser;
import static se.j4j.argumentparser.StringParsers.fileParser;
import static se.j4j.argumentparser.StringParsers.floatParser;
import static se.j4j.argumentparser.StringParsers.integerParser;
import static se.j4j.argumentparser.StringParsers.longParser;
import static se.j4j.argumentparser.StringParsers.shortParser;
import static se.j4j.argumentparser.StringParsers.stringParser;
import static se.j4j.strings.Describers.booleanAsEnabledDisabled;
import static se.j4j.strings.Describers.characterDescriber;
import static se.j4j.strings.Describers.fileDescriber;

import java.io.File;
import java.math.BigInteger;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import se.j4j.argumentparser.ArgumentBuilder.CommandBuilder;
import se.j4j.argumentparser.ArgumentBuilder.DefaultArgumentBuilder;
import se.j4j.argumentparser.ArgumentBuilder.OptionArgumentBuilder;
import se.j4j.strings.Describer;
import se.j4j.strings.Describers;

/**
 * <pre>
 * Used as a starting point to create {@link Argument} instances.
 * 
 * The produced arguments can be passed to
 * {@link CommandLineParser#withArguments(Argument...)} to group several
 * {@link Argument}s together. If only one argument should be
 * parsed {@link ArgumentBuilder#parse(String...)} can be used instead.
 * </pre>
 */
public final class ArgumentFactory
{
	private ArgumentFactory()
	{
	}

	// TODO: add CommandLineParser#andHelp(), a command with an optional argument that specifies a
	// specific argument one needs help with. names("-h", "--help", "?", "help")
	// program help build, should print the usage for the build argument/command

	/**
	 * Creates an {@link Argument} that uses {@link StringParsers#booleanParser()}
	 * 
	 * @param names the {@link ArgumentBuilder#names(String...)} to use
	 */
	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Boolean> booleanArgument(final String ... names)
	{
		return withParser(booleanParser()).names(names);
	}

	/**
	 * Creates an {@link Argument} that uses {@link StringParsers#integerParser()}
	 * 
	 * @param names the {@link ArgumentBuilder#names(String...)} to use
	 */
	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Integer> integerArgument(final String ... names)
	{
		return withParser(integerParser()).names(names);
	}

	/**
	 * Creates an {@link Argument} that uses {@link StringParsers#shortParser()}
	 * 
	 * @param names the {@link ArgumentBuilder#names(String...)} to use
	 */
	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Short> shortArgument(final String ... names)
	{
		return withParser(shortParser()).names(names);
	}

	/**
	 * Creates an {@link Argument} that uses {@link StringParsers#byteParser()}
	 * 
	 * @param names the {@link ArgumentBuilder#names(String...)} to use
	 */
	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Byte> byteArgument(final String ... names)
	{
		return withParser(byteParser()).names(names);
	}

	/**
	 * Creates an {@link Argument} that uses {@link StringParsers#longParser()}
	 * 
	 * @param names the {@link ArgumentBuilder#names(String...)} to use
	 */
	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Long> longArgument(final String ... names)
	{
		return withParser(longParser()).names(names);
	}

	/**
	 * Creates an {@link Argument} that uses {@link StringParsers#bigIntegerParser()}
	 * 
	 * @param names the {@link ArgumentBuilder#names(String...)} to use
	 */
	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<BigInteger> bigIntegerArgument(final String ... names)
	{
		return withParser(bigIntegerParser()).names(names);
	}

	/**
	 * Creates an {@link Argument} that uses {@link StringParsers#charParser()}.<br>
	 * Describes default {@link Character}s with {@link Describers#characterDescriber()} in the
	 * usage.
	 * 
	 * @param names the {@link ArgumentBuilder#names(String...)} to use
	 */
	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Character> charArgument(final String ... names)
	{
		return withParser(charParser()).defaultValueDescriber(characterDescriber()).names(names);
	}

	/**
	 * Creates an {@link Argument} that uses {@link StringParsers#doubleParser()}
	 * 
	 * @param names the {@link ArgumentBuilder#names(String...)} to use
	 */
	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Double> doubleArgument(final String ... names)
	{
		return withParser(doubleParser()).names(names);
	}

	/**
	 * Creates an {@link Argument} that uses {@link StringParsers#floatParser()}
	 * 
	 * @param names the {@link ArgumentBuilder#names(String...)} to use
	 */
	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Float> floatArgument(final String ... names)
	{
		return withParser(floatParser()).names(names);
	}

	/**
	 * Creates an {@link Argument} that uses {@link StringParsers#stringParser()}
	 * 
	 * @param names the {@link ArgumentBuilder#names(String...)} to use
	 */
	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<String> stringArgument(final String ... names)
	{
		return withParser(stringParser()).names(names);
	}

	/**
	 * Creates an {@link Argument} that uses {@link StringParsers#fileParser()}.<br>
	 * Describes default {@link File}s with {@link Describers#fileDescriber()} in the usage.
	 * 
	 * @param names the {@link ArgumentBuilder#names(String...)} to use
	 */
	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<File> fileArgument(final String ... names)
	{
		return withParser(fileParser()).defaultValueDescriber(fileDescriber()).names(names);
	}

	/**
	 * <pre>
	 * Creates an optional (flag) {@link Argument} where the sole existence of the name on the command
	 * line matters. This is the only place to have an {@link ArgumentBuilder#arity(int)} of zero.
	 * 
	 * The default value is printed with {@link Describers#booleanAsEnabledDisabled()}.
	 * This can be changed with {@link ArgumentBuilder#defaultValueDescriber(Describer)}.
	 * 
	 * @see Describers#booleanAsOnOff()
	 * 
	 * @param mandatoryName the first name that enables this option
	 * @param optionalNames aliases that also enables this option
	 * </pre>
	 */
	@CheckReturnValue
	@Nonnull
	public static OptionArgumentBuilder optionArgument(final String mandatoryName, final String ... optionalNames)
	{
		return new OptionArgumentBuilder().defaultValueDescriber(booleanAsEnabledDisabled()).names(asList(mandatoryName, optionalNames));
	}

	/**
	 * <pre>
	 * Creates an {@link Argument} that uses an {@link StringParsers#enumParser(Class)} to parse
	 * arguments.
	 * 
	 * If you end up with a big switch statement for your enum consider using {@link Command}s
	 * instead.
	 * 
	 * </pre>
	 * 
	 * @param enumToHandle the {@link Enum} to retrieve enum values from
	 * @param names the {@link ArgumentBuilder#names(String...)} to use
	 */
	@CheckReturnValue
	@Nonnull
	public static <T extends Enum<T>> DefaultArgumentBuilder<T> enumArgument(final Class<T> enumToHandle, final String ... names)
	{
		return withParser(enumParser(enumToHandle)).names(names);
	}

	/**
	 * <pre>
	 * Creates an {@link Argument} for {@code command}.
	 * 
	 * Should only be used if you mix {@link Argument}s that aren't {@link Command}s
	 * with {@link Command}s as {@link CommandLineParser#withCommands(Command...)}
	 * is preferred if you only support {@link Command}s.
	 * </pre>
	 */
	@CheckReturnValue
	@Nonnull
	public static CommandBuilder command(final Command command)
	{
		return new CommandBuilder(command).names(command.commandName()).description(command);
	}

	/**
	 * Used to create {@link Argument} instances with a custom {@link StringParser}.
	 * A custom {@link StringParser} is one which isn't available through {@link StringParsers}
	 */
	@CheckReturnValue
	@Nonnull
	public static <T> DefaultArgumentBuilder<T> withParser(final StringParser<T> parser)
	{
		checkNotNull(parser);
		return new DefaultArgumentBuilder<T>(parser);
	}
}
