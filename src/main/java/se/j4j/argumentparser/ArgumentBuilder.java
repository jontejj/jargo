package se.j4j.argumentparser;

import java.util.List;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;

public class ArgumentBuilder<T>
{
	private String[] names;
	protected T defaultValue = null;
	private String description = "";
	private boolean required = false;
	private String separator = null;
	private boolean	ignoreCase = false;
	private ArgumentHandler<T> handler;

	ArgumentBuilder(final ArgumentHandler<T> handler)
	{
		this.handler = handler;
	}

	public Argument<T> build()
	{
		return new Argument<T>(handler, defaultValue, description, required, separator, ignoreCase, names);
	}

	protected ArgumentBuilder<T> handler(final ArgumentHandler<T> handler)
	{
		this.handler = handler;
		return this;
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
	public ArgumentBuilder<T> names(final String... names)
	{
		this.names = names;
		return this;
	}

	/**
	 * If used {@link ArgumentParser#parse(String...)} ignores the case of the argument names set by {@link Argument#Argument(String...)}
	 */
	public ArgumentBuilder<T> ignoreCase()
	{
		ignoreCase = true;
		return this;
	}

	/**
	 * TODO: support resource bundles with i18n texts
	 * @param description
	 * @return this builder
	 */
	public ArgumentBuilder<T> description(final String description)
	{
		this.description = description;
		return this;
	}

	/**
	 * Makes {@link ArgumentParser#parse(String...) throw {@link MissingRequiredArgumentException} if this argument isn't given
	 * @return this builder
	 * @throws IllegalStateException if {@link #required()} has been called, because these two methods are mutually exclusive
	 * TODO: should this exclusivity be achieved by returning a new object with a different interface?
	 */
	public ArgumentBuilder<T> required()
	{
		if(defaultValue != null)
		{
			throw new IllegalStateException("Having a requried argument defaulting to some value: " + defaultValue + ", makes no sense. Remove the call to ArgumentBuilder#defaultValue(...) to use a required argument.");
		}
		required = true;
		return this;
	}

	/**
	 * Sets a default value to use for this argument. Returned by {@link ParsedArguments#get(Argument)} when no argument was given
	 * @return this builder
	 * @throws IllegalStateException if {@link #required()} has been called, because these two methods are mutually exclusive
	 * TODO: should this exclusivity be achieved by returning a new object with a different interface?
	 */
	public ArgumentBuilder<T> defaultValue(final T value)
	{
		if(required)
		{
			throw new IllegalStateException("Having a requried argument defaulting to some value: " + value + ", makes no sense. Remove the call to ArgumentBuilder#required to use a default value.");
		}
		this.defaultValue = value;
		return this;
	}

	/**
	 * TODO: add support for splitter:
	 * -ports 1,2,3 => [1,2,3]
	 * @param separator
	 * @return this builder
	 */
	public ArgumentBuilder<T> separator(final String separator)
	{
		this.separator  = separator;
		return this;
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
	 * @return a newly created RepeatedArgument that you need to save into a variable to access the list later on
	 */
	public RepeatedArgumentBuilder<T> repeated()
	{
		return new RepeatedArgumentBuilder<T>(this);
	}

	public static class RepeatedArgumentBuilder<T> extends ArgumentBuilder<List<T>>
	{
		RepeatedArgumentBuilder(final ArgumentBuilder<T> builder)
		{
			super(new RepeatedArgument<T>(builder.handler));
			copy(builder);
		}

		@Override
		public Argument<List<T>> build()
		{
			return super.build();
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
	}

	/**
	 * <pre>
	 * Copies all values from the given copy into this one, except for:
	 * {@link ArgumentBuilder#handler} & {@link ArgumentBuilder#defaultValue} as they change between different builders
	 * (e.g the default value for Argument&lt;Boolean&gt; and Argument&lt;String&gt; are not compatible)
	 * @param copy the ArgumentBuilder to copy from
	 */
	ArgumentBuilder<T> copy(final ArgumentBuilder<?> copy)
	{
		this.names = copy.names;
		this.description = copy.description;
		this.required = copy.required;
		this.separator = copy.separator;
		this.ignoreCase = copy.ignoreCase;
		return this;
	}

	public static class ListArgumentBuilder<T> extends ArgumentBuilder<List<T>>
	{
		ListArgumentBuilder(final ArgumentBuilder<T> builder, final int argumentsToConsume)
		{
			super(new ListArgument<T>(builder.handler, argumentsToConsume));
			copy(builder);
		}

		@Override
		public Argument<List<T>> build()
		{
			return super.build();
		}
	}
}
