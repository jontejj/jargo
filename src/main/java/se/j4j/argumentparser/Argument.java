package se.j4j.argumentparser;

import static java.util.Collections.unmodifiableList;
import static se.j4j.argumentparser.ArgumentExceptions.forLimit;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentExceptions.LimitException;
import se.j4j.argumentparser.CommandLineParsers.ParsedArgumentHolder;
import se.j4j.argumentparser.CommandLineParsers.ParsedArguments;
import se.j4j.argumentparser.StringParsers.InternalStringParser;

/**
 * <pre>
 * An {@link Argument} instance is the fundamental building block that glues all functionality in this package together.
 * 
 * Usual {@link Argument}s are created with the static methods in {@link ArgumentFactory} and then
 * used by the {@link CommandLineParsers} to parse strings (typically from the command line).
 * 
 * TODO: document each property of an argument here
 * 
 * TODO: add readFromConsole(), should required arguments be handled by this always?
 * 			think about Multiple Occurrences (ask Do you want to enter one more value for [-s] (y/N):
 * 			Can autocomplete be provided?
 * 			Repeat reading for invalid values
 * 
 * @param <T> the type of values this {@link Argument} is configured to parse
 * 
 * </pre>
 */
@Immutable
public final class Argument<T>
{
	@Nonnull private final List<String> names;

	@Nonnull private final String description;
	@Nonnull private final String metaDescription;
	@Nullable private final String separator;

	private final boolean required;
	private final boolean ignoreCase;
	private final boolean isPropertyMap;
	private final boolean isAllowedToRepeat;
	private final boolean hideFromUsage;

	@Nonnull private final InternalStringParser<T> parser;
	@Nullable private final Provider<T> defaultValueProvider;
	@Nullable private final Describer<T> defaultValueDescriber;

	@Nullable private final Finalizer<T> finalizer;
	@Nonnull private final Limiter<T> limiter;
	@Nonnull private final Callback<T> valueCallback;

	/**
	 * <pre>
	 * Creates a basic object for handling {@link Argument}s taken from a command line invocation.
	 * For practical uses of this constructor see {@link ArgumentFactory#optionArgument(String...)} (and friends)
	 * and the {@link ArgumentBuilder}.
	 * </pre>
	 * 
	 * @return an Argument that can be given as input to
	 *         {@link CommandLineParser#forArguments(Argument...)} and
	 *         {@link ParsedArguments#get(Argument)}
	 */
	Argument(@Nonnull final ArgumentBuilder<?, T> builder)
	{
		this.parser = builder.internalParser();
		this.defaultValueProvider = builder.defaultValueProvider();
		this.defaultValueDescriber = builder.describer();
		this.description = builder.description();
		this.required = builder.isRequired();
		this.separator = builder.separator();
		this.ignoreCase = builder.isIgnoringCase();
		this.names = unmodifiableList(builder.names());
		this.isPropertyMap = builder.isPropertyMap();
		this.isAllowedToRepeat = builder.isAllowedToRepeat();
		this.hideFromUsage = builder.isHiddenFromUsage();
		this.metaDescription = builder.metaDescription();

		this.finalizer = builder.finalizer();
		this.limiter = builder.limiter();
		this.valueCallback = builder.callback();
	}

	/**
	 * Parses command line arguments and returns the value of this argument.<br>
	 * This is a shorthand method that should be used if only one {@link Argument} is expected as it
	 * will result in an unnecessary amount of {@link CommandLineParser} instance creations.
	 * If several arguments are expected use {@link CommandLineParser#forArguments(Argument...)}
	 * instead. Especially if you're concerned about performance or want to support several
	 * arguments.
	 * 
	 * @param actualArguments the arguments from the command line
	 * @return the parsed value from the <code>actualArguments</code>
	 * @throws ArgumentException if actualArguments isn't compatible with this
	 *             argument
	 */
	@Nullable
	public T parse(@Nonnull String ... actualArguments) throws ArgumentException
	{
		// TODO: consider saving the CommandLineParser instance
		return CommandLineParsers.forArguments(this).parse(actualArguments).get(this);
	}

	@Nonnull
	@CheckReturnValue
	public String usage(@Nonnull String programName)
	{
		// TODO: consider saving the CommandLineParser instance
		return CommandLineParsers.forArguments(this).usage(programName);
	}

	@Nonnull
	InternalStringParser<T> parser()
	{
		return parser;
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
		return description;
	}

	boolean isNamed()
	{
		return !names.isEmpty();
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

	/**
	 * @return the default value for this argument, defaults to
	 *         {@link InternalStringParser#defaultValue()}.
	 *         Set by {@link ArgumentBuilder#defaultValue(Object)} or
	 *         {@link ArgumentBuilder#defaultValueProvider(Provider)}
	 */
	@Nullable
	T defaultValue()
	{
		T value = null;
		if(defaultValueProvider != null)
		{
			value = defaultValueProvider.provideValue();
		}
		else
		{
			value = parser.defaultValue();
		}
		if(finalizer != null)
		{
			value = finalizer.finalizeValue(value);
		}
		// If this throws it indicates that a bad combination of Provider/Limiter is used
		checkLimitForDefaultValue(value);

		return value;
	}

	boolean isIgnoringCase()
	{
		return ignoreCase;
	}

	@Override
	public String toString()
	{
		return CommandLineParsers.forArguments(this).usage("");
	}

	@Nullable
	String defaultValueDescription()
	{
		T defaultValue = defaultValue();
		if(defaultValueDescriber != null)
			return defaultValueDescriber.describe(defaultValue);
		return parser().describeValue(defaultValue);
	}

	@Nonnull
	String metaDescription()
	{
		return metaDescription;
	}

	boolean shouldBeHiddenInUsage()
	{
		return hideFromUsage;
	}

	void checkLimit(@Nullable final T value) throws LimitException
	{
		Limit limit = limiter.withinLimits(value);
		if(limit != Limit.OK)
		{
			LimitException e = forLimit(limit);
			e.errorneousArgument(this);
			throw e;
		}
	}

	private void checkLimitForDefaultValue(@Nullable final T value)
	{
		try
		{
			checkLimit(value);
		}
		catch(LimitException e)
		{
			throw new IllegalArgumentException("Invalid default value given: " + e.getMessage(), e);
		}
	}

	void finalizeValue(@Nonnull ParsedArgumentHolder holder)
	{
		if(finalizer != null)
		{
			T value = holder.getValue(this);
			T finalizedValue = finalizer.finalizeValue(value);
			holder.put(this, finalizedValue);
		}
	}

	void parsedValue(@Nonnull ParsedArgumentHolder holder)
	{
		valueCallback.parsedValue(holder.getValue(this));
	}
}
