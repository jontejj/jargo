package se.j4j.argumentparser.builders;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import se.j4j.argumentparser.ArgumentHandler;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.exceptions.MissingRequiredArgumentException;
import se.j4j.argumentparser.handlers.OptionArgument;
import se.j4j.argumentparser.handlers.internal.ListArgument;
import se.j4j.argumentparser.handlers.internal.MapArgument;
import se.j4j.argumentparser.handlers.internal.RepeatedArgument;
import se.j4j.argumentparser.validators.ValueValidator;

/**
 *
 * @author Jonatan Jönsson <jontejj@gmail.com>
 *
 * @param <SELF_TYPE> the type of the subclass extending this class
 * 			<pre>Concept borrowed from: <a href="http://passion.forco.de/content/emulating-self-types-using-java-generics-simplify-fluent-api-implementation">Ansgar.Konermann's blog</a>
 * @param <T>
 */
public abstract class ArgumentBuilder<SELF_TYPE extends ArgumentBuilder<SELF_TYPE, T> ,T>
{
	List<String> names = Collections.emptyList();
	T defaultValue = null;
	String description = "";
	boolean required = false;
	String separator = null;
	boolean	ignoreCase = false;
	ValueValidator<T> validator;
	ArgumentHandler<T> handler;
	boolean	isPropertyMap;

	protected ArgumentBuilder(final ArgumentHandler<T> handler)
	{
		this.handler = handler;
	}

	@SuppressWarnings("unchecked") //This is passed in by subclasses as a type-variable, so type-safety is up to them
	private SELF_TYPE self()
	{
		return (SELF_TYPE)this;
	}

	/**
	 * Constructs an Immutable {@link Argument} which can be passed to {@link ArgumentParser#forArguments(Argument...)}
	 * @return
	 */
	public Argument<T> build()
	{
		//TODO: how to validate defaultValues
		return new Argument<T>(this);
	}

	protected SELF_TYPE handler(final ArgumentHandler<T> handler)
	{
		this.handler = handler;
		return self();
	}

	/**
	 * @param names <ul>
	 * 			<li>"-o" for a short named option/argument</li>
	 * 			<li>"--option-name" for a long named option/argument</li>
	 * 			<li>"-o", "--option-name" to give the user both choices</li>
	 *          <li>zero elements: the argument must be given at the same position on the command line as it is given to {@link ArgumentParser#forArguments(Argument...)} (not counting named arguments)
	 *            				which is discouraged because it makes your program arguments harder to read and makes your program less maintainable and
	 *            				harder to keep backwards compatible with old scripts as you can't change the order of the arguments without changing those scripts.</li></ul>
	 *
	 * As commands sometimes gets long and hard to understand it's recommended to also support long named arguments for the sake of readability.
	 *
	 * @return this builder
	 */
	public SELF_TYPE names(final String... names)
	{
		this.names = Arrays.asList(names);
		return self();
	}

	/**
	 * If used {@link ArgumentParser#parse(String...)} ignores the case of the argument names set by {@link Argument#Argument(String...)}
	 */
	public SELF_TYPE ignoreCase()
	{
		ignoreCase = true;
		return self();
	}

	/**
	 * TODO: support resource bundles with i18n texts
	 * @param description
	 * @return this builder
	 */
	public SELF_TYPE description(final String description)
	{
		this.description = description;
		return self();
	}

	/**
	 * Makes {@link ArgumentParser#parse(String...) throw {@link MissingRequiredArgumentException} if this argument isn't given
	 * @return this builder
	 * @throws IllegalStateException if {@link #defaultValue(Object)} has been called, because these two methods are mutually exclusive
	 * TODO: should this exclusivity be achieved by returning a new object with a different interface?
	 */
	public SELF_TYPE required()
	{
		if(defaultValue != null)
		{
			throw new IllegalStateException("Having a requried argument defaulting to some value: " + defaultValue + ", makes no sense. Remove the call to ArgumentBuilder#defaultValue(...) to use a required argument.");
		}
		required = true;
		return self();
	}

	/**
	 * Sets a default value to use for this argument. Returned by {@link ParsedArguments#get(Argument)} when no argument was given
	 * @return this builder
	 * @throws IllegalStateException if {@link #required()} has been called, because these two methods are mutually exclusive
	 * TODO: should this exclusivity be achieved by returning a new object with a different interface?
	 */
	public SELF_TYPE defaultValue(final T value)
	{
		if(required)
		{
			throw new IllegalStateException("Having a requried argument defaulting to some value: " + value + ", makes no sense. Remove the call to ArgumentBuilder#required to use a default value.");
		}
		this.defaultValue = value;
		return self();
	}

	public SELF_TYPE validator(final ValueValidator<T> validator)
	{
		this.validator = validator;
		return self();
	}

	/**
	 * TODO: add support for splitter:
	 * -ports 1,2,3 => [1,2,3]
	 * @param separator
	 * @return this builder
	 */
	public SELF_TYPE separator(final String separator)
	{
		this.separator  = separator;
		return self();
	}

	/**
	 * Makes this argument handle properties like arguments: -Dproperty.name=value
	 *
	 * where value is decoded by the previously set {@link ArgumentHandler}
	 * @return
	 */
	public MapArgumentBuilder<T> asPropertyMap()
	{
		return new MapArgumentBuilder<T>(this);
	}

	public static class MapArgumentBuilder<T> extends ArgumentBuilder<MapArgumentBuilder<T>, Map<String, T>>
	{
		protected MapArgumentBuilder(final ArgumentBuilder<?,T> builder)
		{
			super(new MapArgument<T>(builder.handler, builder.validator));
			copy(builder);
			if(this.separator == null)
			{
				//Maybe use assignment instead?
				this.separator = "=";
			}
			this.isPropertyMap = true;
		}

		/**
		 * @deprecated because repeated should be called before {@link #asPropertyMap()}
		 */
		@Deprecated
		@Override
		public RepeatedArgumentBuilder<Map<String, T>> repeated()
		{
			throw new UnsupportedOperationException("You'll need to call repeated before asPropertyMap");
		}
	}

	/**
	 * Useful for handling a variable amount of parameters in the end of a command.
	 * Uses this argument to parse values but assumes that all the following parameters are of the same type,
	 * integer in the following example:
	 *
	 * <pre><code>
	 * ListArgument&lt;Integer&gt; numbers = ArgumentFactory.integerArgument("--numbers").consumeAll();
	 *
	 * String[] threeArgs = {"--numbers", "1", "2", "3"};
	 * assertEqual(Arrays.asList(1, 2, 3), ArgumentParser.forArguments(numbers).parse(threeArgs).get(numbers));
	 *
	 * String[] twoArgs = {"--numbers", "4", "5"};
	 * assertEqual(Arrays.asList(4, 5), ArgumentParser.forArguments(numbers).parse(twoArgs).get(numbers));
	 * </code></pre>
	 *
	 * @return a newly created ListArgument that you need to save into a variable to access the list later on
	 */
	public ListArgumentBuilder<T> consumeAll()
	{
		return new ListArgumentBuilder<T>(this, ListArgument.CONSUME_ALL);
	}

	/**
	 * Uses this argument to parse values but assumes that <code>numberOfParameters</code> of the following
	 * parameters are of the same type,integer in the following example:
	 * <pre><code>
	 * String[] args = {"--numbers", "4", "5", "Hello"};
	 * ListArgument&lt;Integer&gt; numbers = ArgumentFactory.integerArgument("--numbers").arity(2);
	 * assertEqual(Arrays.asList(4, 5), ArgumentParser.forArguments(numbers).parse(args).get(numbers));
	 * </code></pre>
	 * @return a newly created ListArgument that you need to save into a variable to access the list later on
	 */
	public ListArgumentBuilder<T> arity(final int numberOfParameters)
	{
		return new ListArgumentBuilder<T>(this, numberOfParameters);
	}

	/**
	 * Makes it possible to enter several values for the same argument.
	 * Such as this:
	 * <pre><code>
	 * String[] args = {"--number", "1", "--number", "2"};
	 * RepeatedArgument&lt;Integer&gt; number = ArgumentFactory.integerArgument("--number").repeated();
	 * ParsedArguments parsed = ArgumentParser.forArguments(number).parse(args);
	 * assertEquals(Arrays.asList(1, 2), parsed.get(number));
	 * </code></pre>
	 *
	 * If you want to combine a specific {@link #arity(int)} or {@link #consumeAll()} then call those before calling this.
	 * <pre><code>
	 * String[] args = {"--numbers", "5", "6", "--numbers", "3", "4"};
	 * RepeatedArgument&lt;List&lt;Integer&gt;&gt; numbers = ArgumentFactory.integerArgument("--numbers").arity(2).repeated();
	 * ParsedArguments parsed = ArgumentParser.forArguments(numbers).parse(args);
	 *
	 * List&lt;List&lt;Integer&gt;&gt; numberLists = new ArrayList<List<Integer>>();
	 * numberLists.add(Arrays.asList(5, 6));
	 * numberLists.add(Arrays.asList(3, 4));
	 * List&lt;List&lt;Integer&gt;&gt; actual = parsed.get(numbers);
	 * assertEqual("", numberLists, actual);
	 * </code></pre>
	 *
	 * For repeated values in a property map such as this:
	 * <pre><code>
	 * Argument&lt;Map&lt;String, List&lt;Integer&gt;&gt;&gt; numberMap = integerArgument("-N").repeated().asPropertyMap().build();
	 * ParsedArguments parsed = ArgumentParser.forArguments(numberMap).parse("-Nnumber=1", "-Nnumber=2");
	 * assertThat(parsed.get(numberMap).get("number")).isEqualTo(Arrays.asList(1, 2));
	 * </code></pre>
	 *
	 * {@link #repeated()} should be called before {@link #asPropertyMap()}
	 *
	 * @return a newly created RepeatedArgument that you need to save into a variable to access the list later on
	 */
	public RepeatedArgumentBuilder<T> repeated()
	{
		return new RepeatedArgumentBuilder<T>(this);
	}

	/**
	 * <pre>
	 * Copies all values from the given copy into this one, except for:
	 * {@link ArgumentBuilder#handler} & {@link ArgumentBuilder#defaultValue} as they change between different builders
	 * (e.g the default value for Argument&lt;Boolean&gt; and Argument&lt;String&gt; are not compatible)
	 * @param copy the ArgumentBuilder to copy from
	 */
	protected SELF_TYPE copy(final ArgumentBuilder<?, ?> copy)
	{
		this.names = copy.names;
		this.description = copy.description;
		this.required = copy.required;
		this.separator = copy.separator;
		this.ignoreCase = copy.ignoreCase;
		this.isPropertyMap = copy.isPropertyMap;
		return self();
	}

	//Non-Interesting builders
	public static class ListArgumentBuilder<T> extends ArgumentBuilder<ListArgumentBuilder<T>, List<T>>
	{
		public ListArgumentBuilder(final ArgumentBuilder<? extends ArgumentBuilder<?,T>, T> builder, final int argumentsToConsume)
		{
			super(new ListArgument<T>(builder.handler, argumentsToConsume, builder.validator, "")); //TODO: fix ""
			copy(builder);
		}

		/**
		 * This method should be called before {@link ArgumentBuilder#arity(int)}/{@link ArgumentBuilder#consumeAll()}
		 */
		@Deprecated
		@Override
		public ListArgumentBuilder<T> validator(final ValueValidator<List<T>> validator)
		{
			throw new IllegalStateException("Programmer Error. Call validator(...) before arity/consumeAll()");
		}
	}

	public static class RepeatedArgumentBuilder<T> extends ArgumentBuilder<RepeatedArgumentBuilder<T>, List<T>>
	{
		public RepeatedArgumentBuilder(final ArgumentBuilder<? extends ArgumentBuilder<?,T>, T> builder)
		{
			super(new RepeatedArgument<T>(builder.handler, builder.validator));
			copy(builder);
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
		 * This method should be called before repeated()
		 */
		@Deprecated
		@Override
		public RepeatedArgumentBuilder<T> validator(final ValueValidator<List<T>> validator)
		{
			throw new IllegalStateException("Programmer Error. Call validator(...) before repeated()");
		}
	}

	public static class OptionArgumentBuilder extends ArgumentBuilder<OptionArgumentBuilder, Boolean>
	{
		public OptionArgumentBuilder()
		{
			super(null);
			defaultValue(Boolean.FALSE);
		}

		@Override
		public Argument<Boolean> build()
		{
			handler(new OptionArgument(defaultValue));
			return super.build();
		}
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
		 * @deprecated a separator is useless since an optional flag can't be assigned a value
		 */
		@Deprecated
		@Override
		public OptionArgumentBuilder separator(final String separator)
		{
			throw new IllegalStateException("A seperator for an optional flag isn't supported as " +
					"an optional flag can't be assigned a value");
		}

		/**
		 * @deprecated an optional flag can only have an arity of zero
		 */
		@Deprecated
		@Override
		public ListArgumentBuilder<Boolean> arity(final int arity)
		{
			throw new IllegalStateException("An optional flag can't have any other arity than zero");
		}
	}
}
