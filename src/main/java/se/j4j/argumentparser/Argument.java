package se.j4j.argumentparser;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;

public abstract class Argument<T>
{
	private String[] names;
	private T defaultValue = null;
	private String description = "";
	private boolean required = false;
	private String separator = null;
	/**
	 * If set to true {@link ArgumentParser#parse(String...)} ignores the case of the argument names set by {@link Argument#Argument(String...)}
	 */
	private boolean	ignoreCase;

	/**
	 * <pre>
	 * Creates a basic object for handling Arguments taken from a command line invocation.
	 * For practical uses of this constructor see {@link ArgumentFactory#optionArgument(String...)} and friends.
	 * </pre>
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
	 * @return an Argument that can be given as input to {@link ArgumentParser#forArguments(Argument...)} and {@link ParsedArguments#get(Argument)}
	 */
	protected Argument(final String ... names)
	{
		this.names = names;
	}


	/**
	 * @param currentArgument an iterator where {@link Iterator#next()} points to the parameter for a named argument,
	 * 							for an indexed argument it points to the single unnamed argument
	 * @return the parsed value
	 * @throws NoSuchElementException when an argument expects a parameter and it's not found
	 */
	abstract T parse(ListIterator<String> currentArgument) throws ArgumentException;

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
	public ListArgument<T> consumeAll()
	{
		return new ListArgument<T>(this, ListArgument.CONSUME_ALL);
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
	public ListArgument<T> arity(final int numberOfParameters)
	{
		return new ListArgument<T>(this, numberOfParameters);
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
	public RepeatedArgument<T> repeated()
	{
		return new RepeatedArgument<T>(this);
	}

	/**
	 * Sets a default value to use for this argument. Returned by {@link ParsedArguments#get(Argument)} when no argument was given
	 * @return this argument
	 * @throws IllegalStateException if {@link #required()} has been called, because these two methods are mutually exclusive
	 * TODO: should this exclusivity be achieved by returning a new object with a different interface?
	 */
	public Argument<T> defaultValue(final T value)
	{
		if(isRequired())
		{
			throw new UnsupportedOperationException("Having a requried argument defaulting to some value: " + value + ", makes no sense. Remove the call to Argument#required to use a default value.");
		}
		this.defaultValue = value;
		return this;
	}

	/**
	 * Makes {@link ArgumentParser#parse(String...) throw {@link MissingRequiredArgumentException} if this argument isn't given
	 * @return this argument
	 * @throws IllegalStateException if {@link #required()} has been called, because these two methods are mutually exclusive
	 * TODO: should this exclusivity be achieved by returning a new object with a different interface?
	 */
	public Argument<T> required()
	{
		Object defaultValueSet = defaultValue();
		if(defaultValueSet != null)
		{
			throw new UnsupportedOperationException("Having a requried argument defaulting to some value: " + defaultValueSet + ", makes no sense. Remove the call to Argument#defaultValue(...) to use a required argument.");
		}
		required = true;
		return this;
	}

	boolean isRequired()
	{
		return required;
	}

	/**
	 * Works just like the constructor {@link #Argument(String...)} but may provide some clarity to the reader.
	 * @param names see {@link #Argument(String...)}
	 * @return this argument
	 */
	public Argument<T> names(final String... names)
	{
		this.names = names;
		return this;
	}

	/**
	 * TODO: support resource bundles with i18n texts
	 * @param description
	 * @return this argument
	 */
	public Argument<T> description(final String description)
	{
		this.description = description;
		return this;
	}

	/**
	 * TODO: add support for splitter:
	 * -ports 1,2,3 => [1,2,3]
	 * @param separator
	 * @return
	 */
	public Argument<T> separator(final String separator)
	{
		this.separator  = separator;
		return this;
	}

	public String separator()
	{
		return separator;
	}

	public String description()
	{
		return description;
	}

	boolean isNamed()
	{
		return names.length > 0;
	}

	String[] names()
	{
		return names;
	}

	/**
	 * @return the default value for this argument, defaults to null. Set by {@link #defaultValue(Object)}
	 */
	T defaultValue()
	{
		return defaultValue;
	}

	Argument<T> ignoreCase()
	{
		ignoreCase = true;
		return this;
	}

	boolean isIgnoringCase()
	{
		return ignoreCase;
	}

	public String helpText()
	{
		String result = description;

		Object value = defaultValue();
		if(value != null)
		{
			result += " .Defaults to " + value + ".";
		}
		return result;
	}

	@Override
	public String toString()
	{
		String result = "";
		if(names != null)
		{
			for(String name : names)
			{
				result += name + ", ";
			}
		}
		if(result.length() == 0)
		{
			result += "[Unnamed]";
		}
		return result + (defaultValue != null ? " [Default: " + defaultValue + "]" : "") +
				(!"".equals(description) ? "[Description: " + description + "]" : "");
	}

}
