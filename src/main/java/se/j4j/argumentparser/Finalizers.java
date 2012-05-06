package se.j4j.argumentparser;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableList;

public final class Finalizers
{
	private Finalizers()
	{
	}

	/**
	 * Runs several {@link Finalizer}s in the same order as they are
	 * given as arguments here.<br>
	 * If one of the {@link Finalizer}s makes <code>T</code> {@link Immutable},
	 * make sure to pass it in last as it's hard to finalize
	 * an {@link Immutable} value.
	 * 
	 * @param first a {@link Finalizer}
	 * @param second another {@link Finalizer}
	 * @return a merged {@link Finalizer}
	 */
	@Nullable
	public static <T> Finalizer<T> compound(@Nullable Finalizer<T> first, @Nullable Finalizer<T> second)
	{
		// Don't create a CompoundFinalizer when it's not needed
		if(first == null)
			return second;
		else if(second == null)
			return first;

		return new CompoundFinalizer<T>(ImmutableList.of(first, second));
	}

	public static <T> Finalizer<T> compound(@Nonnull Iterable<? extends Finalizer<T>> finalizers)
	{
		return new CompoundFinalizer<T>(ImmutableList.copyOf(finalizers));
	}

	public static <T> Finalizer<List<T>> forListValues(Finalizer<T> elementFinalizer)
	{
		if(elementFinalizer == null)
			return null;
		return new ListValueFinalizer<T>(elementFinalizer);
	}

	public static <T> Finalizer<Map<String, T>> forMapValues(Finalizer<T> valueFinalizer)
	{
		if(valueFinalizer == null)
			return null;
		return new MapValueFinalizer<T>(valueFinalizer);
	}

	public static <T> Finalizer<List<T>> unmodifiableListFinalizer()
	{
		return new UnmodifiableListMaker<T>();
	}

	public static <T> Finalizer<Map<String, T>> unmodifiableMapFinalizer()
	{
		return new UnmodifiableMapMaker<T>();
	}

	/**
	 * Puts several {@link Finalizer}s together and runs them in sequence
	 * 
	 * @param <T> type of value to finalize
	 */
	private static final class CompoundFinalizer<T> implements Finalizer<T>
	{
		@Nonnull private final List<Finalizer<T>> finalizers;

		private CompoundFinalizer(@Nonnull List<Finalizer<T>> finalizers)
		{
			this.finalizers = finalizers;
		}

		@Nullable
		@Override
		public T finalizeValue(@Nullable T value)
		{
			Iterator<Finalizer<T>> finalizersIterator = finalizers.iterator();
			T newValue = finalizersIterator.next().finalizeValue(value);

			while(finalizersIterator.hasNext())
			{
				newValue = finalizersIterator.next().finalizeValue(newValue);
			}
			return newValue;
		}
	}

	private static final class ListValueFinalizer<T> implements Finalizer<List<T>>
	{
		private final Finalizer<T> elementFinalizer;

		private ListValueFinalizer(Finalizer<T> elementFinalizer)
		{
			this.elementFinalizer = elementFinalizer;
		}

		@Override
		public List<T> finalizeValue(List<T> values)
		{
			ListIterator<T> valueIterator = values.listIterator();
			while(valueIterator.hasNext())
			{
				T finalizedValue = elementFinalizer.finalizeValue(valueIterator.next());
				valueIterator.set(finalizedValue);
			}
			return values;
		}
	}

	private static final class MapValueFinalizer<T> implements Finalizer<Map<String, T>>
	{
		private final Finalizer<T> finalizer;

		private MapValueFinalizer(Finalizer<T> valueFinalizer)
		{
			this.finalizer = valueFinalizer;
		}

		@Override
		public Map<String, T> finalizeValue(Map<String, T> map)
		{
			for(Entry<String, T> entry : map.entrySet())
			{
				T finalizedValue = finalizer.finalizeValue(entry.getValue());
				entry.setValue(finalizedValue);
			}
			return map;
		}
	}

	private static final class UnmodifiableListMaker<T> implements Finalizer<List<T>>
	{
		@Override
		public List<T> finalizeValue(List<T> value)
		{
			return unmodifiableList(value);
		}
	}

	private static final class UnmodifiableMapMaker<T> implements Finalizer<Map<String, T>>
	{
		@Override
		public Map<String, T> finalizeValue(Map<String, T> value)
		{
			return unmodifiableMap(value);
		}
	}

}
