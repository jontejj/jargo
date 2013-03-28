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
package se.softhouse.jargo;

import static com.google.common.base.Predicates.alwaysTrue;
import static java.util.Arrays.asList;
import static se.softhouse.common.strings.Descriptions.format;
import static se.softhouse.jargo.ArgumentExceptions.withMessage;
import static se.softhouse.jargo.CommandLineParser.US_BY_DEFAULT;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import se.softhouse.common.guavaextensions.Suppliers2;
import se.softhouse.common.strings.Describer;
import se.softhouse.common.strings.Description;
import se.softhouse.jargo.StringParsers.HelpParser;
import se.softhouse.jargo.StringParsers.InternalStringParser;
import se.softhouse.jargo.internal.Texts.ProgrammaticErrors;
import se.softhouse.jargo.internal.Texts.UserErrors;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

/**
 * <pre>
 * Represents a supported {@link Argument} for a command line invocation.
 * 
 * {@link Argument}s are created with the static methods in {@link Arguments} or with a custom {@link ArgumentBuilder} and then
 * used by the {@link CommandLineParser} to parse strings (typically from the command line).
 * 
 * @param <T> the type of values this {@link Argument} is configured to parse
 * 
 * </pre>
 */
@Immutable
public final class Argument<T>
{
	@Nonnull private final List<String> names;
	@Nonnull private final Description description;
	@Nonnull private final Optional<String> metaDescription;
	private final boolean hideFromUsage;

	@Nonnull private final String separator;
	@Nonnull private final Optional<Locale> localeOveride;
	private final boolean required;
	private final boolean ignoreCase;
	private final boolean isAllowedToRepeat;

	@Nonnull private final InternalStringParser<T> parser;
	@Nonnull private final Supplier<? extends T> defaultValue;
	@Nullable private final Describer<? super T> defaultValueDescriber;
	@Nonnull private final Predicate<? super T> limiter;

	// Internal bookkeeping
	@Nonnull private final Function<T, T> finalizer;
	private final ParameterArity parameterArity;
	private final boolean isPropertyMap;

	/**
	 * <pre>
	 * Creates a basic object for handling {@link Argument}s taken from a command line invocation.
	 * For practical uses of this constructor see {@link Arguments#optionArgument(String, String...)} (and friends)
	 * and the {@link ArgumentBuilder}.
	 * </pre>
	 */
	Argument(final ArgumentBuilder<?, T> builder)
	{
		this.parser = builder.internalParser();
		this.defaultValueDescriber = builder.defaultValueDescriber();
		this.description = builder.description();
		this.required = builder.isRequired();
		this.separator = builder.separator();
		this.localeOveride = builder.localeOverride();
		this.ignoreCase = builder.isIgnoringCase();
		this.names = builder.names();
		this.isPropertyMap = builder.isPropertyMap();
		this.isAllowedToRepeat = builder.isAllowedToRepeat();
		this.hideFromUsage = builder.isHiddenFromUsage();
		this.metaDescription = builder.metaDescription();
		this.parameterArity = builder.parameterArity();

		this.finalizer = builder.finalizer();
		this.limiter = builder.limiter();
		if(builder.defaultValueSupplier() != null)
		{
			this.defaultValue = builder.defaultValueSupplier();
		}
		else
		{
			this.defaultValue = new Supplier<T>(){
				@Override
				public T get()
				{
					return parser.defaultValue();
				}
			};
		}

		// Fail-fast for invalid default values that are created already
		if(Suppliers2.isSuppliedAlready(defaultValue))
		{
			// Calling this makes sure that the default value is within the limits of any limiter
			defaultValue();
		}
	}

	/**
	 * Parses command line arguments and returns the value of this argument.<br>
	 * This is a shorthand method that should be used if only one {@link Argument} is expected.
	 * If several arguments are expected use {@link CommandLineParser#withArguments(Argument...)}
	 * instead. {@link Locale#US} is used by default to parse strings. Use
	 * {@link ArgumentBuilder#locale(Locale)} to specify another {@link Locale}, such as
	 * {@link Locale#getDefault()}.
	 * 
	 * @param actualArguments the arguments from the command line
	 * @return the parsed value from the {@code actualArguments}
	 * @throws ArgumentException if actualArguments isn't what this
	 *             argument expects
	 */
	@Nullable
	public T parse(String ... actualArguments) throws ArgumentException
	{
		return commandLineParser().parse(asList(actualArguments), locale(US_BY_DEFAULT)).get(this);
	}

	/**
	 * Returns the {@link Usage} for this argument. Should only be used if one argument is
	 * supported, otherwise the {@link CommandLineParser#usage()} method should be used instead.
	 * {@link Locale#US} is used by default. Use {@link ArgumentBuilder#locale(Locale)} to specify
	 * another {@link Locale}, such as {@link Locale#getDefault()}.
	 */
	@Nonnull
	@CheckReturnValue
	public Usage usage()
	{
		return new Usage(commandLineParser().allArguments(), locale(US_BY_DEFAULT), ProgramInformation.AUTO, false);
	}

	/**
	 * Describes {@link Argument}s by their first name. If they are indexed and have no name the
	 * {@link StringParser#metaDescription() meta description} is used instead.
	 */
	@Override
	public String toString()
	{
		if(isIndexed())
			return metaDescriptionInRightColumn();
		return names().get(0);
	}

	@Nonnull
	InternalStringParser<T> parser()
	{
		return parser;
	}

	String descriptionOfValidValues(Locale localeToDescribeValuesWith)
	{
		if(limiter != alwaysTrue())
			return limiter.toString();

		return parser().descriptionOfValidValues(this, locale(localeToDescribeValuesWith));
	}

	/**
	 * @return the default value for this argument, defaults to {@link StringParser#defaultValue()}.
	 *         Could also be set by {@link ArgumentBuilder#defaultValue(Object)} or
	 *         {@link ArgumentBuilder#defaultValueSupplier(Supplier)}
	 */
	@Nullable
	T defaultValue()
	{
		T value = finalizer.apply(defaultValue.get());

		// If this throws it indicates that a bad combination of Provider/Limiter
		// is used or simply put that it's an invalid default value
		checkLimitForDefaultValue(value);

		return value;
	}

	@Nullable
	String defaultValueDescription(Locale inLocale)
	{
		T value = defaultValue();
		if(defaultValueDescriber != null)
			return defaultValueDescriber.describe(value, inLocale);
		return parser().describeValue(value);
	}

	@Nonnull
	String metaDescriptionInLeftColumn()
	{
		String meta;

		if(metaDescription.isPresent())
		{
			meta = metaDescription.get();
		}
		else
		{
			// If the meta description hasn't been overridden we default to the one provided by
			// the StringParser interface
			meta = parser.metaDescriptionInLeftColumn(this);
		}

		if(!isPropertyMap())
		{
			meta = separator + meta;
		}
		return meta;
	}

	@Nonnull
	String metaDescriptionInRightColumn()
	{
		// First check if the description has been overridden
		if(metaDescription.isPresent())
			return metaDescription.get();
		return parser.metaDescriptionInRightColumn(this);
	}

	void checkLimit(@Nullable final T value) throws ArgumentException
	{
		if(!limiter.apply(value))
			throw withMessage(format(UserErrors.DISALLOWED_VALUE, value, limiter));
	}

	private void checkLimitForDefaultValue(@Nullable final T value)
	{
		try
		{
			if(!limiter.apply(value))
			{
				String unallowedValue = String.format(UserErrors.DISALLOWED_VALUE, value, limiter);
				throw new IllegalStateException(String.format(ProgrammaticErrors.INVALID_DEFAULT_VALUE, unallowedValue));
			}
		}
		catch(IllegalArgumentException invalidDefaultValue)
		{
			throw new IllegalStateException(String.format(ProgrammaticErrors.INVALID_DEFAULT_VALUE, invalidDefaultValue.getMessage()),
					invalidDefaultValue);
		}
	}

	void finalizeValue(ParsedArguments holder)
	{
		T value = holder.getValue(this);
		T finalizedValue = finalizer.apply(value);
		holder.put(this, finalizedValue);
	}

	boolean isRequired()
	{
		return required;
	}

	@Nullable
	String separator()
	{
		return separator;
	}

	@Nonnull
	String description()
	{
		return description.description();
	}

	@Nonnull
	List<String> names()
	{
		return names;
	}

	boolean isPropertyMap()
	{
		return isPropertyMap;
	}

	boolean isAllowedToRepeat()
	{
		return isAllowedToRepeat;
	}

	ParameterArity parameterArity()
	{
		return parameterArity;
	}

	boolean isIgnoringCase()
	{
		return ignoreCase;
	}

	Locale locale(Locale usualLocale)
	{
		return localeOveride.or(usualLocale);
	}

	boolean isIndexed()
	{
		return names().isEmpty();
	}

	boolean isHelpArgument()
	{
		return parser().equals(HelpParser.INSTANCE);
	}

	private CommandLineParserInstance commandLineParser()
	{
		// Not cached to save memory, users should use CommandLineParser.withArguments if they are
		// concerned about reuse
		return new CommandLineParserInstance(Arrays.<Argument<?>>asList(Argument.this), ProgramInformation.AUTO);
	}

	enum ParameterArity
	{
		/**
		 * Indicates {@link Arguments#optionArgument(String, String...)}
		 */
		NO_ARGUMENTS,
		/**
		 * Indicates {@link ArgumentBuilder#variableArity()}
		 */
		VARIABLE_AMOUNT,
		/**
		 * {@link ArgumentBuilder#arity(int)} or any other {@link Argument}
		 */
		AT_LEAST_ONE_ARGUMENT
	}

	static final Predicate<Argument<?>> IS_INDEXED = new Predicate<Argument<?>>(){
		@Override
		public boolean apply(@Nonnull Argument<?> input)
		{
			return input.isIndexed();
		}
	};

	static final Predicate<Argument<?>> IS_REQUIRED = new Predicate<Argument<?>>(){
		@Override
		public boolean apply(@Nonnull Argument<?> input)
		{
			return input.isRequired();
		}
	};

	static final Predicate<Argument<?>> IS_VISIBLE = new Predicate<Argument<?>>(){
		@Override
		public boolean apply(@Nonnull Argument<?> input)
		{
			return !input.hideFromUsage;
		}
	};

	static final Predicate<Argument<?>> IS_OF_VARIABLE_ARITY = new Predicate<Argument<?>>(){
		@Override
		public boolean apply(@Nonnull Argument<?> input)
		{
			return input.parameterArity() == ParameterArity.VARIABLE_AMOUNT;
		}
	};
}
