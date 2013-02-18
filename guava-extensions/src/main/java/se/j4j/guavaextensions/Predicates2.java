package se.j4j.guavaextensions;

import static com.google.common.base.Preconditions.checkNotNull;
import static se.j4j.strings.Descriptions.format;
import static se.j4j.strings.Descriptions.illegalArgument;

import java.util.List;

import javax.annotation.Nonnull;

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
	 * doesn't return true for {@code elementLimiter.apply(Object)}. Useful for input validation
	 * where you already have a {@link Predicate} for the element type and want to check a list of
	 * such elements.
	 * Without {@link #listPredicate(Predicate)} it would look something like:
	 * 
	 * <pre class="prettyprint">
	 * <code class="language-java">
	 * List&lt;Integer&gt; numbers = Arrays.asList(1, -1, -2);
	 * Optional&lt;Integer&gt; invalidNumber = Iterables.tryFind(numbers, Predicates.not(ABOVE_ZERO));
	 * if(invalidNumber.isPresent())
	 *   throw new IllegalArgumentException("'" + invalidNumber.get() + "' is not " + ABOVE_ZERO);
	 * </code>
	 * </pre>
	 * 
	 * With {@link #listPredicate(Predicate)}:
	 * 
	 * <pre class="prettyprint">
	 * <code class="language-java">
	 * Predicates2.listPredicate(ABOVE_ZERO).apply(numbers);
	 * </code>
	 * </pre>
	 * 
	 * As this breaks the general contract of {@link Predicate#apply(Object)} it should be
	 * considered useful for validating arguments only.
	 * The detail message only mentions the first element that didn't return true.
	 */
	public static <E> Predicate<List<? extends E>> listPredicate(Predicate<E> elementLimiter)
	{
		checkNotNull(elementLimiter);
		if(elementLimiter == Predicates.alwaysTrue())
			return Predicates.alwaysTrue();
		return new ListPredicate<E>(elementLimiter);
	}

	private static final class ListPredicate<E> implements Predicate<List<? extends E>>
	{
		private final Predicate<E> elementLimiter;

		private ListPredicate(Predicate<E> elementLimiter)
		{
			this.elementLimiter = elementLimiter;
		}

		@Override
		public boolean apply(@Nonnull List<? extends E> values)
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
