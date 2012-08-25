package se.j4j.argumentparser.internal;

import static se.j4j.argumentparser.Descriptions.format;
import static se.j4j.argumentparser.Descriptions.illegalArgument;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public final class Predicates2
{
	private Predicates2()
	{
	}

	public static <E> Predicate<List<E>> forEach(Predicate<E> elementLimiter)
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
					throw illegalArgument(format(Texts.UNALLOWED_VALUE, value, elementLimiter));
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
