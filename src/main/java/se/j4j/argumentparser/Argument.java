package se.j4j.argumentparser;

import static se.j4j.argumentparser.ArgumentExceptions.withMessage;
import static se.j4j.argumentparser.Descriptions.format;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.ArgumentBuilder.ListSupplier;
import se.j4j.argumentparser.ArgumentBuilder.SupplierOfInstance;
import se.j4j.argumentparser.CommandLineParser.ParsedArgumentHolder;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.StringParsers.InternalStringParser;
import se.j4j.argumentparser.StringParsers.VariableArityParser;
import se.j4j.argumentparser.internal.Finalizer;
import se.j4j.argumentparser.internal.Texts;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * <pre>
 * An {@link Argument} instance is the fundamental building block that glues all
 * functionality in this package together.
 * 
 * Usual {@link Argument}s are created with the static methods in {@link ArgumentFactory} and then
 * used by the {@link CommandLineParser} to parse strings (typically from the command line).
 * 
 * TODO: document each property of an argument here
 * 
 * TODO: add readFromConsole(), should required arguments be handled by this always?
 * 			think about Multiple Occurrences (ask Do you want to enter one more value for [-s] (y/N):
 * 			Can auto complete be provided?
 * 			Repeat reading for invalid values
 * 
 * @param <T> the type of values this {@link Argument} is configured to parse
 * 
 * </pre>
 */
@Immutable
public final class Argument<T> extends ArgumentSettings
{
	@Nonnull private final List<String> names;

	@Nonnull private final Description description;
	@Nullable private final String metaDescription;
	@Nullable private final String separator;

	private final boolean required;
	private final boolean ignoreCase;
	private final boolean isAllowedToRepeat;
	private final boolean hideFromUsage;

	@Nonnull private final InternalStringParser<T> parser;
	@Nonnull private final Supplier<T> defaultValue;
	@Nullable private final Describer<T> defaultValueDescriber;
	@Nonnull private final Predicate<T> limiter;

	@Nonnull private final Supplier<CommandLineParser> commandLineParser = Suppliers.memoize(new Supplier<CommandLineParser>(){
		@Override
		public CommandLineParser get()
		{
			return CommandLineParser.withArguments(Argument.this);
		}
	});

	// Internal bookkeeping
	@Nonnull private final Finalizer<T> finalizer;
	private final int parameterArity;
	private final boolean isPropertyMap;

	private CommandLineParser commandLineParser()
	{
		return commandLineParser.get();
	}

	/**
	 * <pre>
	 * Creates a basic object for handling {@link Argument}s taken from a command line invocation.
	 * For practical uses of this constructor see {@link ArgumentFactory#optionArgument(String...)} (and friends)
	 * and the {@link ArgumentBuilder}.
	 * </pre>
	 * 
	 * @return an Argument that can be given as input to
	 *         {@link CommandLineParser#withArguments(Argument...)} and
	 *         {@link ParsedArguments#get(Argument)}
	 */
	Argument(@Nonnull final ArgumentBuilder<?, T> builder)
	{
		this.parser = builder.internalParser();
		this.defaultValueDescriber = builder.defaultValueDescriber();
		this.description = builder.description();
		this.required = builder.isRequired();
		this.separator = builder.separator();
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

		// Fail-fast for invalid default values that already are created
		if(defaultValue instanceof SupplierOfInstance<?>)
		{
			// Calling this makes sure that the default value is within the limits of any limiter
			defaultValue();
		}
		else if(defaultValue instanceof ListSupplier<?>)
		{
			ListSupplier<?> listSupplier = (ListSupplier<?>) defaultValue;
			if(listSupplier.singleElementSupplier instanceof SupplierOfInstance<?>)
			{
				defaultValue();
			}
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
	public T parse(@Nonnull String ... actualArguments) throws ArgumentException
	{
		return commandLineParser().parse(actualArguments).get(this);
	}

	@Nonnull
	@CheckReturnValue
	public String usage(@Nonnull String programName)
	{
		return commandLineParser().usage(programName);
	}

	/**
	 * Describes {@link Argument}s by their first name. If they are indexed and have no name the
	 * meta description is used instead.
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

	String descriptionOfValidValues()
	{
		if(limiter != Predicates.alwaysTrue())
			return limiter.toString();

		return parser().descriptionOfValidValues(this);
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

	/**
	 * {@link ArgumentFactory#optionArgument(String, String...)} returns 0<br>
	 * {@link ArgumentBuilder#variableArity()} returns {@link VariableArityParser#VARIABLE_ARITY} <br>
	 * {@link ArgumentBuilder#arity(int)} returns <code>numberOfParameters</code>
	 * 
	 * @return
	 */
	@Override
	int parameterArity()
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
		T value = finalizer.finalizeValue(defaultValue.get());

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
	String defaultValueDescription()
	{
		T value = defaultValue();
		if(defaultValueDescriber != null)
			return defaultValueDescriber.describe(value);
		return parser().describeValue(value, this);
	}

	@Nonnull
	String metaDescriptionInLeftColumn()
	{
		String meta = metaDescription;
		// If the meta description hasn't been overridden we default to the one provided by
		// the StringParser interface
		if(meta == null)
		{
			meta = parser.metaDescriptionInLeftColumn(this);
		}

		if(!isPropertyMap())
		{
			if(separator == null)
			{
				meta = " " + meta;
			}
			else
			{
				meta = separator + meta;
			}
		}
		return meta;
	}

	@Override
	@Nonnull
	String metaDescriptionInRightColumn()
	{
		// First check if the description has been overridden
		if(metaDescription != null)
			return metaDescription;
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
			throw withMessage(format(Texts.UNALLOWED_VALUE, value, limiter));
	}

	private void checkLimitForDefaultValue(@Nullable final T value)
	{
		try
		{
			if(!limiter.apply(value))
			{
				String unallowedValue = String.format(Texts.UNALLOWED_VALUE, value, limiter.toString());
				throw new IllegalStateException(String.format(Texts.INVALID_DEFAULT_VALUE, unallowedValue));
			}
		}
		catch(IllegalArgumentException invalidDefaultValue)
		{
			throw new IllegalStateException(String.format(Texts.INVALID_DEFAULT_VALUE, invalidDefaultValue.getMessage()), invalidDefaultValue);
		}
	}

	void finalizeValue(@Nonnull ParsedArgumentHolder holder)
	{
		T value = holder.getValue(this);
		T finalizedValue = finalizer.finalizeValue(value);
		holder.put(this, finalizedValue);
	}
}
