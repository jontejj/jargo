package se.j4j.argumentparser;

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
 * {@code private static final class WateringParser extends SimpleForwardingStringParser<Integer>
 * {
 * 	WateringParser()
 * 	{
 * 		super(StringParsers.integerParser());
 * 	}
 * 
 * 	public Integer parse(String value) throws ArgumentException
 * 	{
 * 		waterPlants();
 * 		return super.parse(value);
 * 	}
 * 
 * 	private void waterPlants()
 * 	{
 * 		System.out.println("Watering plants");
 * 	}
 * }
 * }
 * 
 * This WateringParser can then be integrated with an argument via the {@link ArgumentFactory#withParser(StringParser)} method.
 * 
 * Most subclasses can just use {@link SimpleForwardingStringParser}.
 * 
 * @param <T> the type the decorated {@link StringParser} handles
 * </pre>
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
	 * @return
	 */
	@Nonnull
	protected abstract StringParser<T> delegate();

	@Override
	public String descriptionOfValidValues()
	{
		return delegate().descriptionOfValidValues();
	}

	@Override
	public T parse(final String value) throws ArgumentException
	{
		return delegate().parse(value);
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

	/**
	 * A {@link ForwardingStringParser} that uses an already created {@link StringParser} as its
	 * delegate.
	 * 
	 * @param <T> the type the decorated {@link StringParser} handles
	 */
	public abstract static class SimpleForwardingStringParser<T> extends ForwardingStringParser<T>
	{
		@Nonnull private final StringParser<T> delegate;

		protected SimpleForwardingStringParser(@Nonnull final StringParser<T> delegate)
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
