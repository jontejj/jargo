package se.j4j.argumentparser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Parses {@link String}s into values of the type {@code T}.
 * Create an {@link Argument} for your created {@link StringParser}s with
 * {@link ArgumentFactory#withParser(StringParser)}.
 * An example implementation for handling joda-time dates:
 * 
 * <pre class="prettyprint">
 * <code class="language-java">
 * public class DateTimeParser implements StringParser&lt;DateTime&gt;
 * {
 * 	public static DefaultArgumentBuilder&lt;DateTime&gt; dateArgument(String ... names)
 * 	{
 * 		return ArgumentFactory.withParser(new DateTimeParser()).defaultValueDescription("Current time").names(names);
 * 	}
 * 
 * 	public String descriptionOfValidValues()
 * 	{
 * 		return "an ISO8601 date, such as 2011-02-28";
 * 	}
 * 
 * 	public DateTime parse(final String value) throws ArgumentException
 * 	{
 * 		try
 * 		{
 * 			return DateTime.parse(value);
 * 		}
 * 		catch(IllegalArgumentException wrongDateFormat)
 * 		{
 * 			throw ArgumentExceptions.withMessage(wrongDateFormat.getLocalizedMessage());
 * 		}
 * 	}
 * 
 * 	public DateTime defaultValue()
 * 	{
 * 		return DateTime.now();
 * 	}
 * 
 * 	public String metaDescription()
 * 	{
 * 		return "&lt;date&gt;";
 * 	}
 * }
 * </code>
 * </pre>
 * 
 * <pre>
 * When printed in usage, this would look like:
 * <code>
 * --start &lt;date&gt; &lt;date&gt;: an ISO8601 date, such as 2011-02-28
 *                Default: Current time
 * </code>
 * </pre>
 * 
 * @param <T> the type to parse {@link String}s into
 */
@Immutable
public interface StringParser<T>
{
	/**
	 * Parses the given {@link String} into the type {@code T}
	 * 
	 * @param argument the string as given from the command line
	 * @return the parsed value
	 * @throws ArgumentException if the string isn't valid according to
	 *             {@link #descriptionOfValidValues()}
	 */
	@Nonnull
	T parse(String argument) throws ArgumentException;

	/**
	 * Describes what values this {@link StringParser} accepts
	 * 
	 * @return a description string to show in usage texts
	 */
	@Nonnull
	String descriptionOfValidValues();

	/**
	 * Used when the {@link Argument} this parser is connected to wasn't given.
	 * Used as a fall-back mechanism when {@link ArgumentBuilder#defaultValue(Object)} isn't used.
	 */
	@Nullable
	T defaultValue();

	/**
	 * <pre>
	 * The meta description is the text displayed after the argument names.
	 * Typically surrounded by < and >
	 * Sort of like the default value of {@link ArgumentBuilder#metaDescription(String)}.
	 * 
	 * @return a meta description that very briefly explains what value this parser expects
	 */
	@Nonnull
	String metaDescription();
}
