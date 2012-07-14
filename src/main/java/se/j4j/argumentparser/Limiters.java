package se.j4j.argumentparser;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
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
	 * Runs several {@link Limiter}s in the same order as they are
	 * given as arguments here.
	 * 
	 * @param first a {@link Limiter}
	 * @param second another {@link Limiter}
	 * @return a merged {@link Limiter}
	 */
	@Nonnull
	@CheckReturnValue
	public static <T> Limiter<T> compound(@Nonnull Limiter<T> first, @Nonnull Limiter<T> second)
	{
		// Don't create a CompoundLimiter when it's not needed
		if(first == noLimits())
			return second;

		return new CompoundLimiter<T>(ImmutableList.of(first, second));
	}

	@Nonnull
	@CheckReturnValue
	public static <T> Limiter<T> compound(@Nonnull Iterable<? extends Limiter<T>> limiters)
	{
		return new CompoundLimiter<T>(ImmutableList.copyOf(limiters));
	}

	@Nonnull
	@CheckReturnValue
	public static <C extends Comparable<C>> Limiter<C> range(C start, C end)
	{
		return new RangeLimiter<C>(Ranges.closed(start, end));
	}

	/**
	 * <b>Note:</b>May be removed in the future if Guava is removed as a dependency
	 */
	@Beta
	@Nonnull
	@CheckReturnValue
	public static <C extends Comparable<C>> Limiter<C> range(Range<C> range)
	{
		return new RangeLimiter<C>(range);
	}

	@Nonnull
	@CheckReturnValue
	public static Limiter<File> existingFiles()
	{
		return ExistingFile.INSTANCE;
	}

	// TODO: add regex limiter (maybe to ArgumentBuilder#limitWith instead, as a pre-limiter?)

	@Nonnull
	@CheckReturnValue
	static <E> Limiter<List<E>> forListValues(@Nonnull Limiter<E> elementLimiter)
	{
		if(elementLimiter == noLimits())
			return noLimits();
		return new ListValueLimiter<E>(elementLimiter);
	}

	@Nonnull
	@CheckReturnValue
	static <K, V> Limiter<Map<K, V>> forMapValues(@Nonnull Limiter<V> valueLimiter)
	{
		if(valueLimiter == noLimits())
			return noLimits();
		return new MapValueLimiter<K, V>(valueLimiter);
	}

	@Nonnull
	@CheckReturnValue
	static <T> Limiter<T> noLimits()
	{
		// Doesn't modify anything, i.e T is unused here
		@SuppressWarnings("unchecked")
		Limiter<T> instance = (Limiter<T>) NoLimits.INSTANCE;
		return instance;
	}

	/**
	 * Puts several {@link Limiter}s together and runs them in sequence
	 * 
	 * @param <T> type of value to validate
	 */
	private static final class CompoundLimiter<T> implements Limiter<T>
	{
		private final Collection<Limiter<T>> limiters;

		private CompoundLimiter(Collection<Limiter<T>> limiters)
		{
			this.limiters = limiters;
		}

		@Override
		public Limit withinLimits(T value)
		{
			for(Limiter<T> limiter : limiters)
			{
				Limit limit = limiter.withinLimits(value);
				if(limit != Limit.OK)
					return limit;
			}
			return Limit.OK;
		}
	}

	/**
	 * Limits arguments to only point to existing {@link File}s.
	 */
	private static final class ExistingFile implements Limiter<File>
	{
		private static final Limiter<File> INSTANCE = new ExistingFile();

		@Override
		public Limit withinLimits(@Nonnull final File file)
		{
			if(file.exists())
				return Limit.OK;

			return Limit.notOk(new DescribeAsNonExistingFile(file));
		}

		private static final class DescribeAsNonExistingFile implements Description
		{
			private final File file;

			private DescribeAsNonExistingFile(File file)
			{
				this.file = file;
			}

			@Override
			public String description()
			{
				return file.getAbsolutePath() + " doesn't exist as a file";
			}
		}
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
	}

	private static final class MapValueLimiter<K, V> implements Limiter<Map<K, V>>
	{
		private final Limiter<V> limiter;

		private MapValueLimiter(Limiter<V> valueLimiter)
		{
			this.limiter = valueLimiter;
		}

		@Override
		public Limit withinLimits(Map<K, V> map)
		{
			for(V value : map.values())
			{
				Limit limit = limiter.withinLimits(value);
				if(limit != Limit.OK)
					return limit;
			}
			return Limit.OK;
		}
	}

	private static final class RangeLimiter<C extends Comparable<C>> implements Limiter<C>
	{
		private final Range<C> rangeToLimitValuesTo;

		private RangeLimiter(final Range<C> rangeToLimitValuesTo)
		{
			this.rangeToLimitValuesTo = rangeToLimitValuesTo;
		}

		@Override
		public Limit withinLimits(C value)
		{
			if(rangeToLimitValuesTo.contains(value))
				return Limit.OK;
			return Limit.notOk("'" + value + "' is not in the range " + rangeToLimitValuesTo.toString());
		}
	}

	/**
	 * A null object {@link Limiter} for parsed values
	 * that doesn't impose any limits
	 * 
	 * @param <T> the type of the parsed value
	 */
	private static final class NoLimits<T> implements Limiter<T>
	{
		private static final Limiter<?> INSTANCE = new NoLimits<Object>();

		@Override
		public Limit withinLimits(T value)
		{
			return Limit.OK;
		}
	}

}
