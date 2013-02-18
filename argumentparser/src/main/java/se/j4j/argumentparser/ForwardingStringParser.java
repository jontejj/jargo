package se.j4j.argumentparser;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * <pre>
 * A <a href="http://en.wikipedia.org/wiki/Decorator_pattern">Decorator</a> class.
 * It allows you to subclass it and override individual methods
 * that you want to customize for an existing {@link StringParser}.
 * 
 * For instance, a parser that's useful in a garden application could look like this:
 * 
 * <pre class="prettyprint">
 * <code class="language-java">
 * private static final class WateringParser extends SimpleForwardingStringParser<Integer>
 * {
 *   WateringParser()
 *   {
 *     super(StringParsers.integerParser());
 *   }
 * 
 *   public Integer parse(String value, Locale locale) throws ArgumentException
 *   {
 *     waterPlants();
 *     return super.parse(value, locale);
 *   }
 * 
 *   private void waterPlants()
 *   {
 *     System.out.println("Watering plants");
 *   }
 * }
 * </code>
 * </pre>
 * 
 * This WateringParser can then be integrated with an argument via
 * {@link ArgumentFactory#withParser(StringParser)}.
 * Most subclasses can just use {@link SimpleForwardingStringParser}.
 * 
 * @param <T> the type the decorated {@link StringParser} handles
 *            </pre>
 */
@Immutable
public abstract class ForwardingStringParser<T> implements StringParser<T>
{
	/**
	 * A factory method that allow subclasses to switch delegate under their own terms,
	 * like the <a href="http://en.wikipedia.org/wiki/Proxy_pattern">Proxy</a> pattern explains.
	 * Just remember that a {@link StringParser} should be treated like it's {@link Immutable} so
	 * make sure your callers can't tell the difference when you switch parser.
	 * 
	 * @return the delegate to pass non-overridden calls to
	 */
	@Nonnull
	protected abstract StringParser<T> delegate();

	@Override
	public String descriptionOfValidValues(Locale locale)
	{
		return delegate().descriptionOfValidValues(locale);
	}

	@Override
	public T parse(final String value, Locale locale) throws ArgumentException
	{
		return delegate().parse(value, locale);
	}

	@Override
	public T defaultValue()
	{
		return delegate().defaultValue();
	}

	@Override
	public String metaDescription()
	{
		return delegate().metaDescription();
	}

	@Override
	public String toString()
	{
		return descriptionOfValidValues(Locale.US);
	}

	/**
	 * A {@link ForwardingStringParser} that uses an already created {@link StringParser} as its
	 * delegate.
	 * 
	 * @param <T> the type the decorated {@link StringParser} handles
	 */
	public abstract static class SimpleForwardingStringParser<T> extends ForwardingStringParser<T>
	{
		@Nonnull private final StringParser<T> delegate;

		protected SimpleForwardingStringParser(final StringParser<T> delegate)
		{
			this.delegate = delegate;
		}

		@Override
		protected final StringParser<T> delegate()
		{
			return delegate;
		}
	}
}
