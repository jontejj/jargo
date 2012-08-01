package se.j4j.argumentparser;

import static se.j4j.argumentparser.Describers.fileDescriber;

import java.io.File;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import se.j4j.argumentparser.StringParsers.Radix;
import se.j4j.argumentparser.internal.NumberType;

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
	public static <C extends Comparable<C>> Limiter<C> range(C start, C end)
	{
		return new RangeLimiter<C>(Ranges.closed(start, end));
	}

	@Nonnull
	@CheckReturnValue
	public static Limiter<File> existingFiles()
	{
		return ExistingFile.INSTANCE;
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
	static <T> Limiter<T> noLimits()
	{
		// Doesn't modify anything, i.e T is unused here
		@SuppressWarnings("unchecked")
		Limiter<T> instance = (Limiter<T>) NoLimits.INSTANCE;
		return instance;
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

		@Override
		public String descriptionOfValidValues()
		{
			return "an existing file";
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
				return fileDescriber().describe(file) + " isn't an existing file";
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

		@Override
		public String descriptionOfValidValues()
		{
			return elementLimiter.descriptionOfValidValues();
		}
	}

	static final class RangeLimiter<C extends Comparable<C>> implements Limiter<C>
	{
		final Range<C> rangeToLimitValuesTo;

		private RangeLimiter(final Range<C> rangeToLimitValuesTo)
		{
			this.rangeToLimitValuesTo = rangeToLimitValuesTo;
		}

		@Override
		public Limit withinLimits(C value)
		{
			if(rangeToLimitValuesTo.contains(value))
				return Limit.OK;
			return Limit.notOk("'" + value + "' is not in the range " + descriptionOfValidValues());
		}

		@Override
		public String descriptionOfValidValues()
		{
			return rangeToLimitValuesTo.lowerEndpoint() + " to " + rangeToLimitValuesTo.upperEndpoint();
		}
	}

	static final class RangeLimiterForRadix<T extends Number & Comparable<T>> implements Limiter<T>
	{
		private final Range<T> range;
		private final Radix radix;
		private final NumberType<T> type;

		RangeLimiterForRadix(final Range<T> range, final Radix radix, final NumberType<T> type)
		{
			this.range = range;
			this.radix = radix;
			this.type = type;
		}

		@Override
		public Limit withinLimits(T value)
		{
			if(range.contains(value))
				return Limit.OK;

			String describedValue = type.toString(value, radix);
			return Limit.notOk("'" + describedValue + "' is not in the range " + descriptionOfValidValues());
		}

		@Override
		public String descriptionOfValidValues()
		{
			String minValue = type.toString(range.lowerEndpoint(), radix);
			String maxValue = type.toString(range.upperEndpoint(), radix);
			return minValue + " to " + maxValue + " (" + radix.description() + ")";
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

		@Override
		public String descriptionOfValidValues()
		{
			throw new IllegalStateException("StringParser#descriptionOfValidValues() should be used instead");
		}
	}

}
