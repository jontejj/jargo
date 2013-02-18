package se.j4j.argumentparser;

import static se.j4j.argumentparser.ArgumentExceptions.withMessage;
import static se.j4j.strings.Descriptions.format;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.StringParsers.InternalStringParser;
import se.j4j.argumentparser.internal.Texts.ProgrammaticErrors;
import se.j4j.argumentparser.internal.Texts.UserErrors;
import se.j4j.guavaextensions.Suppliers2;
import se.j4j.strings.Describer;
import se.j4j.strings.Description;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * <pre>
 * Represents a supported {@link Argument} for a command line invocation.
 * 
 * {@link Argument}s are created with the static methods in {@link ArgumentFactory} or with a custom {@link ArgumentBuilder} and then
 * used by the {@link CommandLineParser} to parse strings (typically from the command line).
 * 
 * @param <T> the type of values this {@link Argument} is configured to parse
 * 
 * </pre>
 */
@Immutable
public final class Argument<T> extends ArgumentSettings
{
	enum ParameterArity
	{
		NO_ARGUMENTS,
		/**
		 * A marker for a variable amount of parameters
		 */
		VARIABLE_AMOUNT,
		AT_LEAST_ONE_ARGUMENT
	}

	@Nonnull private final List<String> names;

	@Nonnull private final Description description;
	@Nullable private final Optional<String> metaDescription;
	@Nullable private final String separator;

	private final Optional<Locale> localeOveride;

	private final boolean required;
	private final boolean ignoreCase;
	private final boolean isAllowedToRepeat;
	private final boolean hideFromUsage;

	@Nonnull private final InternalStringParser<T> parser;
	@Nonnull private final Supplier<? extends T> defaultValue;
	@Nullable private final Describer<? super T> defaultValueDescriber;
	@Nonnull private final Predicate<? super T> limiter;

	@Nonnull private final Supplier<CommandLineParserInstance> commandLineParser = Suppliers.memoize(new Supplier<CommandLineParserInstance>(){
		@Override
		public CommandLineParserInstance get()
		{
			return new CommandLineParserInstance(Arrays.<Argument<?>>asList(Argument.this), ProgramInformation.AUTO);
		}
	});

	// Internal bookkeeping
	@Nonnull private final Function<T, T> finalizer;
	private final ParameterArity parameterArity;
	private final boolean isPropertyMap;

	private CommandLineParserInstance commandLineParser()
	{
		return commandLineParser.get();
	}

	/**
	 * <pre>
	 * Creates a basic object for handling {@link Argument}s taken from a command line invocation.
	 * For practical uses of this constructor see {@link ArgumentFactory#optionArgument(String, String...)} (and friends)
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
	 * instead.
	 * 
	 * @param actualArguments the arguments from the command line
	 * @return the parsed value from the {@code actualArguments}
	 * @throws ArgumentException if actualArguments isn't what this
	 *             argument expects
	 */
	@Nullable
	public T parse(String ... actualArguments) throws ArgumentException
	{
		return commandLineParser().parse(Arrays.asList(actualArguments), locale(Locale.getDefault())).get(this);
	}

	/**
	 * Returns a usage string for this argument. Should only be used if one argument is supported,
	 * otherwise the {@link CommandLineParser#usage()} method should be used instead.
	 */
	@Nonnull
	@CheckReturnValue
	public String usage()
	{
		return new Usage(commandLineParser().allArguments(), locale(Locale.getDefault()), ProgramInformation.AUTO).toString();
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
		if(limiter != Predicates.alwaysTrue())
			return limiter.toString();

		return parser().descriptionOfValidValues(this, locale(localeToDescribeValuesWith));
	}

	@Override
	boolean isRequired()
	{
		return required;
	}

	@Override
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

	@Override
	@Nonnull
	List<String> names()
	{
		return names;
	}

	@Override
	boolean isPropertyMap()
	{
		return isPropertyMap;
	}

	@Override
	boolean isAllowedToRepeat()
	{
		return isAllowedToRepeat;
	}

	@Override
	ParameterArity parameterArity()
	{
		return parameterArity;
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

	@Override
	boolean isIgnoringCase()
	{
		return ignoreCase;
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

	@Override
	@Nonnull
	String metaDescriptionInRightColumn()
	{
		// First check if the description has been overridden
		if(metaDescription.isPresent())
			return metaDescription.get();
		return parser.metaDescriptionInRightColumn(this);
	}

	@Override
	boolean isHiddenFromUsage()
	{
		return hideFromUsage;
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

	Locale locale(Locale usualLocale)
	{
		return localeOveride.or(usualLocale);
	}
}
