package se.j4j.argumentparser.builders;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.interfaces.ArgumentHandler;
import se.j4j.argumentparser.interfaces.ValueValidator;
import se.j4j.argumentparser.internal.Usage;

/**
 * @author Jonatan Jönsson <jontejj@gmail.com>
 *
 * TODO: decide what names to use
 * Argument
 * ArgumentHandler
 * 
 * Converter? names....
 * ArgumentDefinition
 * @param <T>
 */
@Immutable
public final class Argument<T>
{
	private final @Nonnull List<String> names;
	//TODO: this could be Mutable, how can we prevent this?
	private final @Nullable T defaultValue;
	private final @Nonnull String description;
	private final boolean required;
	private final @Nullable String separator;
	private final boolean ignoreCase;
	private final @Nonnull ArgumentHandler<T> handler;
	private final @Nullable ValueValidator<T> validator;
	private final boolean isPropertyMap;
	private final boolean isAllowedToRepeat;

	//TODO: add support for metaVars, i.e  -file <path> 	Path to some file
	//TODO: add support for hidden arguments (should not appear in usage)

	/**
	 * <pre>
	 * Creates a basic object for handling Arguments taken from a command line invocation.
	 * For practical uses of this constructor see {@link ArgumentFactory#optionArgument(String...)} and the {@link ArgumentBuilder}.
	 * </pre>
	 * @return an Argument that can be given as input to {@link ArgumentParser#forArguments(Argument...)} and {@link ParsedArguments#get(Argument)}
	 */
	Argument(final @Nonnull ArgumentBuilder<?, T> builder)
	{
		this.handler = builder.handler;
		this.defaultValue = builder.defaultValue;
		this.description = builder.description;
		this.required = builder.required;
		this.separator = builder.separator;
		this.ignoreCase = builder.ignoreCase;
		this.names = Collections.unmodifiableList(builder.names);
		this.validator = builder.validator;
		this.isPropertyMap = builder.isPropertyMap;
		this.isAllowedToRepeat = builder.isAllowedToRepeat;
	}

	@Nonnull
	public ArgumentHandler<?> handler()
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
	 * @return the default value for this argument, defaults to null. Set by {@link ArgumentBuilder#defaultValue(Object)}
	 */
	@Nullable
	public T defaultValue()
	{
		return defaultValue;
	}

	public void validate(final Object value) throws InvalidArgument
	{
		if(validator != null)
		{
			validator.validate((T) value);
		}
	}

	/**
	 * @ret

	 */
	public boolean isIgnoringCase()
	{
		return ignoreCase;
	}

	@Override
	public String toString()
	{
		return Usage.forSingleArgument(this);
	}
}
