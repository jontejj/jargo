package se.j4j.argumentparser;

import static se.j4j.argumentparser.Describers.characterDescriber;
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

import java.io.File;
import java.math.BigInteger;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import se.j4j.argumentparser.ArgumentBuilder.OptionArgumentBuilder;
import se.j4j.argumentparser.StringParsers.Radix;

/**
 * <pre>
 * Used as a starting point to create {@link Argument} instances through
 * different {@link ArgumentBuilder}s.
 * 
 * The produced arguments can either be passed to
 * {@link CommandLineParsers#forArguments(Argument...)} to group several
 * {@link Argument}s together or if only one argument should be
 * parsed {@link ArgumentBuilder#parse(String...)} can be called instead.
 * </pre>
 */
public final class ArgumentFactory
{
	private ArgumentFactory()
	{
	}

	// TODO: add helpArgument()
	// names("-h", "--help", "?", "help")

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Boolean> booleanArgument(@Nonnull final String ... names)
	{
		return withParser(booleanParser()).metaDescription("boolean").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Integer> integerArgument(@Nonnull final String ... names)
	{
		return new RadixiableArgumentBuilder<Integer>(){
			@Override
			protected StringParser<Integer> parser()
			{
				return integerParser(radix());
			}
		}.metaDescription("integer").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Short> shortArgument(@Nonnull final String ... names)
	{
		return new RadixiableArgumentBuilder<Short>(){
			@Override
			protected StringParser<Short> parser()
			{
				return shortParser(radix());
			}
		}.metaDescription("short").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Byte> byteArgument(@Nonnull final String ... names)
	{
		return new RadixiableArgumentBuilder<Byte>(){
			@Override
			protected StringParser<Byte> parser()
			{
				return byteParser(radix());
			}
		}.metaDescription("byte").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Long> longArgument(@Nonnull final String ... names)
	{
		return new RadixiableArgumentBuilder<Long>(){
			@Override
			protected StringParser<Long> parser()
			{
				return longParser(radix());
			}
		}.metaDescription("long").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<BigInteger> bigIntegerArgument(@Nonnull final String ... names)
	{
		return withParser(bigIntegerParser()).metaDescription("big-integer").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Character> charArgument(@Nonnull final String ... names)
	{
		return withParser(charParser()).metaDescription("character").defaultValueDescription(characterDescriber()).names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Double> doubleArgument(@Nonnull final String ... names)
	{
		return withParser(doubleParser()).metaDescription("double").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Float> floatArgument(@Nonnull final String ... names)
	{
		return withParser(floatParser()).metaDescription("float").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<String> stringArgument(@Nonnull final String ... names)
	{
		return withParser(stringParser()).metaDescription("string").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<File> fileArgument(@Nonnull final String ... names)
	{
		return withParser(fileParser()).metaDescription("path").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static OptionArgumentBuilder optionArgument(@Nonnull final String ... names)
	{
		// TODO: check names length, either enforce it by having the first
		// argument a String and not part of the varargs or
		// checkArgument(names.length > 0)
		return new OptionArgumentBuilder().names(names).defaultValue(false);
	}

	@CheckReturnValue
	@Nonnull
	public static <T extends Enum<T>> DefaultArgumentBuilder<T> enumArgument(@Nonnull final Class<T> enumToHandle)
	{
		return withParser(enumParser(enumToHandle)).metaDescription("value");
	}

	@CheckReturnValue
	@Nonnull
	public static CommandBuilder command(@Nonnull final Command command)
	{
		return new CommandBuilder(command).names(command.commandName());
	}

	@CheckReturnValue
	@Nonnull
	public static <T> DefaultArgumentBuilder<T> withParser(@Nonnull final StringParser<T> parser)
	{
		return new DefaultArgumentBuilder<T>(parser);
	}

	@NotThreadSafe
	public static final class DefaultArgumentBuilder<T> extends ArgumentBuilder<DefaultArgumentBuilder<T>, T>
	{
		private DefaultArgumentBuilder(@Nonnull final StringParser<T> parser)
		{
			super(parser);
		}
	}

	@NotThreadSafe
	public static final class CommandBuilder extends ArgumentBuilder<CommandBuilder, String>
	{
		private CommandBuilder(@Nonnull final Command command)
		{
			super(command);
		}
	}

	public abstract static class RadixiableArgumentBuilder<T extends Number> extends ArgumentBuilder<RadixiableArgumentBuilder<T>, T>
	{
		private Radix radix = Radix.DECIMAL;

		private RadixiableArgumentBuilder()
		{
		}

		/**
		 * Use the given {@link Radix} when parsing/printing values.
		 * Defaults to {@link Radix#DECIMAL}.<br>
		 * <b>Note:</b> {@link Radix#BINARY}, {@link Radix#OCTAL} & {@link Radix#HEX} is parsed as
		 * unsigned values as the sign doesn't really make sense when such values are used.
		 * 
		 * @param aRadix the radix to parse/print values with
		 * @return this builder
		 */
		public RadixiableArgumentBuilder<T> radix(final Radix aRadix)
		{
			radix = aRadix;
			return this;
		}

		Radix radix()
		{
			return radix;
		}
	}

}
