package se.j4j.argumentparser;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableList;

/**
 * Gives you static access to implementations of the {@link Finalizer} interface.
 */
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
	@Nonnull
	public static <T> Finalizer<T> compound(@Nonnull Finalizer<T> first, @Nonnull Finalizer<T> second)
	{
		// Don't create a CompoundFinalizer when it's not needed
		if(first == noFinalizer())
			return second;

		return new CompoundFinalizer<T>(ImmutableList.of(first, second));
	}

	/**
	 * @see #compound(Finalizer, Finalizer)
	 */
	@Nonnull
	public static <T> Finalizer<T> compound(@Nonnull Iterable<? extends Finalizer<T>> finalizers)
	{
		return new CompoundFinalizer<T>(ImmutableList.copyOf(finalizers));
	}

	@Nonnull
	static <E> Finalizer<List<E>> forListValues(@Nonnull Finalizer<E> elementFinalizer)
	{
		if(elementFinalizer == noFinalizer())
			return noFinalizer();
		return new ListValueFinalizer<E>(elementFinalizer);
	}

	@Nonnull
	static <K, V> Finalizer<Map<K, V>> forMapValues(@Nonnull Finalizer<V> valueFinalizer)
	{
		if(valueFinalizer == noFinalizer())
			return noFinalizer();
		return new MapValueFinalizer<K, V>(valueFinalizer);
	}

	@Nonnull
	static <E> Finalizer<List<E>> unmodifiableListFinalizer()
	{
		return new UnmodifiableListMaker<E>();
	}

	@Nonnull
	static <K, V> Finalizer<Map<K, V>> unmodifiableMapFinalizer()
	{
		return new UnmodifiableMapMaker<K, V>();
	}

	@Nonnull
	@CheckReturnValue
	static <T> Finalizer<T> noFinalizer()
	{
		// Doesn't modify anything, i.e T is unused here
		@SuppressWarnings("unchecked")
		Finalizer<T> instance = (Finalizer<T>) NoFinalizer.INSTANCE;
		return instance;
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

	private static final class ListValueFinalizer<E> implements Finalizer<List<E>>
	{
		private final Finalizer<E> elementFinalizer;

		private ListValueFinalizer(Finalizer<E> elementFinalizer)
		{
			this.elementFinalizer = elementFinalizer;
		}

		@Override
		public List<E> finalizeValue(List<E> values)
		{
			ListIterator<E> valueIterator = values.listIterator();
			while(valueIterator.hasNext())
			{
				E finalizedValue = elementFinalizer.finalizeValue(valueIterator.next());
				valueIterator.set(finalizedValue);
			}
			return values;
		}
	}

	private static final class MapValueFinalizer<K, V> implements Finalizer<Map<K, V>>
	{
		private final Finalizer<V> finalizer;

		private MapValueFinalizer(Finalizer<V> valueFinalizer)
		{
			this.finalizer = valueFinalizer;
		}

		@Override
		public Map<K, V> finalizeValue(Map<K, V> map)
		{
			for(Entry<K, V> entry : map.entrySet())
			{
				V finalizedValue = finalizer.finalizeValue(entry.getValue());
				entry.setValue(finalizedValue);
			}
			return map;
		}
	}

	private static final class UnmodifiableListMaker<E> implements Finalizer<List<E>>
	{
		@Override
		public List<E> finalizeValue(List<E> value)
		{
			return unmodifiableList(value);
		}
	}

	private static final class UnmodifiableMapMaker<K, V> implements Finalizer<Map<K, V>>
	{
		@Override
		public Map<K, V> finalizeValue(Map<K, V> value)
		{
			return unmodifiableMap(value);
		}
	}

	/**
	 * A null object {@link Finalizer} that doesn't modify the received object in any way before
	 * returning it.
	 */
	private static final class NoFinalizer<T> implements Finalizer<T>
	{
		private static final Finalizer<?> INSTANCE = new NoFinalizer<Object>();

		@Override
		public T finalizeValue(T value)
		{
			return value;
		}
	}
}
