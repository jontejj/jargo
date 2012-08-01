package se.j4j.argumentparser;

import static se.j4j.argumentparser.ArgumentExceptions.forMissingParameter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.CommandLineParser.ArgumentIterator;
import se.j4j.argumentparser.StringParsers.InternalStringParser;

/**
 * <pre>
 * A <a href="http://en.wikipedia.org/wiki/Decorator_pattern">Decorator</a> class.
 * It allows you to subclass it and override individual methods
 * that you want to customize for an existing {@link StringParser}.
 * 
 * TODO: show code example
 * 
 * Most subclasses can just use {@link SimpleForwardingStringParser}.
 * 
 * Implementation Note: ForwardringStringParser also acts as a bridge for {@link InternalStringParser}s but
 * as InternalStringParser is package-private this detail should be seen as a implementation detail for now.
 * 
 * @param <T> the type the decorated {@link StringParser} handles
 * </pre>
 */
@Immutable
public abstract class ForwardingStringParser<T> extends InternalStringParser<T> implements StringParser<T>
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

	// Bridged InternalStringParser methods

	@Override
	T parse(ArgumentIterator arguments, T previousOccurance, ArgumentSettings argumentSettings) throws ArgumentException
	{
		if(!arguments.hasNext())
			throw forMissingParameter(argumentSettings);
		return parse(arguments.next());
	}

	@Override
	String descriptionOfValidValues(ArgumentSettings argumentSettings)
	{
		return descriptionOfValidValues();
	}

	@Override
	String describeValue(T value, ArgumentSettings argumentSettings)
	{
		return String.valueOf(value);
	}

	@Override
	String metaDescription(ArgumentSettings argumentSettings)
	{
		return metaDescription();
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

		/**
		 * Use the subclass itself as the delegate, only used internally.
		 */
		SimpleForwardingStringParser()
		{
			// TODO: this smells, could this delegation business be moved into StringParserBridge?
			this.delegate = this;
		}

		@Override
		protected final StringParser<T> delegate()
		{
			return delegate;
		}
	}
}
