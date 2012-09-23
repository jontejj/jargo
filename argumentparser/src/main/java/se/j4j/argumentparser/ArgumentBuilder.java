package se.j4j.argumentparser;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.isEmpty;
import static java.util.Collections.emptyList;
import static se.j4j.argumentparser.StringParsers.optionParser;
import static se.j4j.argumentparser.StringParsers.stringParser;
import static se.j4j.argumentparser.StringParsers.VariableArityParser.VARIABLE_ARITY;
import static se.j4j.guavaextensions.Functions2.listTransformer;
import static se.j4j.guavaextensions.Predicates2.listPredicate;
import static se.j4j.strings.Descriptions.EMPTY_STRING;
import static se.j4j.strings.Descriptions.withString;

import java.util.List;
import java.util.Map;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.ForwardingStringParser.SimpleForwardingStringParser;
import se.j4j.argumentparser.StringParsers.FixedArityParser;
import se.j4j.argumentparser.StringParsers.InternalStringParser;
import se.j4j.argumentparser.StringParsers.KeyValueParser;
import se.j4j.argumentparser.StringParsers.OptionParser;
import se.j4j.argumentparser.StringParsers.RepeatedArgumentParser;
import se.j4j.argumentparser.StringParsers.StringParserBridge;
import se.j4j.argumentparser.StringParsers.StringSplitterParser;
import se.j4j.argumentparser.StringParsers.VariableArityParser;
import se.j4j.guavaextensions.Functions2;
import se.j4j.guavaextensions.Suppliers2;
import se.j4j.strings.Describer;
import se.j4j.strings.Describers;
import se.j4j.strings.Description;
import se.j4j.texts.Texts;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Ranges;

/**
 * <pre>
 * Responsible for building {@link Argument} instances.
 * Example builders can be created through the {@link ArgumentFactory}.
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
	private int parameterArity = 1;

	// Members that uses the T type, think about
	// ListArgumentBuilder#copyAsListBuilder() when adding new ones
	@Nullable private Supplier<T> defaultValueSupplier = null;
	@Nullable private Describer<T> defaultValueDescriber = null;
	@Nonnull private Function<T, T> finalizer = Functions.identity();
	@Nonnull private Predicate<T> limiter = Predicates.alwaysTrue();

	@Nullable private final InternalStringParser<T> internalStringParser;

	@Nonnull private final SELF_TYPE myself;

	/**
	 * Creates a default {@link ArgumentBuilder} that produces arguments that:
	 * <ul>
	 * <li>doesn't have any {@link #names(String...)}</li>
	 * <li>has an empty {@link #description(String)}</li>
	 * <li>isn't {@link #required()}</li>
	 * <li>uses {@link StringParser#defaultValue()} on {@link #parser()} to produce default values</li>
	 * <li>uses {@link Object#toString()} to describe the default value</li>
	 * <li>uses {@link StringParser#metaDescription()} on {@link #parser()} to produce meta
	 * descriptions</li>
	 * <li>doesn't have any {@link #limitTo(Predicate)} set</li>
	 * </ul>
	 * Typically called implicitly by subclasses.
	 */
	protected ArgumentBuilder()
	{
		this.internalStringParser = null;
		myself = self();
	}

	ArgumentBuilder(final InternalStringParser<T> stringParser)
	{
		this.internalStringParser = stringParser;
		myself = self();
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
	 *         {@link CommandLineParser#withArguments(Argument...)} <br>
	 *         When the parsing is done the parsed value for this
	 *         argument can be fetched with {@link ParsedArguments#get(Argument)}.
	 * @throws IllegalStateException if it's not possible to construct an {@link Argument} with the
	 *             current settings
	 */
	@CheckReturnValue
	@Nonnull
	@OverridingMethodsMustInvokeSuper
	public Argument<T> build()
	{
		return new Argument<T>(this);
	}

	/**
	 * Parses command line arguments and returns the value of the argument built
	 * by {@link #build()}.<br>
	 * <br>
	 * This is a shorthand method that should be used if only one {@link Argument} is expected as it
	 * will throw if unexpected arguments are
	 * encountered. If several arguments are expected use
	 * {@link CommandLineParser#withArguments(Argument...)} instead.
	 * 
	 * @param actualArguments the arguments from the command line
	 * @return the parsed value from the {@code actualArguments}
	 * @throws ArgumentException if actualArguments isn't compatible with this
	 *             argument
	 */
	@Nullable
	public final T parse(String ... actualArguments) throws ArgumentException
	{
		return build().parse(actualArguments);
	}

	/**
	 * Returns a usage string for the argument that this builder builds.
	 * Should only be used if one argument is supported,
	 * otherwise the {@link CommandLineParser#usage(String)} method should be used instead.
	 */
	public final String usage(String programName)
	{
		return build().usage(programName);
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
	InternalStringParser<T> internalParser()
	{
		StringParser<T> parser = parser();
		if(parser != InternalArgumentBuilder.MARKER)
			return new StringParserBridge<T>(parser);
		return internalStringParser;
	}

	/**
	 * Specifies the argument names that triggers the argument being built. As commands sometimes
	 * gets long and hard to understand it's recommended to also support long named arguments,
	 * making the commands even longer but more readable instead <br>
	 * <b>Note</b>: If you choice to use multiple {@link #required()} indexed arguments all of them
	 * must have unique {@link #metaDescription(String)}s. This ensures that error messages better
	 * can point out erroneous arguments
	 * 
	 * @param argumentNames <ul>
	 *            <li>"-o" for a short named option/argument</li>
	 *            <li>"--option-name" for a long named option/argument</li>
	 *            <li>"-o", "--option-name" to give the user both choices</li>
	 *            <li>zero elements: makes this an indexed argument, meaning that the argument must
	 *            be given at the same position on the command line as it is given to
	 *            {@link CommandLineParser#withArguments(Argument...)} (not counting named
	 *            arguments).</li>
	 *            </ul>
	 * @return this builder
	 */
	public SELF_TYPE names(final String ... argumentNames)
	{
		names = copyOf(argumentNames);
		return myself;
	}

	/**
	 * Works just like {@link #names(String...)} but it takes an {@link Iterable} instead.
	 * 
	 * @param argumentNames the list to use as argument names
	 * @return this builder
	 */
	public SELF_TYPE names(final Iterable<String> argumentNames)
	{
		names = copyOf(argumentNames);
		return myself;
	}

	/**
	 * Ignores the case of the argument names set by {@link ArgumentBuilder#names(String...)}
	 * 
	 * @return this builder
	 */
	public final SELF_TYPE ignoreCase()
	{
		ignoreCase = true;
		return myself;
	}

	/**
	 * TODO: support resource bundles with i18n texts
	 */

	/**
	 * Provides a description of what this argument does/means
	 * 
	 * @param descriptionString the description to use
	 * @return this builder
	 */
	public final SELF_TYPE description(final String descriptionString)
	{
		description = withString(descriptionString);
		return myself;
	}

	/**
	 * Provides a description of what this argument does/means
	 * 
	 * @param aDescription
	 * @return this builder
	 */
	public final SELF_TYPE description(final Description aDescription)
	{
		description = aDescription;
		return myself;
	}

	/**
	 * <pre>
	 * Makes {@link CommandLineParser#parse(String...)} throw
	 * {@link ArgumentException} if this argument isn't given.
	 * It's however preferred to use {@link #defaultValue(Object)} instead.
	 * 
	 * The {@link Argument#toString()} will be used to print each missing argument.
	 * 
	 * @return this builder
	 * @throws IllegalStateException if {@link #defaultValue(Object)} (or
	 *             {@link #defaultValueSupplier(Supplier)}) has been
	 *             called, because these two methods are mutually exclusive
	 * </pre>
	 */
	public SELF_TYPE required()
	{
		checkState(defaultValueSupplier == null, Texts.DEFAULT_VALUE_AND_REQUIRED);
		required = true;
		return myself;
	}

	/**
	 * <pre>
	 * Sets a default value to use for this argument.
	 * Returned by {@link ParsedArguments#get(Argument)} when no argument was given.
	 * To create default values lazily see {@link ArgumentBuilder#defaultValueSupplier(Supplier)}.
	 * 
	 * <b>Mutability</b>:Remember that as {@link Argument} is {@link Immutable}
	 * this value should be so too if multiple argument parsings is going to take place.
	 * If mutability is wanted {@link ArgumentBuilder#defaultValueSupplier(Supplier)} should be used instead.
	 * 
	 * @return this builder
	 * @throws IllegalStateException if {@link #required()} has been called,
	 * because these two methods are mutually exclusive
	 */
	public SELF_TYPE defaultValue(@Nullable final T value)
	{
		checkState(!required, Texts.DEFAULT_VALUE_AND_REQUIRED);
		defaultValueSupplier = Suppliers.ofInstance(value);
		return myself;
	}

	/**
	 * <pre>
	 * Sets a supplier that can supply default values in the absence of this argument
	 * 
	 * Even if {@link #limitTo(Predicate)} is used, the {@link Supplier#get()} isn't called
	 * until the default value is actually needed ({@link ParsedArguments#get(Argument)}. If the
	 * default value is deemed non-allowed at that point an {@link IllegalStateException} is thrown.
	 * 
	 * <b>Note:</b>May be removed in the future if Guava is removed as a dependency
	 * 
	 * Wrap your supplier with {@link Suppliers#memoize(Supplier)} if you want to cache created values.
	 * 
	 * @return this builder
	 * @throws IllegalStateException if {@link #required()} has been called,
	 * because these two methods are mutually exclusive
	 */
	@Beta
	public final SELF_TYPE defaultValueSupplier(final Supplier<T> aDefaultValueSupplier)
	{
		checkState(!required, Texts.DEFAULT_VALUE_AND_REQUIRED);
		defaultValueSupplier = aDefaultValueSupplier;
		return myself;
	}

	/**
	 * Provides a way to give the usage texts a better explanation of a default
	 * value than {@link Object#toString()} provides. Always prints {@code aDescription} regardless
	 * of what the default value is.
	 * 
	 * @param aDescription the description
	 * @return this builder
	 * @see Describers#withConstantString(String)
	 */
	public final SELF_TYPE defaultValueDescription(final String aDescription)
	{
		this.defaultValueDescriber = Describers.withConstantString(aDescription);
		return myself;
	}

	/**
	 * {@link Describer} version of {@link #defaultValueDescription(String)}
	 * 
	 * @param describer a describer
	 * @return this builder
	 */
	public final SELF_TYPE defaultValueDescriber(final Describer<T> describer)
	{
		this.defaultValueDescriber = describer;
		return myself;
	}

	/**
	 * <pre>
	 * By default {@link StringParser}s provides a meta description (by implementing {@link StringParser#metaDescription()}
	 * that describes the type of data they expect. For instance, if you're writing a music player,
	 * a user of your application would (most probably) rather see:
	 * 
	 * --track-nr <track nr>    The track number to play (using {@code  metaDescription("<track nr>") })
	 * 
	 * instead of
	 * 
	 * --track-nr <integer>     The track number to play (the default provided by {@link StringParser#metaDescription()})
	 * 
	 * So when using general data type parsers such as {@link StringParsers#integerParser()} you're better of if
	 * you provide a meta description that explains what the {@code integer} represents in the context of this argument.
	 * 
	 * <b>Note:</b> empty meta descriptions aren't allowed
	 * <b>Note:</b> the surrounding < & > isn't enforced or added automatically but it's
	 * preferred to have them because a space can easily be mistaken for something else
	 * 
	 * @param aMetaDescription "<track nr>" in the above example
	 * @return this builder
	 * @throws IllegalArgumentException if aMetaDescription is empty or null
	 * </pre>
	 */
	public final SELF_TYPE metaDescription(final String aMetaDescription)
	{
		checkArgument(!isNullOrEmpty(aMetaDescription), Texts.INVALID_META_DESCRIPTION);
		this.metaDescription = aMetaDescription;
		return myself;
	}

	/**
	 * Hides this argument so that it's not displayed in the usage texts.<br>
	 * It's recommended that hidden arguments have a reasonable {@link #defaultValue(Object)} and
	 * aren't {@link #required()}, in fact this is recommended for all arguments.
	 * 
	 * @return this builder
	 */
	public final SELF_TYPE hideFromUsage()
	{
		this.hideFromUsage = true;
		return myself;
	}

	/**
	 * For instance, "=" in --author=jjonsson
	 * 
	 * @param aSeparator the character that separates the argument name and
	 *            argument value, defaults to a space
	 * @return this builder
	 */
	public SELF_TYPE separator(final String aSeparator)
	{
		separator = aSeparator;
		return myself;
	}

	/**
	 * <pre>
	 * Limits values parsed so that they conform to some specific rule.
	 * Only values for which {@link Predicate#apply(Object)} returns true, will be accepted.
	 * Other values will cause an {@link ArgumentException}.
	 * For example {@link Ranges#closed(Comparable, Comparable)} only allows values within a range.
	 * 
	 * To override the default error message that is generated with {@link Texts#UNALLOWED_VALUE}
	 * you can throw {@link IllegalArgumentException} from {@link Predicate#apply(Object)}. The detail
	 * message of that exception will be used by {@link ArgumentException#getMessageAndUsage(String)}.
	 * When this is needed it's generally recommended to write a parser of its own instead.
	 * 
	 * <b>Note:</b>{@link Object#toString()} on {@code aLimiter} will
	 * replace {@link StringParser#descriptionOfValidValues()} in the usage
	 * 
	 * <b>Note:</b>The validity of {@link StringParser#defaultValue()} isn't checked until
	 * it's actually needed when {@link ParsedArguments#get(Argument)} is called. This is so
	 * because {@link StringParser#defaultValue()} could take an arbitrary long time.
	 * </pre>
	 * 
	 * @param aLimiter a limiter
	 * @return this builder
	 */
	@Beta
	public final SELF_TYPE limitTo(Predicate<T> aLimiter)
	{
		limiter = aLimiter;
		return myself;
	}

	/**
	 * <pre>
	 * Makes this argument handle properties like arguments:
	 * -Dproperty_name=value
	 * where "-D" is the string supplied to {@link #names(String...)},
	 * "value" is decoded by the previously set {@link StringParser}.
	 * "property_name" is the {@link String} key in the resulting {@link Map}
	 * 
	 * Tip: You can pass {@link #defaultValueDescriber(Describer)} a
	 * {@link Describers#mapDescriber(Map)} to describe each property that you
	 * support (given that you've used {@link ArgumentBuilder#defaultValue(Object)}
	 * to use sane defaults for your properties).
	 * 
	 * @return a new (more specific) builder
	 * @throws IllegalStateException if a {@link #defaultValueDescriber(Describer)} or
	 *             {@link #defaultValue(Object)} has been set as they have no place in a property
	 *             map.
	 * @see #asKeyValuesWithKeyParser(StringParser) to use other types as keys than
	 * {@link String}s
	 */
	@CheckReturnValue
	public final MapArgumentBuilder<String, T> asPropertyMap()
	{
		checkState(defaultValueSupplier == null, Texts.INVALID_CALL_ORDER, "defaultValue", "asPropertyMap");
		checkState(defaultValueDescriber == null, Texts.INVALID_CALL_ORDER, "defaultValueDescriber", "asPropertyMap");
		return new MapArgumentBuilder<String, T>(this, stringParser());
	}

	/**
	 * <pre>
	 * Makes this argument handle properties like arguments:
	 * -Dproperty_name=value
	 * where "-D" is one of the strings supplied to {@link #names(String...)},
	 * "property_name" is decoded by {@code keyParser} and
	 * "value" is decoded by the {@link StringParser} previously passed to the constructor.
	 * 
	 * For example:
	 * {@code
	 * Map<Integer, Integer> numberMap = ArgumentFactory.integerArgument("-N")
	 * 						.asKeyValuesWithKeyParser(StringParsers.integerParser())
	 * 						.parse("-N1=5", "-N2=10");
	 * assertThat(numberMap.get(1)).isEqualTo(5);
	 * }
	 * 
	 * For this to work correctly it's paramount that {@code Key} implements a
	 * proper {@link Object#hashCode()} because it's going to be used a key in a {@link Map}.
	 * </pre>
	 * 
	 * @return a new (more specific) builder
	 * @throws IllegalStateException if a {@link #defaultValueDescriber(Describer)} or
	 *             {@link #defaultValue(Object)} has been set as they have no place in a property
	 *             map.
	 */
	@CheckReturnValue
	public final <Key> MapArgumentBuilder<Key, T> asKeyValuesWithKeyParser(StringParser<Key> keyParser)
	{
		checkState(defaultValueSupplier == null, Texts.INVALID_CALL_ORDER, "defaultValue", "asKeyValuesWithKeyParser");
		checkState(defaultValueDescriber == null, Texts.INVALID_CALL_ORDER, "defaultValueDescriber", "asKeyValuesWithKeyParser");
		return new MapArgumentBuilder<Key, T>(this, keyParser);
	}

	/**
	 * <pre>
	 * When given a "," this allows for
	 * arguments such as:
	 * -numbers 1,2,3
	 * where the resulting {@code List<Integer>} would contain 1, 2 & 3.
	 * 
	 * Doesn't allow empty lists.
	 * </pre>
	 * 
	 * @param valueSeparator the string to split the input with
	 * @return a new (more specific) builder
	 */
	@CheckReturnValue
	public SplitterArgumentBuilder<T> splitWith(final String valueSeparator)
	{
		return new SplitterArgumentBuilder<T>(this, valueSeparator);
	}

	/**
	 * <pre>
	 * Useful for handling a variable amount of parameters in the end of a
	 * command.
	 * Uses this argument to parse values but assumes that all the following
	 * parameters are of the same type, integer in the following example:
	 * {@code
	 * String[] threeArgs = {"--numbers", "1", "2", "3"};
	 * List<Integer> numbers = integerArgument("--numbers").variableArity().parse(threeArgs);
	 * assertThat(numbers).isEqualTo(asList(1, 2, 3));
	 * 
	 * String[] twoArgs = {"--numbers", "1", "2"};
	 * List<Integer> numbers = integerArgument("--numbers").variableArity().parse(twoArgs);
	 * assertThat(numbers).isEqualTo(asList(1, 2));
	 * }
	 * </pre>
	 * 
	 * @return a new (more specific) builder
	 */
	@CheckReturnValue
	public ArityArgumentBuilder<T> variableArity()
	{
		return new ArityArgumentBuilder<T>(this);
	}

	/**
	 * <pre>
	 * Uses this argument to parse values but assumes that {@code numberOfParameters} of the
	 * following parameters are of the same type,integer in the following example:
	 * {@code
	 * String[] args = {"--numbers", "1", "2"};
	 * List<Integer> numbers = integerArgument("--numbers").arity(2).parse(args);
	 * assertThat(numbers).isEqualTo(asList(1, 2));
	 * }
	 * <b>Note:</b>If the argument isn't {@link #required()} the default value
	 * will be a list that contains {@code numberOfParameters} elements
	 * of {@link StringParser#defaultValue()}, in the above example that would be two zeros.
	 * If this isn't wanted use {@link #defaultValue(Object)} to override it.
	 * 
	 * @return a new (more specific) builder
	 * </pre>
	 */
	@CheckReturnValue
	public ArityArgumentBuilder<T> arity(final int numberOfParameters)
	{
		checkArgument(numberOfParameters > 1, Texts.TO_SMALL_ARITY, numberOfParameters);
		return new ArityArgumentBuilder<T>(this, numberOfParameters);
	}

	/**
	 * <pre>
	 * Makes it possible to enter several values for the same argument. Such as this:
	 * 
	 * TODO: make all code examples prettified:)
	 * 
	 * <pre class="prettyprint">
	 * <code class="language-java">
	 * String[] arguments = {"--number", "1", "--number", "2"};
	 * List&lt;Integer&gt; numbers = integerArgument("--number").repeated().parse(arguments);
	 * assertThat(numbers).isEqualTo(Arrays.asList(1, 2));
	 * </code>
	 * </pre>
	 * 
	 * If you want to combine {@link #repeated()} with a specific {@link #arity(int)} then call
	 * {@link #arity(int)} before calling this. {@code String[] arguments = "--numbers", "1", "2",
	 * "--numbers", "3", "4"};
	 * List<List<Integer>> numberLists =
	 * integerArgument("--numbers").arity(2).repeated().parse(arguments);
	 * assertThat(numberLists).isEqualTo(asList(asList(1, 2), asList(3, 4)));
	 * }
	 * For repeated values in a property map such as this: {@code String[] arguments =
	 * "-Nnumber=1", "-Nnumber=2"};
	 * Map<String, List<Integer>> numberMap =
	 * integerArgument("-N").repeated().asPropertyMap().parse(arguments);
	 * assertThat(numberMap.get("number")).isEqualTo(Arrays.asList(1, 2));
	 * } {@link #repeated()} should be called before {@link #asPropertyMap()}.
	 * For arguments without a name use {@link #variableArity()} instead.
	 * 
	 * @return a new (more specific) builder
	 *         </pre>
	 */
	@CheckReturnValue
	public RepeatedArgumentBuilder<T> repeated()
	{
		return new RepeatedArgumentBuilder<T>(this);
	}

	@Override
	public String toString()
	{
		return new Argument<T>(this).toString();
	}

	/**
	 * <pre>
	 * Copies all values from the given copy into this one, except for:
	 * {@link #parser()}, {@link #defaultValueSupplier()} & {@link #defaultValueDescriber()}
	 * as they may change between different builders
	 * (e.g the default value for Argument<Boolean> and Argument<List<Boolean>> are not compatible)
	 * @param copy the ArgumentBuilder to copy from
	 */
	@OverridingMethodsMustInvokeSuper
	protected void copy(final ArgumentBuilder<?, ?> copy)
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

	/**
	 * <pre>
	 * {@code aFinalizer} is called after {@link StringParser#parse(String)}
	 * but before any predicates given to {@link #limitTo(Predicate)} are tested.
	 * 
	 * Is used internally to finalize values produced by {@link StringParser#parse(String)}.
	 * 
	 * For example {@link RepeatedArgumentParser} uses this to make the resulting {@link List} {@link Immutable}.
	 * 
	 * For regular {@link StringParser}s it's recommended to use {@link SimpleForwardingStringParser}
	 * and decorate your {@link StringParser} with any finalization there instead.
	 * 
	 * <b>Note:</b> If {@link #finalizeWith(Function)} have been called before,
	 * the given {@code aFinalizer} will be run after that finalizer.
	 * 
	 * </pre>
	 * 
	 * @param aFinalizer a finalizer
	 * @return this builder
	 */
	final SELF_TYPE finalizeWith(Function<T, T> aFinalizer)
	{
		finalizer = Functions2.compound(finalizer, aFinalizer);
		return myself;
	}

	/**
	 * <pre>
	 * A mechanism for finalizing parsed values before they enter a {@link ParsedArguments} instance.
	 * 
	 * It's used by internal string parsers that supports repeated values, those need to wrap
	 * lists, maps etc. in unmodifiable ones before the parsing is said to be complete.
	 * 
	 * For regular {@link StringParser}s it's recommended to use {@link SimpleForwardingStringParser}
	 * and decorate your {@link StringParser} with any finalization there instead.
	 * 
	 * @param <T> the type of value to finalize
	 * </pre>
	 */

	final void allowRepeatedArguments()
	{
		isAllowedToRepeat = true;
	}

	final void setAsPropertyMap()
	{
		isPropertyMap = true;
	}

	final void setParameterArity(int arity)
	{
		parameterArity = arity;
	}

	/**
	 * @formatter.off
	 */
	@Nonnull final List<String> names(){ return names; }

	@Nullable final Describer<T> defaultValueDescriber(){ return defaultValueDescriber; }

	@Nonnull Description description(){ return description; }

	final boolean isRequired(){ return required; }

	@Nullable final String separator(){ return separator; }

	final boolean isIgnoringCase(){ return ignoreCase; }

	final boolean isPropertyMap(){ return isPropertyMap; }

	final int parameterArity(){ return parameterArity; }

	final boolean isAllowedToRepeat(){ return isAllowedToRepeat; }

	@Nullable final String metaDescription(){ return metaDescription; }
	final boolean isHiddenFromUsage(){ return hideFromUsage; }

	@Nullable final Supplier<T> defaultValueSupplier(){ return defaultValueSupplier; }

	@Nonnull final Function<T, T> finalizer(){ return finalizer; }
	@Nonnull final Predicate<T> limiter(){ return limiter; }

	/**
	 * @formatter.on
	 */

	/**
	 * A very simple version of an {@link ArgumentBuilder}, it's exposed mainly to
	 * lessen the exposure of the {@code SELF_TYPE} argument of the {@link ArgumentBuilder}.
	 */
	@NotThreadSafe
	public static final class DefaultArgumentBuilder<T> extends ArgumentBuilder<DefaultArgumentBuilder<T>, T>
	{
		private final StringParser<T> parser;

		DefaultArgumentBuilder(final StringParser<T> aParser)
		{
			parser = aParser;
		}

		@Override
		protected StringParser<T> parser()
		{
			return parser;
		}
	}

	/**
	 * Builder for {@link Command}s. Created with {@link ArgumentFactory#command(Command)}.
	 */
	@NotThreadSafe
	public static final class CommandBuilder extends InternalArgumentBuilder<CommandBuilder, String>
	{
		CommandBuilder(final Command command)
		{
			super(command);
		}
		// description, hideFromUsage, ignoreCase, metaDescription

		// TODO: these shouldn't be available for commands...
		// limitTo, defaultValue, defaultValueSupplier, defaultValueDescription,
		// required, splitWith, arity, variableArity

	}

	// Non-Interesting builders below, most declarations under here handles
	// (by deprecating) invalid invariants between different argument properties

	private static class InternalArgumentBuilder<Builder extends InternalArgumentBuilder<Builder, T>, T> extends ArgumentBuilder<Builder, T>
	{
		/**
		 * Only used to flag that this builder is an internal one, not used for parsing
		 */
		static final StringParser<?> MARKER = new StringParserBridge<String>(StringParsers.stringParser());

		InternalArgumentBuilder()
		{

		}

		InternalArgumentBuilder(InternalStringParser<T> parser)
		{
			super(parser);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected StringParser<T> parser()
		{
			return (StringParser<T>) MARKER;
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
			finalizeWith(listTransformer(builder.finalizer));
			finalizeWith(Functions2.<T>unmodifiableList());

			limitTo(listPredicate(builder.limiter));
			if(builder.defaultValueSupplier != null)
			{
				defaultValueSupplier(Suppliers2.ofRepeatedElements(builder.defaultValueSupplier, nrOfElementsInDefaultValue));
			}
			if(builder.defaultValueDescriber != null)
			{
				defaultValueDescriber(Describers.listDescriber(builder.defaultValueDescriber));
			}
		};
	}

	/**
	 * An intermediate builder used by {@link #splitWith(String)}. It's mainly used to switch the T
	 * argument of the previous builder to List<T> and to indicate invalid call orders.
	 */
	@NotThreadSafe
	public static final class ArityArgumentBuilder<T> extends ListArgumentBuilder<ArityArgumentBuilder<T>, T>
	{
		private ArityArgumentBuilder(final ArgumentBuilder<? extends ArgumentBuilder<?, T>, T> builder, final int arity)
		{
			super(new FixedArityParser<T>(builder.internalParser(), arity));
			setParameterArity(arity);
			init(builder, arity);
		}

		private ArityArgumentBuilder(final ArgumentBuilder<? extends ArgumentBuilder<?, T>, T> builder)
		{
			super(new VariableArityParser<T>(builder.internalParser()));
			setParameterArity(VARIABLE_ARITY);
			init(builder, 1);
		}

		private void init(final ArgumentBuilder<? extends ArgumentBuilder<?, T>, T> builder, int nrOfElementsInDefaultValue)
		{
			copy(builder);
			copyAsListBuilder(builder, nrOfElementsInDefaultValue);
		}

		/**
		 * @deprecated
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

	/**
	 * An intermediate builder used by {@link #repeated()}. It's mainly used to switch the T
	 * argument of the previous builder to List<T> and to indicate invalid call orders.
	 */
	@NotThreadSafe
	public static final class RepeatedArgumentBuilder<T> extends ListArgumentBuilder<RepeatedArgumentBuilder<T>, T>
	{
		private RepeatedArgumentBuilder(final ArgumentBuilder<? extends ArgumentBuilder<?, T>, T> builder)
		{
			super(new RepeatedArgumentParser<T>(builder.internalParser()));
			copy(builder);
			copyAsListBuilder(builder, 1);
			allowRepeatedArguments();
		}

		/**
		 * @deprecated this method should be called before {@link #repeated()}
		 */
		@Deprecated
		@Override
		public ArityArgumentBuilder<List<T>> arity(final int numberOfParameters)
		{
			throw new IllegalStateException("Programmer Error. Call arity(...) before repeated()");
		}

		/**
		 * @deprecated this method should be called before {@link #repeated()}
		 */
		@Override
		@Deprecated
		public ArityArgumentBuilder<List<T>> variableArity()
		{
			throw new IllegalStateException("Programmer Error. Call variableArity(...) before repeated()");
		}

		/**
		 * @deprecated call {@link #splitWith(String)} before {@link #repeated()}
		 */
		@Deprecated
		@Override
		public SplitterArgumentBuilder<List<T>> splitWith(final String valueSeparator)
		{
			throw new IllegalStateException("call splitWith(String) before repeated()");
		}
	}

	/**
	 * An intermediate builder used by {@link ArgumentFactory#optionArgument(String, String...)} to
	 * indicate invalid call orders
	 */
	@NotThreadSafe
	public static final class OptionArgumentBuilder extends InternalArgumentBuilder<OptionArgumentBuilder, Boolean>
	{
		OptionArgumentBuilder()
		{
			defaultValue(false);
			setParameterArity(OptionParser.NO_ARGUMENTS);
		}

		// TODO: if withAction(Action action) isn't added then maybe at least add it here?

		@Override
		InternalStringParser<Boolean> internalParser()
		{
			return optionParser(defaultValueSupplier().get());
		}

		@Override
		public OptionArgumentBuilder defaultValue(Boolean value)
		{
			checkNotNull(value, Texts.OPTION_DOES_NOT_ALLOW_NULL_AS_DEFAULT);
			return super.defaultValue(value);
		}

		@Override
		public OptionArgumentBuilder names(Iterable<String> argumentNames)
		{
			checkArgument(!isEmpty(argumentNames), Texts.OPTIONS_REQUIRES_AT_LEAST_ONE_NAME);
			return super.names(argumentNames);
		}

		@Override
		public OptionArgumentBuilder names(String ... argumentNames)
		{
			checkArgument(argumentNames.length >= 1, Texts.OPTIONS_REQUIRES_AT_LEAST_ONE_NAME);
			return super.names(argumentNames);
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

	/**
	 * An intermediate builder used by {@link #asPropertyMap()}. It's mainly used to switch the T
	 * argument of the previous builder to Map<K, T> and to indicate invalid call orders.
	 */
	@NotThreadSafe
	public static final class MapArgumentBuilder<K, V> extends InternalArgumentBuilder<MapArgumentBuilder<K, V>, Map<K, V>>
	{
		private MapArgumentBuilder(final ArgumentBuilder<?, V> builder, StringParser<K> keyParser)
		{
			super(new KeyValueParser<K, V>(keyParser, builder.internalParser(), builder.limiter));
			copy(builder);

			// TODO: should the state be verified just before the argument is built?
			checkState(names().size() > 0, Texts.NO_NAME_FOR_PROPERTY_MAP);
			if(separator() != null)
			{
				checkState(separator().length() > 0, Texts.EMPTY_SEPARATOR);
			}
			else
			{
				separator("=");
			}

			finalizeWith(Functions2.<K, V>mapValueTransformer(builder.finalizer));
			finalizeWith(Functions2.<K, V>unmodifiableMap());

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

	/**
	 * An intermediate builder used by {@link #splitWith(String)}. It's mainly used to switch the T
	 * argument of the previous builder to List<T> and to indicate invalid call orders.
	 */
	@NotThreadSafe
	public static final class SplitterArgumentBuilder<T> extends ListArgumentBuilder<SplitterArgumentBuilder<T>, T>
	{
		private SplitterArgumentBuilder(final ArgumentBuilder<?, T> builder, final String valueSeparator)
		{
			super(new StringSplitterParser<T>(valueSeparator, builder.internalParser()));
			copy(builder);
			copyAsListBuilder(builder, 1);
		}

		/**
		 * @deprecated you can't use both {@link #splitWith(String)} and {@link #arity(int)}
		 */
		@Deprecated
		@Override
		public ArityArgumentBuilder<List<T>> arity(final int numberOfParameters)
		{
			throw new IllegalStateException("You can't use both splitWith and arity");
		}

		/**
		 * @deprecated you can't use both {@link #splitWith(String)} and {@link #variableArity()}
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
	abstract static class ArgumentSettings implements Comparable<ArgumentSettings>
	{
		@Nonnull
		abstract List<String> names();

		@Nullable
		abstract String separator();

		abstract String metaDescriptionInRightColumn();

		abstract boolean isAllowedToRepeat();

		abstract boolean isRequired();

		abstract boolean isIgnoringCase();

		abstract boolean isPropertyMap();

		abstract boolean isHiddenFromUsage();

		abstract int parameterArity();

		boolean isIndexed()
		{
			return names().isEmpty();
		}

		/**
		 * Compares {@link Argument}s by their {@link Argument#toString()}, ignoring case.
		 */
		@Override
		public int compareTo(ArgumentSettings other)
		{
			// TODO: what about locale-sensitive ordering?
			return toString().compareToIgnoreCase(other.toString());
		}

		// Predicates

		static final Predicate<ArgumentSettings> IS_VISIBLE = new Predicate<ArgumentSettings>(){
			@Override
			public boolean apply(ArgumentSettings input)
			{
				return !input.isHiddenFromUsage();
			}
		};

		static final Predicate<ArgumentSettings> IS_NAMED = new Predicate<ArgumentSettings>(){
			@Override
			public boolean apply(ArgumentSettings input)
			{
				return !input.isIndexed();
			}
		};

		static final Predicate<ArgumentSettings> IS_REQUIRED = new Predicate<ArgumentSettings>(){
			@Override
			public boolean apply(ArgumentSettings input)
			{
				return input.isRequired();
			}
		};

		static final Predicate<ArgumentSettings> IS_OF_VARIABLE_ARITY = new Predicate<ArgumentSettings>(){
			@Override
			public boolean apply(ArgumentSettings input)
			{
				return input.parameterArity() == VARIABLE_ARITY;
			}
		};
	}
}
