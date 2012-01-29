package se.j4j.argumentparser;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;

/**
 * <p><b>Note:</b> Although this class is not final, it cannot be subclassed
 * outside of this package as it has no public or protected constructors. Thus,
 * instances of this type are guaranteed to be immutable.
 *
 * @author Jonatan Jönsson <jontejj@gmail.com>
 *
 * @param <T>
 */
public class Argument<T>
{
	private final String[] names;
	private final T defaultValue;
	private final String description;
	private final boolean required;
	private final String separator;
	private final boolean ignoreCase;
	private final ArgumentHandler<T> handler;

	/**
	 * <pre>
	 * Creates a basic object for handling Arguments taken from a command line invocation.
	 * For practical uses of this constructor see {@link ArgumentFactory#optionArgument(String...)} and the {@link ArgumentBuilder}.
	 * </pre>
	 * @return an Argument that can be given as input to {@link ArgumentParser#forArguments(Argument...)} and {@link ParsedArguments#get(Argument)}
	 */
	Argument(final ArgumentHandler<T> handler, final T defaultValue, final String description, final boolean required, final String separator, final boolean ignoreCase, final String ... names)
	{
		this.handler = handler;
		this.defaultValue = defaultValue;
		this.description = description;
		this.required = required;
		this.separator = separator;
		this.ignoreCase = ignoreCase;
		this.names = names;
	}

	Argument(final Argument<T> copy)
	{
		this.handler = copy.handler;
		this.defaultValue = copy.defaultValue;
		this.description = copy.description;
		this.required = copy.required;
		this.separator = copy.separator;
		this.ignoreCase = copy.ignoreCase;
		this.names = copy.names;
	}

	public ArgumentHandler<?> handler()
	{
		return handler;
	}

	boolean isRequired()
	{
		return required;
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
	public T defaultValue()
	{
		return defaultValue;
	}

	/**
	 * @ret

	 */
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
