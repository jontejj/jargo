package se.j4j.argumentparser;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.util.Collections.emptyList;
import static se.j4j.argumentparser.Describers.withStaticString;
import static se.j4j.argumentparser.Descriptions.EMPTY_STRING;
import static se.j4j.argumentparser.Descriptions.forString;
import static se.j4j.argumentparser.StringParsers.optionParser;
import static se.j4j.argumentparser.StringParsers.stringParser;
import static se.j4j.argumentparser.internal.StringsUtil.surroundWithMarkers;

import java.util.List;
import java.util.Map;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import se.j4j.argumentparser.ArgumentExceptions.MissingRequiredArgumentException;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.StringParsers.FixedArityParser;
import se.j4j.argumentparser.StringParsers.InternalStringParser;
import se.j4j.argumentparser.StringParsers.KeyValueParser;
import se.j4j.argumentparser.StringParsers.Radix;
import se.j4j.argumentparser.StringParsers.RepeatedArgumentParser;
import se.j4j.argumentparser.StringParsers.StringParserBridge;
import se.j4j.argumentparser.StringParsers.StringSplitterParser;
import se.j4j.argumentparser.StringParsers.VariableArityParser;
import se.j4j.argumentparser.internal.NumberType;

import com.google.common.annotations.Beta;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

/**
 * <pre>
 * Responsible for building {@link Argument} instances.
 * Example builders can be created by {@link ArgumentFactory}.
 * 
 * <b>Note:</b>The code examples assumes that all methods in {@link ArgumentFactory} have been statically imported.
 * 
 * <b>Note:</b>Some methods needs to be called in a specific order
 * (to make the generic type system produce the correct type) and to guide the
 * caller, such invalid orders are documented with {@link Deprecated}. If those warnings
 * are ignored {@link IllegalStateException} will be thrown at the offending call.
 * 
 * @param <SELF_TYPE> the type of the subclass extending this class.
 * 		Concept borrowed from: <a href="http://passion.forco.de/content/emulating-self-types-using-java-generics-simplify-fluent-api-implementation">Ansgar.Konermann's blog</a>
 * 		The pattern also resembles the <a href="http://en.wikipedia.org/wiki/Curiously_recurring_template_pattern">Curiously recurring template pattern</a>
 * @param <T> the type of arguments the built {@link Argument} instance should handle,
 * 	such as {@link Integer} in the case of {@link ArgumentFactory#integerArgument(String...)}
 * </pre>
 */
@NotThreadSafe
public abstract class ArgumentBuilder<SELF_TYPE extends ArgumentBuilder<SELF_TYPE, T>, T>
{
	// ArgumentSetting variables, think about #copy() when adding new ones
	@Nonnull private List<String> names = emptyList();
	@Nonnull private Description description = EMPTY_STRING;
	private boolean required = false;
	@Nullable private String separator = null;
	private boolean ignoreCase = false;
	private boolean isPropertyMap = false;
	private boolean isAllowedToRepeat = false;
	@Nullable private String metaDescription = null;
	private boolean hideFromUsage = false;

	// Members that uses the T type, think about
	// ListArgumentBuilder#copyAsListBuilder() when adding new ones
	@Nullable private Supplier<T> defaultValueSupplier = null;
	@Nullable private Describer<T> defaultValueDescriber = null;
	@Nonnull private Finalizer<T> finalizer = Finalizers.noFinalizer();
	@Nonnull private Limiter<T> limiter = Limiters.noLimits();

	@Nullable private final InternalStringParser<T> internalStringParser;

	protected ArgumentBuilder()
	{
		this.internalStringParser = null;
	}

	ArgumentBuilder(@Nonnull final InternalStringParser<T> stringParser)
	{
		this.internalStringParser = stringParser;
	}

	// SELF_TYPE is passed in by subclasses as a type-variable, so type-safety
	// is up to them
	@SuppressWarnings("unchecked")
	private SELF_TYPE self()
	{
		return (SELF_TYPE) this;
	}

	/**
	 * @return an Immutable {@link Argument} which can be passed to
	 *         {@link CommandLineParser#forArguments(Argument...)}
	 */
	@CheckReturnValue
	@Nonnull
	@OverridingMethodsMustInvokeSuper
	public Argument<T> build()
	{
		// TODO: this could potentially cache the created object, how to make subclasses set
		// themselves as dirty?
		return new Argument<T>(this);
	}

	/**
	 * Parses command line arguments and returns the value of the argument built
	 * by {@link #build()}.<br>
	 * <br>
	 * This is a shorthand method that should be used if only one {@link Argument} is expected as it
	 * will throw if unexpected arguments are
	 * encountered. If several arguments are expected use
	 * {@link CommandLineParser#forArguments(Argument...)} instead.
	 * 
	 * @param actualArguments the arguments from the command line
	 * @return the parsed value from the <code>actualArguments</code>
	 * @throws ArgumentException if actualArguments isn't compatible with this
	 *             argument
	 */
	@Nullable
	public final T parse(@Nonnull String ... actualArguments) throws ArgumentException
	{
		return build().parse(actualArguments);
	}

	public final String usage(@Nonnull String programName)
	{
		return CommandLineParser.forArguments(build()).usage(programName);
	}

	/**
	 * <pre>
	 * Returns a customized parser that the {@link Argument} will use to parse values.
	 * 
	 * This is a suitable place to verify the configuration of your parser.
	 * 
	 * If your {@link StringParser} doesn't support any configuration you can use
	 * {@link ArgumentFactory#withParser(StringParser)} directly instead of subclassing
	 * {@link ArgumentBuilder}
	 * 
	 * @return the {@link StringParser} that performs the actual parsing of an argument value
	 * @throws IllegalStateException if the parser have been configured wrongly
	 * </pre>
	 */
	@Nonnull
	protected abstract StringParser<T> parser();

	@Nonnull
	final InternalStringParser<T> internalParser()
	{
		StringParser<T> parser = parser();
		if(parser != InternalArgumentBuilder.NULL)
			return bridgedParser(parser);
		return internalStringParser;
	}

	private static <T> InternalStringParser<T> bridgedParser(@Nonnull final StringParser<T> parser)
	{
		if(parser instanceof InternalStringParser)
		{
			@SuppressWarnings("unchecked")
			InternalStringParser<T> internalParser = (InternalStringParser<T>) parser;
			return internalParser;
		}
		return new StringParserBridge<T>(parser);
	}

	/**
	 * <b>Note</b>: As commands sometimes gets long and hard to understand it's
	 * recommended to also support long named arguments for the sake of
	 * readability.
	 * 
	 * @param argumentNames <ul>
	 *            <li>"-o" for a short named option/argument</li>
	 *            <li>"--option-name" for a long named option/argument</li>
	 *            <li>"-o", "--option-name" to give the user both choices</li>
	 *            <li>zero elements: the argument must be given at the same position on the command
	 *            line as it is given to {@link CommandLineParser#forArguments(Argument...)} (not
	 *            counting named arguments) which is <b>discouraged</b> because it makes your
	 *            program arguments harder to read and makes your program less maintainable and
	 *            harder to keep backwards compatible with old scripts as you can't change the order
	 *            of the arguments without changing those scripts.</li>
	 *            </ul>
	 * @return this builder
	 */
	public SELF_TYPE names(@Nonnull final String ... argumentNames)
	{
		names = copyOf(argumentNames);
		return self();
	}

	/**
	 * Works just like {@link #names(String...)} but it takes an {@link Iterable} instead.
	 * 
	 * @param argumentNames the list to use as argument names
	 * @return this builder
	 */
	public SELF_TYPE names(@Nonnull final Iterable<String> argumentNames)
	{
		names = copyOf(argumentNames);
		return self();
	}

	/**
	 * If used {@link CommandLineParser#parse(String...)} ignores the case of
	 * the argument names set by {@link ArgumentBuilder#names(String...)}
	 * 
	 * @return this builder
	 */
	public SELF_TYPE ignoreCase()
	{
		ignoreCase = true;
		return self();
	}

	/**
	 * TODO: support resource bundles with i18n texts
	 */

	/**
	 * Provides a description of what this argument does/means
	 * 
	 * @param descriptionString
	 * @return this builder
	 */
	public SELF_TYPE description(@Nonnull final String descriptionString)
	{
		description = forString(descriptionString);
		return self();
	}

	/**
	 * Provides a description of what this argument does/means
	 * 
	 * @param aDescription
	 * @return this builder
	 */
	public SELF_TYPE description(@Nonnull final Description aDescription)
	{
		description = aDescription;
		return self();
	}

	/**
	 * Makes {@link CommandLineParser#parse(String...)} throw
	 * {@link MissingRequiredArgumentException} if this argument isn't given
	 * 
	 * @return this builder
	 * @throws IllegalStateException if {@link #defaultValue(Object)} (or
	 *             {@link #defaultValueSupplier(Supplier)}) has been
	 *             called, because these two methods are mutually exclusive
	 */
	public SELF_TYPE required()
	{
		if(defaultValueSupplier != null)
			throw new IllegalStateException("Having a requried argument defaulting to some value (" + defaultValueSupplier.get()
					+ ") makes no sense. Remove the call to ArgumentBuilder#defaultValueSupplier(...) to use a required argument.");

		required = true;
		return self();
	}

	/**
	 * <pre>
	 * Sets a default value to use for this argument.
	 * Returned by {@link ParsedArguments#get(Argument)} when no argument was given.
	 * To create default values lazily see {@link ArgumentBuilder#defaultValueProvider(Provider)}.
	 * 
	 * <b>Mutability</b>:Remember that as {@link Argument} is {@link Immutable}
	 * this value should be so too if multiple argument parsings is going to take place.
	 * If mutability is wanted {@link ArgumentBuilder#defaultValueProvider(Provider)} should be used instead.
	 * 
	 * @return this builder
	 * @throws IllegalStateException if {@link #required()} has been called,
	 * because these two methods are mutually exclusive
	 */
	public SELF_TYPE defaultValue(@Nullable final T value)
	{
		if(required)
			throw new IllegalStateException("Having a requried argument defaulting to some value(" + value
					+ ") makes no sense. Remove the call to ArgumentBuilder#required to use a default value.");

		defaultValueSupplier = Suppliers.ofInstance(value);
		return self();
	}

	/**
	 * <pre>
	 * Sets a supplier that can supply default values in the absence of this argument
	 * 
	 * <b>Note:</b>May be removed in the future if Guava is removed as a dependency
	 * 
	 * @see Suppliers#memoize(Supplier)
	 * 
	 * @return this builder
	 * @throws IllegalStateException if {@link #required()} has been called,
	 * because these two methods are mutually exclusive
	 */
	@Beta
	public SELF_TYPE defaultValueSupplier(@Nonnull final Supplier<T> aDefaultValueSupplier)
	{
		if(required)
			throw new IllegalStateException("Having a requried argument and a default value supplier makes no sense. Remove the call to ArgumentBuilder#required to use a default value.");

		defaultValueSupplier = aDefaultValueSupplier;
		return self();
	}

	/**
	 * Provides a way to give the usage texts a better explanation of a default
	 * value than {@link Object#toString()} provides
	 * 
	 * @param aDescription the description
	 * @return this builder
	 */
	public SELF_TYPE defaultValueDescription(@Nonnull final String aDescription)
	{
		this.defaultValueDescriber = withStaticString(aDescription);
		return self();
	}

	/**
	 * Provides a way to give the usage texts a better explanation of a default
	 * value than {@link Object#toString()} provides
	 * 
	 * @param describer a describer
	 * @return this builder
	 */
	public SELF_TYPE defaultValueDescription(@Nonnull final Describer<T> describer)
	{
		this.defaultValueDescriber = describer;
		return self();
	}

	/**
	 * <pre>
	 * By default {@link StringParser}s provides a meta description (by implementing {@link StringParser#metaDescription()}
	 * that describes the type of data they expect. For instance, if you're writing a music player,
	 * as a user of your application would you rather see:
	 * 
	 * --track-nr &lt;integer&gt;     The track number to play (the default provided by {@link StringParser#metaDescription()})
	 * or:
	 * --track-nr &lt;track nr&gt;    The track number to play (using <code> metaDescription("track nr") </code>)
	 * ?
	 * </pre>
	 * 
	 * <b>Note:</b> empty meta descriptions aren't allowed
	 * 
	 * @param aMetaDescription "track nr" in the above example
	 * @return this builder
	 * @throws IllegalArgumentException if aMetaDescription is empty or null
	 */
	public SELF_TYPE metaDescription(@Nonnull final String aMetaDescription)
	{
		checkArgument(!isNullOrEmpty(aMetaDescription), "a meta description can't be null/empty");
		this.metaDescription = surroundWithMarkers(aMetaDescription);
		return self();
	}

	/**
	 * Hides this argument so that it's not displayed in the usage texts.<br>
	 * It's recommended that hidden arguments aren't {@link #required()}
	 * 
	 * @return this builder
	 */
	public SELF_TYPE hideFromUsage()
	{
		this.hideFromUsage = true;
		return self();
	}

	/**
	 * @param aSeparator the character that separates the argument name and
	 *            argument value, defaults to a space
	 * @return this builder
	 */
	public SELF_TYPE separator(@Nonnull final String aSeparator)
	{
		separator = aSeparator;
		return self();
	}

	/**
	 * <pre>
	 * Makes this argument handle properties like arguments:
	 * -Dproperty_name=value
	 * where "-D" is the string supplied to {@link #names(String...)},
	 * "value" is decoded by the previously set {@link StringParser}.
	 * "property_name" is the key in the resulting {@link Map}
	 * </pre>
	 * 
	 * @return this builder wrapped in a more specific builder
	 */
	@CheckReturnValue
	public final MapArgumentBuilder<String, T> asPropertyMap()
	{
		checkState(defaultValueSupplier == null, "The default value needs to be set after the asPropertyMap invocation.");
		checkState(defaultValueDescriber == null, "The default value describer needs to be set after the asPropertyMap invocation.");
		return new MapArgumentBuilder<String, T>(this, stringParser());
	}

	/**
	 * <pre>
	 * Makes this argument handle properties like arguments:
	 * -Dproperty_name=value
	 * where "-D" is one of the strings supplied to {@link #names(String...)},
	 * "property_name" is decoded by <code>keyParser</code> and
	 * "value" is decoded by the {@link StringParser} previously passed to the constructor.
	 * 
	 * For example:
	 * <code>
	 * Map&lt;Integer, Integer&gt; numberMap = ArgumentFactory.integerArgument("-N")
	 * 						.asKeyValuesWithKeyParser(StringParsers.integerParser())
	 * 						.parse("-N1=5", "-N2=10");
	 * assertThat(numberMap.get(1)).isEqualTo(5);
	 * </code>
	 * 
	 * For this to work correctly it's paramount that <code>Key</code> implements a
	 * proper {@link Object#hashCode()} because it's going to be used a key in a {@link Map}.
	 * </pre>
	 * 
	 * @return this builder wrapped in a more specific builder
	 */
	@CheckReturnValue
	public final <Key extends Comparable<Key>> MapArgumentBuilder<Key, T> asKeyValuesWithKeyParser(@Nonnull StringParser<Key> keyParser)
	{
		checkState(defaultValueSupplier == null, "The default value needs to be set after the asKeyValuesWithKeyParser invocation.");
		checkState(defaultValueDescriber == null, "The default value describer needs to be set after the asKeyValuesWithKeyParser invocation.");
		return new MapArgumentBuilder<Key, T>(this, keyParser);
	}

	/**
	 * <pre>
	 * When given a "," this allows for
	 * arguments such as:
	 * -numbers 1,2,3
	 * where the resulting <code>List&lt;Integer&gt;</code> would contain 1, 2 & 3.
	 * 
	 * Doesn't allow empty lists.
	 * </pre>
	 * 
	 * @param valueSeparator the string to split the input with
	 * @return this builder wrapped in a more specific builder
	 */
	@CheckReturnValue
	public SplitterArgumentBuilder<T> splitWith(@Nonnull final String valueSeparator)
	{
		return new SplitterArgumentBuilder<T>(this, valueSeparator);
	}

	/**
	 * <pre>
	 * Useful for handling a variable amount of parameters in the end of a
	 * command.
	 * Uses this argument to parse values but assumes that all the following
	 * parameters are of the same type, integer in the following example:
	 * <code>
	 * String[] threeArgs = {"--numbers", "1", "2", "3"};
	 * List&lt;Integer&gt; numbers = integerArgument("--numbers").variableArity().parse(threeArgs);
	 * assertThat(numbers).isEqualTo(asList(1, 2, 3));
	 * 
	 * String[] twoArgs = {"--numbers", "1", "2"};
	 * List&lt;Integer&gt; numbers = integerArgument("--numbers").variableArity().parse(twoArgs);
	 * assertThat(numbers).isEqualTo(asList(1, 2));
	 * </code>
	 * </pre>
	 * 
	 * @return this builder wrapped in a more specific builder
	 */
	@CheckReturnValue
	public ArityArgumentBuilder<T> variableArity()
	{
		return new ArityArgumentBuilder<T>(this);
	}

	/**
	 * <pre>
	 * Uses this argument to parse values but assumes that <code>numberOfParameters</code> of the
	 * following parameters are of the same type,integer in the following example:
	 * <code>
	 * String[] args = {"--numbers", "1", "2"};
	 * List&lt;Integer&gt; numbers = integerArgument("--numbers").arity(2).parse(args);
	 * assertThat(numbers).isEqualTo(asList(1, 2));
	 * </code>
	 * <b>Note:</b>If the argument isn't {@link #required()} the default value
	 * will be a list that contains <code>numberOfParameters</code> elements
	 * of {@link StringParser#defaultValue()}, in the above example that would be two zeros.
	 * If this isn't wanted use {@link #defaultValue(Object)} to override it.
	 * 
	 * @return this builder wrapped in a more specific builder
	 * </pre>
	 */
	@CheckReturnValue
	public ArityArgumentBuilder<T> arity(final int numberOfParameters)
	{
		checkArgument(numberOfParameters > 1, "Arity requires at least 2 parameters (got %s)", numberOfParameters);

		return new ArityArgumentBuilder<T>(this, numberOfParameters);
	}

	/**
	 * <pre>
	 * Makes it possible to enter several values for the same argument. Such as this:
	 * <code>
	 * String[] arguments = {"--number", "1", "--number", "2"};
	 * List&lt;Integer&gt; numbers = integerArgument("--number").repeated().parse(arguments);
	 * assertThat(numbers).isEqualTo(Arrays.asList(1, 2));
	 * </code>
	 * 
	 * If you want to combine {@link #repeated()} with a specific {@link #arity(int)} then call
	 * {@link #arity(int)} before calling this.
	 * <code>
	 * String[] arguments = {"--numbers", "1", "2", "--numbers", "3", "4"};
	 * List&lt;List&lt;Integer&gt;&gt; numberLists = integerArgument("--numbers").arity(2).repeated().parse(arguments);
	 * assertThat(numberLists).isEqualTo(asList(asList(1, 2), asList(3, 4)));
	 * </code>
	 * 
	 * For repeated values in a property map such as this:
	 * <code>
	 * String[] arguments = {"-Nnumber=1", "-Nnumber=2"};
	 * Map&lt;String, List&lt;Integer&gt;&gt; numberMap = integerArgument("-N").repeated().asPropertyMap().parse(arguments);
	 * assertThat(numberMap.get("number")).isEqualTo(Arrays.asList(1, 2));
	 * </code>
	 * 
	 * {@link #repeated()} should be called before {@link #asPropertyMap()}.
	 * 
	 * For arguments without a name use {@link #variableArity()} instead.
	 * 
	 * @return this builder wrapped in a more specific builder
	 */
	@CheckReturnValue
	public RepeatedArgumentBuilder<T> repeated()
	{
		return new RepeatedArgumentBuilder<T>(this);
	}

	/**
	 * <pre>
	 * {@link Finalizer}s are called after {@link StringParser#parse(String)}
	 * but before {@link Limiter#withinLimits(Object)}.
	 * 
	 * Can be used to modify the value produced by {@link StringParser#parse(String)}.
	 * 
	 * For example {@link RepeatedArgumentParser} uses this to make the resulting {@link List} {@link Immutable}.
	 * 
	 * <b>Note:</b> If {@link #finalizeWith(Finalizer)} have been called before,
	 * the given <code>aFinalizer</code> will be run after that finalizer.
	 * To clear out any previously set {@link Finalizer}s use {@link #clearFinalizers()}
	 * </pre>
	 * 
	 * @param aFinalizer a finalizer
	 * @return this builder
	 */
	public SELF_TYPE finalizeWith(@Nonnull Finalizer<T> aFinalizer)
	{
		finalizer = Finalizers.compound(finalizer, aFinalizer);
		return self();
	}

	/**
	 * Clear out all {@link Finalizer}s set by {@link #finalizeWith(Finalizer)}
	 * 
	 * @return this builder
	 */
	public SELF_TYPE clearFinalizers()
	{
		finalizer = Finalizers.noFinalizer();
		return self();
	}

	/**
	 * <pre>
	 * Limits values parsed so that they conform to some specific rule.
	 * For example {@link Limiters#existingFiles()} only allows existing files.
	 * 
	 * <b>Note:</b> If {@link #limitTo(Limiter)} has been called before for this
	 * builder the previous {@link Limiter} will be called
	 * before this newly added one, i.e both will be called.
	 * 
	 * To clear out previously set {@link Limiter}s call {@link #clearLimiters()}.
	 * 
	 * </pre>
	 * 
	 * @param aLimiter a limiter
	 * @return this builder
	 */
	public SELF_TYPE limitTo(@Nonnull Limiter<T> aLimiter)
	{
		limiter = Limiters.compound(limiter, aLimiter);
		return self();
	}

	/**
	 * Clear out all {@link Limiter}s set by {@link #limitTo(Limiter)}
	 * 
	 * @return this builder
	 */
	public SELF_TYPE clearLimiters()
	{
		limiter = Limiters.noLimits();
		return self();
	}

	@Override
	public String toString()
	{
		return new Argument<T>(this).toString();
	}

	/**
	 * <pre>
	 * Copies all values from the given copy into this one, except for:
	 * {@link ArgumentBuilder#parser()}, {@link ArgumentBuilder#defaultValueSupplier} & {@link ArgumentBuilder#defaulValueDescriber}
	 * as they may change between different builders
	 * (e.g the default value for Argument&lt;Boolean&gt; and Argument&lt;List&lt;Boolean&gt;&gt; are not compatible)
	 * @param copy the ArgumentBuilder to copy from
	 */
	@OverridingMethodsMustInvokeSuper
	protected void copy(@Nonnull final ArgumentBuilder<?, ?> copy)
	{
		this.names = copy.names;
		this.description = copy.description;
		this.required = copy.required;
		this.separator = copy.separator;
		this.ignoreCase = copy.ignoreCase;
		this.isPropertyMap = copy.isPropertyMap;
		this.isAllowedToRepeat = copy.isAllowedToRepeat;
		this.metaDescription = copy.metaDescription;
		this.hideFromUsage = copy.hideFromUsage;
	}

	SELF_TYPE allowRepeatedArguments()
	{
		isAllowedToRepeat = true;
		return self();
	}

	SELF_TYPE setAsPropertyMap()
	{
		isPropertyMap = true;
		return self();
	}

	/**
	 * @formatter.off
	 */
	@Nonnull
	List<String> names(){ return names; }

	@Nullable Describer<T> defaultValueDescriber(){ return defaultValueDescriber; }

	@Nonnull Description description(){ return description; }

	boolean isRequired(){ return required; }

	@Nullable String separator(){ return separator; }

	boolean isIgnoringCase(){ return ignoreCase; }

	boolean isPropertyMap(){ return isPropertyMap; }

	boolean isAllowedToRepeat(){ return isAllowedToRepeat; }

	@Nullable String metaDescription(){ return metaDescription; }
	boolean isHiddenFromUsage(){ return hideFromUsage; }

	@Nullable Supplier<T> defaultValueSupplier(){ return defaultValueSupplier; }

	@Nonnull Finalizer<T> finalizer(){ return finalizer; }
	@Nonnull Limiter<T> limiter(){ return limiter; }

	/**
	 * @formatter.on
	 */

	@NotThreadSafe
	public static final class DefaultArgumentBuilder<T> extends ArgumentBuilder<DefaultArgumentBuilder<T>, T>
	{
		private final StringParser<T> parser;

		DefaultArgumentBuilder(@Nonnull final StringParser<T> aParser)
		{
			parser = aParser;
		}

		@Override
		protected StringParser<T> parser()
		{
			return parser;
		}
	}

	@NotThreadSafe
	public static final class RadixiableArgumentBuilder<T extends Number & Comparable<T>> extends ArgumentBuilder<RadixiableArgumentBuilder<T>, T>
	{
		private Radix radix = Radix.DECIMAL;
		private final NumberType<T> type;
		private Range<T> validRange;

		private RadixiableArgumentBuilder(NumberType<T> type)
		{
			this.type = type;
			validRange = type.asRange();
		}

		static <T extends Number & Comparable<T>> RadixiableArgumentBuilder<T> radixiableArgument(NumberType<T> type)
		{
			return new RadixiableArgumentBuilder<T>(type);
		}

		/**
		 * Use the given {@link Radix} when parsing/printing values.
		 * Defaults to {@link Radix#DECIMAL}.<br>
		 * <b>Note:</b> {@link Radix#BINARY}, {@link Radix#OCTAL} & {@link Radix#HEX} is parsed as
		 * unsigned values as the sign doesn't really make much sense when such values are used.
		 * 
		 * @param aRadix the radix to parse/print values with
		 * @return this builder
		 */
		public RadixiableArgumentBuilder<T> radix(@Nonnull final Radix aRadix)
		{
			radix = aRadix;
			return this;
		}

		public RadixiableArgumentBuilder<T> minValue(@Nonnull T minimumValue)
		{
			validRange = Ranges.closed(minimumValue, validRange.upperEndpoint());
			return this;
		}

		public RadixiableArgumentBuilder<T> maxValue(@Nonnull T maximumValue)
		{
			validRange = Ranges.closed(validRange.lowerEndpoint(), maximumValue);
			return this;
		}

		@Override
		protected StringParser<T> parser()
		{
			// TODO: maybe radix can be passed to the limiter and print the endpoints for the
			// limiter?
			// TODO: should default value be validated here? What if it's expensive to create the
			// default value?
			// limitTo(Limiters.range(validRange));
			return StringParsers.RadixiableParser.radixiableParser(radix, type, validRange);
		}
	}

	@NotThreadSafe
	public static final class CommandBuilder extends InternalArgumentBuilder<CommandBuilder, String>
	{
		CommandBuilder(@Nonnull final Command command)
		{
			super(command);
		}
	}

	// Non-Interesting builders below, most declarations under here handles
	// (by deprecating) invalid invariants between different argument properties

	private static class InternalArgumentBuilder<Builder extends InternalArgumentBuilder<Builder, T>, T> extends ArgumentBuilder<Builder, T>
	{
		static final StringParser<?> NULL = new ForwardingStringParser.SimpleForwardingStringParser<Object>(){
		};

		InternalArgumentBuilder()
		{

		}

		InternalArgumentBuilder(@Nonnull InternalStringParser<T> parser)
		{
			super(parser);
		}

		// Only used to flag that this builder is an internal one
		@SuppressWarnings("unchecked")
		@Override
		protected StringParser<T> parser()
		{
			return (StringParser<T>) NULL;
		}
	}

	private static class ListArgumentBuilder<Builder extends ListArgumentBuilder<Builder, T>, T> extends InternalArgumentBuilder<Builder, List<T>>
	{
		ListArgumentBuilder(InternalStringParser<List<T>> parser)
		{
			super(parser);
		}

		void copyAsListBuilder(ArgumentBuilder<?, T> builder, int nrOfElementsInDefaultValue)
		{
			finalizeWith(Finalizers.forListValues(builder.finalizer));
			finalizeWith(Finalizers.<T>unmodifiableListFinalizer());

			limitTo(Limiters.forListValues(builder.limiter));
			if(builder.defaultValueSupplier != null)
			{
				defaultValueSupplier(new ListSupplier<T>(builder.defaultValueSupplier, nrOfElementsInDefaultValue));
			}
			if(builder.defaultValueDescriber != null)
			{
				defaultValueDescription(Describers.forListValues(builder.defaultValueDescriber));
			}
		};
	}

	@NotThreadSafe
	public static final class ArityArgumentBuilder<T> extends ListArgumentBuilder<ArityArgumentBuilder<T>, T>
	{
		private ArityArgumentBuilder(@Nonnull final ArgumentBuilder<? extends ArgumentBuilder<?, T>, T> builder, final int arity)
		{
			super(new FixedArityParser<T>(builder.internalParser(), arity));
			init(builder, arity);
		}

		private ArityArgumentBuilder(@Nonnull final ArgumentBuilder<? extends ArgumentBuilder<?, T>, T> builder)
		{
			super(new VariableArityParser<T>(builder.internalParser()));
			init(builder, 1);
		}

		private void init(@Nonnull final ArgumentBuilder<? extends ArgumentBuilder<?, T>, T> builder, int nrOfElementsInDefaultValue)
		{
			copy(builder);
			copyAsListBuilder(builder, nrOfElementsInDefaultValue);
		}

		/**
		 * @Deprecated
		 *             <pre>
		 * This doesn't work with {@link ArgumentBuilder#arity(int)} or {@link ArgumentBuilder#variableArity()}
		 * I.e --foo 1,2 3,4
		 * is currently unsupported
		 * </pre>
		 */
		@Deprecated
		@Override
		public SplitterArgumentBuilder<List<T>> splitWith(final String valueSeparator)
		{
			throw new IllegalStateException("splitWith(...) doesn't work with arity/variableArity()");
		}
	}

	@NotThreadSafe
	public static final class RepeatedArgumentBuilder<T> extends ListArgumentBuilder<RepeatedArgumentBuilder<T>, T>
	{
		private RepeatedArgumentBuilder(@Nonnull final ArgumentBuilder<? extends ArgumentBuilder<?, T>, T> builder)
		{
			super(new RepeatedArgumentParser<T>(builder.internalParser()));
			copy(builder);
			copyAsListBuilder(builder, 1);
			allowRepeatedArguments();
		}

		/**
		 * @Deprecated this method should be called before {@link #repeated()}
		 */
		@Deprecated
		@Override
		public ArityArgumentBuilder<List<T>> arity(final int numberOfParameters)
		{
			throw new IllegalStateException("Programmer Error. Call arity(...) before repeated()");
		}

		/**
		 * @Deprecated this method should be called before {@link #repeated()}
		 */
		@Override
		@Deprecated
		public ArityArgumentBuilder<List<T>> variableArity()
		{
			throw new IllegalStateException("Programmer Error. Call variableArity(...) before repeated()");
		}

		/**
		 * @Deprecated call {@link #splitWith(String)} before {@link #repeated()}
		 */
		@Deprecated
		@Override
		public SplitterArgumentBuilder<List<T>> splitWith(final String valueSeparator)
		{
			throw new IllegalStateException("call splitWith(String) before repeated()");
		}
	}

	@NotThreadSafe
	public static final class OptionArgumentBuilder extends InternalArgumentBuilder<OptionArgumentBuilder, Boolean>
	{
		OptionArgumentBuilder()
		{
			defaultValue(false);
		}

		@Override
		protected StringParser<Boolean> parser()
		{
			return optionParser(defaultValueSupplier().get());
		}

		// TODO: as these are starting to get out of hand, maybe introduce
		// BasicArgumentBuilder without any advanced stuff
		/**
		 * @deprecated an optional flag can't be required
		 */
		@Deprecated
		@Override
		public OptionArgumentBuilder required()
		{
			throw new IllegalStateException("An optional flag can't be requried");
		}

		/**
		 * @deprecated a separator is useless since an optional flag can't be
		 *             assigned a value
		 */
		@Deprecated
		@Override
		public OptionArgumentBuilder separator(final String aSeparator)
		{
			throw new IllegalStateException("A seperator for an optional flag isn't supported as an optional flag can't be assigned a value");
		}

		/**
		 * @deprecated an optional flag can only have an arity of zero
		 */
		@Deprecated
		@Override
		public ArityArgumentBuilder<Boolean> arity(final int numberOfParameters)
		{
			throw new IllegalStateException("An optional flag can't have any other arity than zero");
		}

		/**
		 * @deprecated an optional flag can only have an arity of zero
		 */
		@Deprecated
		@Override
		public ArityArgumentBuilder<Boolean> variableArity()
		{
			throw new IllegalStateException("An optional flag can't have any other arity than zero");
		}

		/**
		 * @deprecated an optional flag can't be split by anything
		 */
		@Deprecated
		@Override
		public SplitterArgumentBuilder<Boolean> splitWith(final String valueSeparator)
		{
			throw new IllegalStateException("An optional flag can't be split as it has no value that is parsed");
		}
	}

	@NotThreadSafe
	public static final class MapArgumentBuilder<K extends Comparable<K>, V> extends InternalArgumentBuilder<MapArgumentBuilder<K, V>, Map<K, V>>
	{
		private MapArgumentBuilder(@Nonnull final ArgumentBuilder<?, V> builder, StringParser<K> keyParser)
		{
			super(new KeyValueParser<K, V>(builder.internalParser(), bridgedParser(keyParser)));
			copy(builder);

			if(names().isEmpty())
				throw new IllegalStateException("No leading identifier (otherwise called names), for example -D, specified for property map. Call names(...) to provide it.");

			if(separator() != null)
			{
				checkState(!separator().isEmpty(), "In a key=value pair a separator of at least one character is required");
			}
			else
			{
				separator("=");
			}

			limitTo(Limiters.<K, V>forMapValues(builder.limiter));

			finalizeWith(Finalizers.<K, V>forMapValues(builder.finalizer));
			finalizeWith(Finalizers.<K, V>unmodifiableMapFinalizer());

			setAsPropertyMap();
		}

		/**
		 * @deprecated because {@link #repeated()} should be called before {@link #asPropertyMap()}
		 */
		@Deprecated
		@Override
		public RepeatedArgumentBuilder<Map<K, V>> repeated()
		{
			throw new IllegalStateException("You'll need to call repeated before asPropertyMap");
		}

		/**
		 * @deprecated because {@link #splitWith(String)} should be
		 *             called before {@link #asPropertyMap()}.
		 *             This is to make generic work its magic and produce the
		 *             correct type, for example {@code Map<String, List<Integer>>}.
		 */
		@Deprecated
		@Override
		public SplitterArgumentBuilder<Map<K, V>> splitWith(final String valueSeparator)
		{
			throw new IllegalStateException("You'll need to call splitWith before asPropertyMap");
		}
	}

	@NotThreadSafe
	public static final class SplitterArgumentBuilder<T> extends ListArgumentBuilder<SplitterArgumentBuilder<T>, T>
	{
		private SplitterArgumentBuilder(@Nonnull final ArgumentBuilder<?, T> builder, @Nonnull final String valueSeparator)
		{
			super(new StringSplitterParser<T>(valueSeparator, builder.internalParser()));
			copy(builder);
			copyAsListBuilder(builder, 1);
		}

		/**
		 * @deprecated you can't use both {@link #splitWith(StringSplitter)} and {@link #arity(int)}
		 */
		@Deprecated
		@Override
		public ArityArgumentBuilder<List<T>> arity(final int numberOfParameters)
		{
			throw new IllegalStateException("You can't use both splitWith and arity");
		}

		/**
		 * @deprecated you can't use both {@link #splitWith(StringSplitter)} and
		 *             {@link #variableArity()}
		 */
		@Deprecated
		@Override
		public ArityArgumentBuilder<List<T>> variableArity()
		{
			throw new IllegalStateException("You can't use both splitWith and variableArity");
		}
	}

	/**
	 * Exposes package-private {@link Argument} methods that can be called without the generic type
	 * parameter
	 */
	abstract static class ArgumentSettings
	{
		@Nonnull
		abstract List<String> names();

		@Nullable
		abstract String separator();

		abstract String metaDescriptionInRightColumn();

		abstract boolean isAllowedToRepeat();

		abstract boolean isIgnoringCase();

		abstract boolean isPropertyMap();

		boolean isNamed()
		{
			return !names().isEmpty();
		}
	}

	private static final class ListSupplier<T> implements Supplier<List<T>>
	{
		private final Supplier<T> singleElementSupplier;
		private final int elementsToSupply;

		ListSupplier(Supplier<T> elementSupplier, final int elementsToSupply)
		{
			this.singleElementSupplier = elementSupplier;
			this.elementsToSupply = elementsToSupply;
		}

		@Override
		public List<T> get()
		{
			List<T> result = newArrayListWithCapacity(elementsToSupply);
			for(int i = 0; i < elementsToSupply; i++)
			{
				result.add(singleElementSupplier.get());
			}
			return result;
		}

	}
}
