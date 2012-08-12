package se.j4j.argumentparser;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import se.j4j.argumentparser.internal.Texts;

import com.google.common.annotations.Beta;
import com.google.common.base.Predicate;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

/**
 * Gives you static access to implementations of the {@link Limiter} interface.
 */
public final class Limiters
{
	private Limiters()
	{
	}

	/**
	 * Returns a {@link Limiter} that limits values to be within {@code start} (inclusive) and
	 * {@code end} (inclusive).
	 */
	@Nonnull
	@CheckReturnValue
	public static <C extends Comparable<C>> Limiter<C> range(@Nonnull C start, @Nonnull C end)
	{
		return new RangeLimiter<C>(Ranges.closed(start, end));
	}

	private static final class RangeLimiter<C extends Comparable<C>> implements Limiter<C>
	{
		private final Range<C> range;

		private RangeLimiter(final Range<C> rangeToLimitValuesTo)
		{
			this.range = rangeToLimitValuesTo;
		}

		@Override
		public Limit withinLimits(C value)
		{
			if(range.contains(value))
				return Limit.OK;
			return Limit.notOk(Descriptions.format(Texts.OUT_OF_RANGE, value, range.lowerEndpoint(), range.upperEndpoint()));
		}

		@Override
		public String descriptionOfValidValues()
		{
			return range.lowerEndpoint() + " to " + range.upperEndpoint();
		}

		@Override
		public String toString()
		{
			return "RangeLimiter: " + descriptionOfValidValues();
		}
	}

	/**
	 * <pre>
	 * Exposes a {@link Limiter} as a Guava {@link Predicate}.
	 * <b>Note:</b>This method may be removed in the future if Guava is removed as a dependency.
	 * 
	 * @param limiter the limiter to use as the {@link Predicate}
	 * @return a {@link Predicate} that filters out any values not within the limits of the given {@code limiter}
	 * </pre>
	 */
	@Beta
	public static <T> Predicate<T> asPredicate(final Limiter<T> limiter)
	{
		return new Predicate<T>(){
			@Override
			public boolean apply(T input)
			{
				return limiter.withinLimits(input) == Limit.OK;
			}
		};
	}

	// TODO: add public static <T> Limiter<T> forPredicate(final Predicate<T>
	// predicateToExposeAsLimiter)
	// or just use Predicate and use toString to print valid values,
	// that would make it possible to remove Limit, Limiter & Limiters

	/**
	 * A <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">null object</a> {@link Limiter}
	 * for parsed values that doesn't impose any limits
	 * 
	 * @param <T> the type of the parsed value
	 */
	@Nonnull
	@CheckReturnValue
	static <T> Limiter<T> noLimits()
	{
		// Doesn't modify anything, i.e T is unused here
		@SuppressWarnings("unchecked")
		Limiter<T> instance = (Limiter<T>) NoLimits.INSTANCE;
		return instance;
	}

	private static final class NoLimits<T> implements Limiter<T>
	{
		private static final Limiter<?> INSTANCE = new NoLimits<Object>();

		@Override
		public Limit withinLimits(T value)
		{
			return Limit.OK;
		}

		@Override
		public String descriptionOfValidValues()
		{
			throw new IllegalStateException("StringParser#descriptionOfValidValues() should be used instead");
		}
	}

	@Nonnull
	@CheckReturnValue
	static <E> Limiter<List<E>> forListValues(@Nonnull Limiter<E> elementLimiter)
	{
		if(elementLimiter == noLimits())
			return noLimits();
		return new ListValueLimiter<E>(elementLimiter);
	}

	private static final class ListValueLimiter<E> implements Limiter<List<E>>
	{
		private final Limiter<E> elementLimiter;

		private ListValueLimiter(Limiter<E> elementLimiter)
		{
			this.elementLimiter = elementLimiter;
		}

		@Override
		public Limit withinLimits(List<E> values)
		{
			for(E value : values)
			{
				Limit limit = elementLimiter.withinLimits(value);
				if(limit != Limit.OK)
					return limit;
			}
			return Limit.OK;
		}

		@Override
		public String descriptionOfValidValues()
		{
			return elementLimiter.descriptionOfValidValues();
		}
	}
}
