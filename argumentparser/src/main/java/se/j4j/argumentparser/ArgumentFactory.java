/* Copyright 2013 Jonatan Jönsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
*/
package se.j4j.argumentparser;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.asList;
import static se.j4j.argumentparser.StringParsers.bigDecimalParser;
import static se.j4j.argumentparser.StringParsers.bigIntegerParser;
import static se.j4j.argumentparser.StringParsers.booleanParser;
import static se.j4j.argumentparser.StringParsers.byteParser;
import static se.j4j.argumentparser.StringParsers.charParser;
import static se.j4j.argumentparser.StringParsers.enumParser;
import static se.j4j.argumentparser.StringParsers.fileParser;
import static se.j4j.argumentparser.StringParsers.integerParser;
import static se.j4j.argumentparser.StringParsers.longParser;
import static se.j4j.argumentparser.StringParsers.shortParser;
import static se.j4j.argumentparser.StringParsers.stringParser;
import static se.j4j.strings.Describers.booleanAsEnabledDisabled;
import static se.j4j.strings.Describers.characterDescriber;
import static se.j4j.strings.Describers.fileDescriber;
import static se.j4j.strings.Describers.numberDescriber;

import java.io.File;
import java.math.BigDecimal;
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
 * parsed, {@link ArgumentBuilder#parse(String...)} can be used instead.
 * </pre>
 */
public final class ArgumentFactory
{
	private ArgumentFactory()
	{
	}

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
	 * Creates an {@link Argument} that uses {@link StringParsers#byteParser()}
	 * 
	 * @param names the {@link ArgumentBuilder#names(String...)} to use
	 */
	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Byte> byteArgument(final String ... names)
	{
		return withParser(byteParser()).defaultValueDescriber(numberDescriber()).names(names);
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
		return withParser(shortParser()).defaultValueDescriber(numberDescriber()).names(names);
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
		return withParser(integerParser()).defaultValueDescriber(numberDescriber()).names(names);
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
		return withParser(longParser()).defaultValueDescriber(numberDescriber()).names(names);
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
		return withParser(bigIntegerParser()).defaultValueDescriber(numberDescriber()).names(names);
	}

	/**
	 * Creates an {@link Argument} that uses {@link StringParsers#bigIntegerParser()}
	 * 
	 * @param names the {@link ArgumentBuilder#names(String...)} to use
	 */
	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<BigDecimal> bigDecimalArgument(final String ... names)
	{
		return withParser(bigDecimalParser()).defaultValueDescriber(numberDescriber()).names(names);
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
	 * Creates an {@link CommandBuilder} for {@code command}. Used for setting up custom things like
	 * {@link ArgumentBuilder#ignoreCase()} for a {@link Command#commandName()}. Typically not needed
	 * as {@link CommandLineParser#and(Command)} or {@link CommandLineParser#withCommands(Command...)}
	 * can be used.
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
