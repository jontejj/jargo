package se.j4j.argumentparser;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static se.j4j.argumentparser.DefaultValueProviders.listWithOneValue;
import static se.j4j.argumentparser.DefaultValueProviders.nonLazyProvider;

import java.util.List;
import java.util.Map;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import se.j4j.argumentparser.ArgumentFactory.ArgumentSplitter;
import se.j4j.argumentparser.ArgumentFactory.ListArgument;
import se.j4j.argumentparser.ArgumentFactory.MapArgument;
import se.j4j.argumentparser.ArgumentFactory.OptionArgument;
import se.j4j.argumentparser.ArgumentFactory.RepeatedArgument;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.MissingRequiredArgumentException;

/**
 * <pre>
 * Responsible for building {@link Argument} instances.
 * Example builders can be seen in {@link ArgumentFactory}.
 * 
 * <b>Note:</b>Some methods needs to be called in a specific order 
 * (to make the generic type system produce the correct type) and to guide the 
 * caller, such orders are documented with {@link Deprecated}. If those warnings 
 * are ignored {@link IllegalStateException} will be thrown at the offending call.
 * 
 * @param <SELF_TYPE> the type of the subclass extending this class. 
 * 		Concept borrowed from: <a href="http://passion.forco.de/content/emulating-self-types-using-java-generics-simplify-fluent-api-implementation">Ansgar.Konermann's blog</a>
 * 		The pattern can also be called <a href="http://en.wikipedia.org/wiki/Curiously_recurring_template_pattern">Curiously recurring template pattern</a>
 * @param <T> the type of arguments the built {@link Argument} instance should handle, such as {@link Integer}
 * </pre>
 */
@NotThreadSafe
public abstract class ArgumentBuilder<SELF_TYPE extends ArgumentBuilder<SELF_TYPE, T>, T>
{
	@Nonnull protected List<String> names = emptyList();

	@Nullable protected String defaultValueDescription = null;
	@Nonnull protected String description = "";
	protected boolean required = false;
	@Nullable protected String separator = null;
	protected boolean ignoreCase = false;

	protected boolean isPropertyMap = false;
	protected boolean isAllowedToRepeat = false;
	@Nonnull protected String metaDescription = "";
	protected boolean hideFromUsage = false;

	@Nullable protected final ArgumentHandler<T> handler;

	@Nullable protected DefaultValueProvider<T> defaultValueProvider = null;
	// No Null Object instance because it would require an unnecessary put
	// operation
	@Nullable protected Finalizer<T> finalizer = null;
	@Nonnull protected Limiter<T> limiter = Limiters.noLimits();
	@Nonnull protected Callback<T> callback = Callbacks.noCallback();

	/**
	 * @param handler if null, you'll need to override {@link #handler()}, like
	 *            {@link OptionArgumentBuilder} does.
	 */
	protected ArgumentBuilder(@Nullable final ArgumentHandler<T> handler)
	{
		this.handler = handler;
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
	 *         {@link ArgumentParser#forArguments(Argument...)}
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
		return build().parse(actualArguments);
	}

	public String usage(@Nonnull String programName)
	{
		return ArgumentParser.forArguments(build()).usage(programName);
	}

	/**
	 * Can be overridden by builders that support custom parameters that affects
	 * the handler
	 * 
	 * @return the {@link ArgumentHandler} that performs the actual parsing of
	 *         an argument value
	 */
	@Nonnull
	protected ArgumentHandler<T> handler()
	{
		return handler;
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
	 *            <li>zero elements: the argument must be given at the same
	 *            position on the command line as it is given to
	 *            {@link ArgumentParser#forArguments(Argument...)} (not counting
	 *            named arguments) which is discouraged because it makes your
	 *            program arguments harder to read and makes your program less
	 *            maintainable and harder to keep backwards compatible with old
	 *            scripts as you can't change the order of the arguments without
	 *            changing those scripts.</li>
	 *            </ul>
	 * @return this builder
	 */
	public SELF_TYPE names(@Nonnull final String ... argumentNames)
	{
		names = asList(argumentNames);
		return self();
	}

	/**
	 * If used {@link ArgumentParser#parse(String...)} ignores the case of the
	 * argument names set by {@link Argument#Argument(String...)}
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
	 * TODO: add {@link Description} methods for all string related methods
	 * 
	 * @param aDescription
	 * @return this builder
	 */
	public SELF_TYPE description(@Nonnull final String aDescription)
	{
		description = aDescription;
		return self();
	}

	/**
	 * Makes {@link ArgumentParser#parse(String...)} throw
	 * {@link MissingRequiredArgumentException} if this argument isn't given
	 * 
	 * @return this builder
	 * @throws IllegalStateException if {@link #defaultValue(Object)} has been
	 *             called, because these two methods are mutually exclusive
	 */
	public SELF_TYPE required()
	{
		if(defaultValueProvider != null)
			throw new IllegalStateException("Having a requried argument defaulting to some value (" + defaultValueProvider
					+ ") makes no sense. Remove the call to ArgumentBuilder#defaultValue(...) to use a required argument.");

		required = true;
		return self();
	}

	/**
	 * <pre>
	 * Sets a default value to use for this argument.
	 * Returned by {@link ParsedArguments#get(Argument)} when no argument was given.
	 * To create default values lazily see {@link ArgumentBuilder#defaultValueProvider(DefaultValueProvider)}.
	 * 
	 * <b>Mutability</b>:Remember that as {@link Argument} is {@link Immutable}
	 * this value should be so too if multiple argument parsings is going to take place.
	 * If mutability is wanted {@link ArgumentBuilder#defaultValueProvider(DefaultValueProvider)} should be used instead.
	 * 
	 * @return this builder
	 * @throws IllegalStateException if
	 * {@link #required()} has been called, because these two methods are mutually exclusive
	 */
	public SELF_TYPE defaultValue(@Nullable final T value)
	{
		if(required)
			throw new IllegalStateException("Having a requried argument defaulting to some value(" + value
					+ ") makes no sense. Remove the call to ArgumentBuilder#required to use a default value.");

		defaultValueProvider = nonLazyProvider(value);
		return self();
	}

	/**
	 * <pre>
	 * Sets a provider that can provide default values when this argument isn't given.
	 * @return this builder
	 * @throws IllegalStateException if
	 * {@link #required()} has been called, because these two methods are mutually exclusive
	 */
	public SELF_TYPE defaultValueProvider(@Nonnull final DefaultValueProvider<T> provider)
	{
		if(required)
			throw new IllegalStateException("Having a requried argument and a default value provider makes no sense. Remove the call to ArgumentBuilder#required to use a default value.");

		defaultValueProvider = provider;
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
		this.defaultValueDescription = aDescription;
		return self();
	}

	/**
	 * <pre>
	 * In this example "path" is the metaDescription
	 * -file &lt;path&gt; Path to some file
	 * </pre>
	 * 
	 * {@link ArgumentFactory} sets this by default for the standard types
	 * 
	 * @return this builder
	 */
	public SELF_TYPE metaDescription(@Nonnull final String aMetaDescription)
	{
		this.metaDescription = " <" + aMetaDescription + ">";
		return self();
	}

	/**
	 * Hidden arguments should not appear in usage.<br>
	 * Can be used for arguments that programmers use
	 * but users of the program doesn't.
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
	 *            argument value,
	 *            defaults to space
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
	 * -Dproperty.name=value
	 * where value is decoded by the previously set {@link ArgumentHandler}.
	 * property.name is the key in the resulting {@link Map},
	 * value is ... the value
	 * 
	 * </pre>
	 * 
	 * @return this builder wrapped in a more specific builder
	 */
	@CheckReturnValue
	public MapArgumentBuilder<T> asPropertyMap()
	{
		return new MapArgumentBuilder<T>(this);
	}

	/**
	 * <pre>
	 * When given a {@link StringSplitter} such as {@link StringSplitters#comma()} this allows for
	 * arguments such as:
	 * -numbers 1,2,3 
	 * where the resulting{@link List&lt;Integer&gt;} would contain 1, 2 & 3.
	 * 
	 * Doesn't allow empty lists.
	 * </pre>
	 * 
	 * @param splitter a splitter
	 * @return this builder wrapped in a more specific builder
	 */
	@CheckReturnValue
	public SplitterArgumentBuilder<T> splitWith(@Nonnull final StringSplitter splitter)
	{
		return new SplitterArgumentBuilder<T>(this, splitter);
	}

	/**
	 * Useful for handling a variable amount of parameters in the end of a
	 * command.
	 * Uses this argument to parse values but assumes that all the following
	 * parameters are of the same type, integer in the following example:
	 * 
	 * <pre>
	 * <code>
	 * ListArgument&lt;Integer&gt; numbers = ArgumentFactory.integerArgument("--numbers").consumeAll();
	 * 
	 * String[] threeArgs = {"--numbers", "1", "2", "3"};
	 * assertEqual(Arrays.asList(1, 2, 3), ArgumentParser.forArguments(numbers).parse(threeArgs).get(numbers));
	 * 
	 * String[] twoArgs = {"--numbers", "4", "5"};
	 * assertEqual(Arrays.asList(4, 5), ArgumentParser.forArguments(numbers).parse(twoArgs).get(numbers));
	 * </code>
	 * </pre>
	 * 
	 * @return this builder wrapped in a more specific builder
	 */
	@CheckReturnValue
	public ListArgumentBuilder<T> consumeAll()
	{
		return new ListArgumentBuilder<T>(this, ListArgument.CONSUME_ALL);
	}

	/**
	 * Uses this argument to parse values but assumes that
	 * <code>numberOfParameters</code> of the following
	 * parameters are of the same type,integer in the following example:
	 * 
	 * <pre>
	 * {@code
	 * String[] args = }{{@code "--numbers", "4", "5", "Hello"}};
	 * {@code
	 * Argument<List<Integer>> numbers = ArgumentFactory.integerArgument("--numbers").arity(2).build();
	 * List<Integer> actualNumbers = ArgumentParser.forArguments(numbers).parse(args).get(numbers);
	 * assertThat(actualNumbers).isEqualTo(Arrays.asList(4, 5));
	 * }
	 * </pre>
	 * 
	 * @return this builder wrapped in a more specific builder
	 */
	@CheckReturnValue
	public ListArgumentBuilder<T> arity(final int numberOfParameters)
	{
		return new ListArgumentBuilder<T>(this, numberOfParameters);
	}

	/**
	 * Makes it possible to enter several values for the same argument.
	 * Such as this:
	 * 
	 * <pre>
	 * <code>
	 * String[] args = {"--number", "1", "--number", "2"};
	 * RepeatedArgument&lt;Integer&gt; number = ArgumentFactory.integerArgument("--number").repeated();
	 * ParsedArguments parsed = ArgumentParser.forArguments(number).parse(args);
	 * assertEquals(Arrays.asList(1, 2), parsed.get(number));
	 * </code>
	 * </pre>
	 * 
	 * If you want to combine a specific {@link #arity(int)} or
	 * {@link #consumeAll()} then call those before calling this.
	 * 
	 * <pre>
	 * <code>
	 * String[] args = {"--numbers", "5", "6", "--numbers", "3", "4"};
	 * RepeatedArgument&lt;List&lt;Integer&gt;&gt; numbers = ArgumentFactory.integerArgument("--numbers").arity(2).repeated();
	 * ParsedArguments parsed = ArgumentParser.forArguments(numbers).parse(args);
	 * 
	 * List&lt;List&lt;Integer&gt;&gt; numberLists = new ArrayList<List<Integer>>();
	 * numberLists.add(Arrays.asList(5, 6));
	 * numberLists.add(Arrays.asList(3, 4));
	 * List&lt;List&lt;Integer&gt;&gt; actual = parsed.get(numbers);
	 * assertEqual("", numberLists, actual);
	 * </code>
	 * </pre>
	 * 
	 * For repeated values in a property map such as this:
	 * 
	 * <pre>
	 * <code>
	 * Argument&lt;Map&lt;String, List&lt;Integer&gt;&gt;&gt; numberMap = integerArgument("-N").repeated().asPropertyMap().build();
	 * ParsedArguments parsed = ArgumentParser.forArguments(numberMap).parse("-Nnumber=1", "-Nnumber=2");
	 * assertThat(parsed.get(numberMap).get("number")).isEqualTo(Arrays.asList(1, 2));
	 * </code>
	 * 
	 * {@link #repeated()} should be called before {@link #asPropertyMap()}.
	 * 
	 * For arguments without a name use {@link #consumeAll()} instead.
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
	 * {@link Finalizer}s are called after {@link ArgumentHandler#parse(java.util.ListIterator, Object, Argument)} 
	 * but before {@link Limiter#withinLimits(Object)}.
	 * 
	 * Can be used to modify the value produced by
	 * {@link ArgumentHandler#parse(java.util.ListIterator, Object, Argument)}.
	 * 
	 * For example {@link RepeatedArgument} uses this to make the resulting
	 * {@link List} {@link Immutable}.
	 * 
	 * <b>Note:</b> If {@link #finalizeWith(Finalizer)} have been 
	 * called before, the given <code>aFinalizer</code> will be run after that finalizer.
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
		finalizer = null;
		return self();
	}

	/**
	 * <pre>
	 * Limits values parsed so that they conform to some specific rule.
	 * For example {@link PositiveInteger} only allows positive integers.
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

	/**
	 * <pre>
	 * Can be used to perform some action when an argument value has been
	 * parsed.
	 * 
	 * <b>Note:</b> If {@link #callbackForValues(Callback)} has been called
	 * before for this builder the previous {@link Callback} will be called
	 * before this newly added one, i.e both will be called.
	 * 
	 * To clear out previously set {@link Callback}s call
	 * {@link #clearCallbacks()}.
	 * 
	 * </pre>
	 * 
	 * @param aCallback a listener for parsed values
	 * @return this builder
	 */
	public SELF_TYPE callbackForValues(@Nonnull Callback<T> aCallback)
	{
		callback = Callbacks.compound(callback, aCallback);
		return self();
	}

	/**
	 * Clear out all {@link Callback}s set by
	 * {@link #callbackForValues(Callback)}
	 * 
	 * @return this builder
	 */
	public SELF_TYPE clearCallbacks()
	{
		callback = Callbacks.noCallback();
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
	 * {@link ArgumentBuilder#handler} & {@link ArgumentBuilder#defaultValueProvider} as they may change between different builders
	 * (e.g the default value for Argument&lt;Boolean&gt; and Argument&lt;List&lt;Boolean&gt;&gt; are not compatible)
	 * @param copy the ArgumentBuilder to copy from
	 */
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
		this.defaultValueDescription = copy.defaultValueDescription;
	}

	// Non-Interesting builders below, most declarations here under handles
	// (by deprecating) invalid invariants between different argument properties

	public static class ListArgumentBuilder<T> extends ArgumentBuilder<ListArgumentBuilder<T>, List<T>>
	{
		public ListArgumentBuilder(@Nonnull final ArgumentBuilder<? extends ArgumentBuilder<?, T>, T> builder, final int argumentsToConsume)
		{
			super(new ListArgument<T>(builder.handler(), argumentsToConsume));
			copy(builder);
			this.finalizer = Finalizers.unmodifiableListFinalizer();
			this.finalizer = Finalizers.compound(Finalizers.forListValues(builder.finalizer), this.finalizer);

			callbackForValues(Callbacks.forListValues(builder.callback));
			limitTo(Limiters.forListValues(builder.limiter));
		}

		/**
		 * This doesn't work with {@link ArgumentBuilder#arity(int)} or
		 * {@link ArgumentBuilder#consumeAll()},
		 * either separate your arguments with a splitter or a space
		 */
		@Deprecated
		@Override
		public SplitterArgumentBuilder<List<T>> splitWith(final StringSplitter splitter)
		{
			throw new IllegalStateException("splitWith(...) doesn't work with arity/consumeAll()");
		}
	}

	public static class RepeatedArgumentBuilder<T> extends ArgumentBuilder<RepeatedArgumentBuilder<T>, List<T>>
	{
		public RepeatedArgumentBuilder(@Nonnull final ArgumentBuilder<? extends ArgumentBuilder<?, T>, T> builder)
		{
			super(new RepeatedArgument<T>(builder.handler()));
			copy(builder);
			if(builder.defaultValueProvider != null)
			{
				this.defaultValueProvider = listWithOneValue(builder.defaultValueProvider);
			}

			this.finalizer = Finalizers.unmodifiableListFinalizer();
			this.finalizer = Finalizers.compound(Finalizers.forListValues(builder.finalizer), this.finalizer);

			callbackForValues(Callbacks.forListValues(builder.callback));
			limitTo(Limiters.forListValues(builder.limiter));
			isAllowedToRepeat = true;
		}

		/**
		 * This method should be called before repeated()
		 */
		@Deprecated
		@Override
		public ListArgumentBuilder<List<T>> arity(final int numberOfParameters)
		{
			throw new IllegalStateException("Programmer Error. Call arity(...) before repeated()");
		}

		/**
		 * This method should be called before repeated()
		 */
		@Override
		@Deprecated
		public ListArgumentBuilder<List<T>> consumeAll()
		{
			throw new IllegalStateException("Programmer Error. Call consumeAll(...) before repeated()");
		}

		/**
		 * Call {@link #splitWith(StringSplitter)} before {@link #repeated()}
		 */
		@Deprecated
		@Override
		public SplitterArgumentBuilder<List<T>> splitWith(final StringSplitter splitter)
		{
			throw new IllegalStateException("call splitWith(Splitter) before repeated()");
		}
	}

	public static class OptionArgumentBuilder extends ArgumentBuilder<OptionArgumentBuilder, Boolean>
	{
		public OptionArgumentBuilder()
		{
			super(null);
			defaultValueProvider = nonLazyProvider(false);
		}

		@Override
		protected ArgumentHandler<Boolean> handler()
		{
			return new OptionArgument(defaultValueProvider.defaultValue());
		}

		// TODO: as these are starting to get out of hand, maybe introduce a
		// basic builder without any advanced stuff
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
			throw new IllegalStateException("A seperator for an optional flag isn't supported as " + "an optional flag can't be assigned a value");
		}

		/**
		 * @deprecated an optional flag can only have an arity of zero
		 */
		@Deprecated
		@Override
		public ListArgumentBuilder<Boolean> arity(final int numberOfParameters)
		{
			throw new IllegalStateException("An optional flag can't have any other arity than zero");
		}

		/**
		 * @deprecated an optional flag can only have an arity of zero
		 */
		@Deprecated
		@Override
		public ListArgumentBuilder<Boolean> consumeAll()
		{
			throw new IllegalStateException("An optional flag can't have any other arity than zero");
		}

		/**
		 * @deprecated an optional flag can't be split by anything
		 */
		@Deprecated
		@Override
		public SplitterArgumentBuilder<Boolean> splitWith(final StringSplitter splitter)
		{
			throw new IllegalStateException("An optional flag can't be split as it has no value");
		}
	}

	public static class MapArgumentBuilder<T> extends ArgumentBuilder<MapArgumentBuilder<T>, Map<String, T>>
	{
		protected MapArgumentBuilder(@Nonnull final ArgumentBuilder<?, T> builder)
		{
			super(new MapArgument<T>(builder.handler()));
			copy(builder);

			this.limiter = Limiters.forMapValues(builder.limiter);
			this.callback = Callbacks.forMapValues(builder.callback);
			this.finalizer = Finalizers.unmodifiableMapFinalizer();
			this.finalizer = Finalizers.compound(Finalizers.forMapValues(builder.finalizer), this.finalizer);

			if(this.separator == null)
			{
				// Maybe use assignment instead?
				this.separator = MapArgument.DEFAULT_SEPARATOR;
			}
			this.isPropertyMap = true;
			if(names.isEmpty())
				throw new IllegalStateException("No leading identifier (otherwise called names), for example -D, specified for property map. Call names(...) to provide it.");
		}

		/**
		 * @deprecated because {@link #repeated()} should be called before
		 *             {@link #asPropertyMap()}
		 */
		@Deprecated
		@Override
		public RepeatedArgumentBuilder<Map<String, T>> repeated()
		{
			throw new IllegalStateException("You'll need to call repeated before asPropertyMap");
		}

		/**
		 * @deprecated because {@link #splitWith(StringSplitter)} should be
		 *             called before {@link #asPropertyMap()}.
		 *             This is to make generic work it's magic and produce the
		 *             correct type, for example
		 *             {@code Map<String, List<Integer>>}.
		 */
		@Deprecated
		@Override
		public SplitterArgumentBuilder<Map<String, T>> splitWith(final StringSplitter splitter)
		{
			throw new IllegalStateException("You'll need to call splitWith before asPropertyMap");
		}
	}

	public static class SplitterArgumentBuilder<T> extends ArgumentBuilder<SplitterArgumentBuilder<T>, List<T>>
	{
		protected SplitterArgumentBuilder(@Nonnull final ArgumentBuilder<?, T> builder, @Nonnull final StringSplitter splitter)
		{
			super(new ArgumentSplitter<T>(splitter, builder.handler()));
			copy(builder);
			if(builder.defaultValueProvider != null)
			{
				this.defaultValueProvider = listWithOneValue(builder.defaultValueProvider);
			}
			this.finalizer = Finalizers.unmodifiableListFinalizer();
			this.finalizer = Finalizers.compound(Finalizers.forListValues(builder.finalizer), this.finalizer);
			callbackForValues(Callbacks.forListValues(builder.callback));
			limitTo(Limiters.forListValues(builder.limiter));
		}

		/**
		 * @deprecated you can't use both {@link #splitWith(StringSplitter)} and
		 *             {@link #arity(int)}
		 */
		@Deprecated
		@Override
		public ListArgumentBuilder<List<T>> arity(final int numberOfParameters)
		{
			throw new IllegalStateException("You can't use both splitWith and arity");
		}

		/**
		 * @deprecated you can't use both {@link #splitWith(StringSplitter)} and
		 *             {@link #consumeAll(int)}
		 */
		@Deprecated
		@Override
		public ListArgumentBuilder<List<T>> consumeAll()
		{
			throw new IllegalStateException("You can't use both splitWith and consumeAll");
		}
	}
}
