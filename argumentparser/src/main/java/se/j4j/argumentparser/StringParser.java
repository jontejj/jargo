/* Copyright 2013 Jonatan Jönsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
*/
package se.j4j.argumentparser;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Parses {@link String}s into values of the type {@code T}.
 * Create an {@link Argument} for your {@link StringParser} with
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
 * 	public DateTime parse(String value, Locale locale) throws ArgumentException
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
 * @see StringParsers
 */
@Immutable
public interface StringParser<T>
{
	/**
	 * Parses the given {@link String} into the type {@code T}
	 * 
	 * @param argument the string as given from the command line
	 * @param locale the locale to parse strings with, may matter when parsing numbers, dates et.c
	 * @return the parsed value
	 * @throws ArgumentException if the string isn't valid according to
	 *             {@link #descriptionOfValidValues(Locale)}
	 */
	@Nonnull
	T parse(String argument, Locale locale) throws ArgumentException;

	/**
	 * Describes what values this {@link StringParser} accepts
	 * 
	 * @param locale the locale to print the description with
	 * @return a description string to show in usage texts
	 */
	@Nonnull
	String descriptionOfValidValues(Locale locale);

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
	 * For instance:
	 * {@link StringParsers#integerParser()}s {@link #metaDescription()} is &lt;integer&gt;
	 * 
	 * @return a meta description that very briefly explains what value this parser expects
	 */
	@Nonnull
	String metaDescription();
}
