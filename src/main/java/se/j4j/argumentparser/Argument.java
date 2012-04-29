package se.j4j.argumentparser;

import static java.util.Collections.unmodifiableList;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentParser.ParsedArgumentHolder;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.defaultproviders.NonLazyValueProvider;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.interfaces.ArgumentHandler;
import se.j4j.argumentparser.interfaces.DefaultValueProvider;
import se.j4j.argumentparser.interfaces.ParsedValueCallback;
import se.j4j.argumentparser.interfaces.ParsedValueFinalizer;
import se.j4j.argumentparser.interfaces.ValueValidator;
import se.j4j.argumentparser.internal.Usage;

/**
 * TODO: decide what names to use
 * Argument
 * ArgumentHandler
 * Converter? names....
 * ArgumentDefinition
 * 
 * @param <T>
 */
@Immutable
public final class Argument<T>
{
	private final @Nonnull List<String> names;

	private final @Nullable String defaultValueDescription;
	private final @Nonnull String description;
	private final @Nonnull String metaDescription;
	private final @Nullable String separator;

	private final boolean required;
	private final boolean ignoreCase;
	private final boolean isPropertyMap;
	private final boolean isAllowedToRepeat;
	private final boolean hideFromUsage;

	private final @Nonnull ArgumentHandler<T> handler;
	private final @Nonnull ValueValidator<T> validator;
	private final @Nullable DefaultValueProvider<T> defaultValueProvider;
	private final @Nullable ParsedValueFinalizer<T> parsedValueFinalizer;
	private final @Nonnull ParsedValueCallback<T> parseValueCallback;

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
	Argument(final @Nonnull ArgumentBuilder<?, T> builder)
	{
		this.handler = builder.handler();
		this.defaultValueProvider = builder.defaultValueProvider;
		this.defaultValueDescription = builder.defaultValueDescription;
		this.description = builder.description;
		this.required = builder.required;
		this.separator = builder.separator;
		this.ignoreCase = builder.ignoreCase;
		this.names = unmodifiableList(builder.names);
		this.validator = builder.validator;
		this.isPropertyMap = builder.isPropertyMap;
		this.isAllowedToRepeat = builder.isAllowedToRepeat;
		this.hideFromUsage = builder.hideFromUsage;
		this.metaDescription = builder.metaDescription;
		this.parsedValueFinalizer = builder.parsedValueFinalizer;
		this.parseValueCallback = builder.parsedValueCallback;
		if(handler == null)
			throw new IllegalArgumentException("No handler set for argument with names: " + names());
		if(defaultValueProvider instanceof NonLazyValueProvider<?>)
		{
			T defaultValue = defaultValueProvider.defaultValue();
			try
			{
				validate(defaultValue);
			}
			catch(InvalidArgument e)
			{
				// This means that a value passed to
				// ArgumentBuilder#defaultValue(Object) wasn't acceptable
				throw new RuntimeException("Invalid default value (" + defaultValue + ") given", e);
			}
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
		try
		{
			validate(value);
		}
		catch(InvalidArgument e)
		{
			// This indicates a programmer error in a DefaultValueProvider
			// implementation
			throw new RuntimeException("Invalid default value (" + value + ") given", e);
		}
		return value;
	}

	public boolean isIgnoringCase()
	{
		return ignoreCase;
	}

	@Override
	public String toString()
	{
		return Usage.forSingleArgument(this);
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

	public void validate(final @Nullable T value) throws InvalidArgument
	{
		validator.validate(value);
	}

	void finalizeValue(@Nullable T value, @Nonnull ParsedArgumentHolder holder)
	{
		if(parsedValueFinalizer != null)
		{
			holder.put(this, parsedValueFinalizer.finalizeValue(value));
		}
	}

	void parsedValue(@Nullable T value)
	{
		parseValueCallback.parsedValue(value);
	}
}
