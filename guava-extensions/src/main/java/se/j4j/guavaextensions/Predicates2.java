package se.j4j.guavaextensions;

import static se.j4j.strings.Descriptions.format;
import static se.j4j.strings.Descriptions.illegalArgument;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Additional implementations of the {@link Predicate} interface
 */
public final class Predicates2
{
	private Predicates2()
	{
	}

	/**
	 * Returns a predicate that throws an {@link IllegalArgumentException} if any element in a list
	 * doesn't return true for {@code elementLimiter.apply(Object)}.
	 * The detail message only mentions the first element that didn't return true.
	 */
	public static <E> Predicate<List<E>> listPredicate(Predicate<E> elementLimiter)
	{
		if(elementLimiter == Predicates.alwaysTrue())
			return Predicates.alwaysTrue();
		return new ListPredicate<E>(elementLimiter);
	}

	private static final class ListPredicate<E> implements Predicate<List<E>>
	{
		private final Predicate<E> elementLimiter;

		private ListPredicate(Predicate<E> elementLimiter)
		{
			this.elementLimiter = elementLimiter;
		}

		@Override
		public boolean apply(List<E> values)
		{
			for(E value : values)
			{
				if(!elementLimiter.apply(value))
					throw illegalArgument(format("'%s' is not %s", value, elementLimiter));
			}
			return true;
		}

		@Override
		public String toString()
		{
			return elementLimiter.toString();
		}
	}
}
