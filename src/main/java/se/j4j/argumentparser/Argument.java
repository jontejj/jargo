package se.j4j.argumentparser;

import static java.util.Collections.unmodifiableList;
import static se.j4j.argumentparser.exceptions.LimitException.limitException;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentParser.ParsedArgumentHolder;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.DefaultValueProviders.NonLazyValueProvider;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.LimitException;

/**
 * <pre>
 * An {@link Argument} instance is the fundamental building block that glues all functionality in this package together.
 * 
 * Usual {@link Argument}s are created with the static methods in {@link ArgumentFactory} and then
 * used by the {@link ArgumentParser} to parse strings (typically from the command line).
 * 
 * TODO: document each property of an argument here
 * 
 * @param <T> the type of values this {@link Argument} is configured to handle
 * 
 * </pre>
 */
@Immutable
public final class Argument<T>
{
	@Nonnull private final List<String> names;

	@Nullable private final String defaultValueDescription;
	@Nonnull private final String description;
	@Nonnull private final String metaDescription;
	@Nullable private final String separator;

	private final boolean required;
	private final boolean ignoreCase;
	private final boolean isPropertyMap;
	private final boolean isAllowedToRepeat;
	private final boolean hideFromUsage;

	@Nonnull private final ArgumentHandler<T> handler;
	@Nullable private final DefaultValueProvider<T> defaultValueProvider;

	@Nullable private final Finalizer<T> finalizer;
	@Nonnull private final Limiter<T> limiter;
	@Nonnull private final Callback<T> valueCallback;

	/**
	 * <pre>
	 * Creates a basic object for handling Arguments taken from a command line invocation.
	 * For practical uses of this constructor see {@link ArgumentFactory#optionArgument(String...)} (and friends)
	 * and the {@link ArgumentBuilder}.
	 * </pre>
	 * 
	 * @return an Argument that can be given as input to
	 *         {@link ArgumentParser#forArguments(Argument...)} and
	 *         {@link ParsedArguments#get(Argument)}
	 */
	Argument(@Nonnull final ArgumentBuilder<?, T> builder)
	{
		this.handler = builder.handler();
		this.defaultValueProvider = builder.defaultValueProvider;
		this.defaultValueDescription = builder.defaultValueDescription;
		this.description = builder.description;
		this.required = builder.required;
		this.separator = builder.separator;
		this.ignoreCase = builder.ignoreCase;
		this.names = unmodifiableList(builder.names);
		this.isPropertyMap = builder.isPropertyMap;
		this.isAllowedToRepeat = builder.isAllowedToRepeat;
		this.hideFromUsage = builder.hideFromUsage;
		this.metaDescription = builder.metaDescription;

		this.finalizer = builder.finalizer;
		this.limiter = builder.limiter;
		this.valueCallback = builder.callback;

		if(defaultValueProvider instanceof NonLazyValueProvider<?>)
		{
			T defaultValue = defaultValueProvider.defaultValue();
			// Check if the value passed to
			// ArgumentBuilder#defaultValue(Object) is acceptable
			checkLimitForDefaultValue(defaultValue);
		}
	}

	/**
	 * Parses command line arguments and returns the value of this argument.<br>
	 * This is a shorthand method that should be used if only one
	 * {@link Argument} is expected as it will result in an unnecessary
	 * amount of {@link ArgumentParser} instance creations.
	 * If several arguments are expected use
	 * {@link ArgumentParser#forArguments(Argument...)} instead.
	 * Especially if you're concerned about performance.
	 * 
	 * @param actualArguments the arguments from the command line
	 * @return the parsed value from the <code>actualArguments</code>
	 * @throws ArgumentException if actualArguments isn't compatible with this
	 *             argument
	 */
	@Nullable
	public T parse(@Nonnull String ... actualArguments) throws ArgumentException
	{
		// TODO: consider saving the ArgumentParser instance
		return ArgumentParser.forArguments(this).parse(actualArguments).get(this);
	}

	@Nonnull
	public ArgumentHandler<T> handler()
	{
		return handler;
	}

	public boolean isRequired()
	{
		return required;
	}

	@Nullable
	public String separator()
	{
		return separator;
	}

	@Nonnull
	public String description()
	{
		return description;
	}

	public boolean isNamed()
	{
		return !names.isEmpty();
	}

	@Nonnull
	public List<String> names()
	{
		return names;
	}

	public boolean isPropertyMap()
	{
		return isPropertyMap;
	}

	public boolean isAllowedToRepeat()
	{
		return isAllowedToRepeat;
	}

	/**
	 * @return the default value for this argument, defaults to
	 *         {@link ArgumentHandler#defaultValue()}.
	 *         Set by {@link ArgumentBuilder#defaultValue(Object)} or
	 *         {@link ArgumentBuilder#defaultValueProvider(DefaultValueProvider)}
	 */
	@Nullable
	public T defaultValue()
	{
		T value = null;
		if(defaultValueProvider != null)
		{
			value = defaultValueProvider.defaultValue();
		}
		else
		{
			value = handler.defaultValue();
		}
		// If this throws it indicates a programmer error in a
		// DefaultValueProvider implementation or that the wrong limiter is used
		checkLimitForDefaultValue(value);

		return value;
	}

	public boolean isIgnoringCase()
	{
		return ignoreCase;
	}

	@Override
	public String toString()
	{
		return ArgumentParser.forArguments(this).usage("");
	}

	@Nullable
	public String defaultValueDescription()
	{
		return defaultValueDescription;
	}

	@Nonnull
	public String metaDescription()
	{
		return metaDescription;
	}

	public boolean shouldBeHiddenInUsage()
	{
		return hideFromUsage;
	}

	void checkLimit(@Nullable final T value) throws LimitException
	{
		Limit limit = limiter.withinLimits(value);
		if(limit != Limit.OK)
		{
			LimitException e = limitException(limit);
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

	void finalizeValue(@Nullable T value, @Nonnull ParsedArgumentHolder holder)
	{
		if(finalizer != null)
		{
			holder.put(this, finalizer.finalizeValue(value));
		}
	}

	void parsedValue(@Nullable T value)
	{
		valueCallback.parsedValue(value);
	}
}
