package se.j4j.argumentparser;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

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
	public static <T> Limiter<T> compound(@Nonnull Limiter<T> first, @Nonnull Limiter<T> second)
	{
		// Don't create a CompoundLimiter when it's not needed
		if(first == noLimits())
			return second;
		else if(second == noLimits())
			return first;

		return new CompoundLimiter<T>(ImmutableList.of(first, second));
	}

	public static <T> Limiter<T> compound(@Nonnull Iterable<? extends Limiter<T>> limiters)
	{
		return new CompoundLimiter<T>(ImmutableList.copyOf(limiters));
	}

	public static Limiter<Integer> positiveInteger()
	{
		return PositiveInteger.INSTANCE;
	}

	public static Limiter<File> existingFile()
	{
		return ExistingFile.INSTANCE;
	}

	public static <T> Limiter<List<T>> forListValues(Limiter<T> elementLimiter)
	{
		if(elementLimiter == noLimits())
			return noLimits();
		return new ListValueLimiter<T>(elementLimiter);
	}

	public static <T> Limiter<Map<String, T>> forMapValues(Limiter<T> valueLimiter)
	{
		if(valueLimiter == noLimits())
			return noLimits();
		return new MapValueLimiter<T>(valueLimiter);
	}

	public static <T> Limiter<T> noLimits()
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
	 * Limits values to {@link Integer}s greater than or equal to zero.
	 */
	private static final class PositiveInteger implements Limiter<Integer>
	{
		private static final Limiter<Integer> INSTANCE = new PositiveInteger();

		@Override
		public Limit withinLimits(@Nonnull final Integer value)
		{
			if(value >= 0)
				return Limit.OK;

			return Limit.notOk(value + " is not a positive integer");
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
			final File file;

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

	private static final class ListValueLimiter<T> implements Limiter<List<T>>
	{
		private final Limiter<T> elementLimiter;

		private ListValueLimiter(Limiter<T> elementLimiter)
		{
			this.elementLimiter = elementLimiter;
		}

		@Override
		public Limit withinLimits(List<T> values)
		{
			for(T value : values)
			{
				Limit limit = elementLimiter.withinLimits(value);
				if(limit != Limit.OK)
					return limit;
			}
			return Limit.OK;
		}
	}

	private static final class MapValueLimiter<T> implements Limiter<Map<String, T>>
	{
		private final Limiter<T> limiter;

		private MapValueLimiter(Limiter<T> valueLimiter)
		{
			this.limiter = valueLimiter;
		}

		@Override
		public Limit withinLimits(Map<String, T> map)
		{
			for(T value : map.values())
			{
				Limit limit = limiter.withinLimits(value);
				if(limit != Limit.OK)
					return limit;
			}
			return Limit.OK;
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
