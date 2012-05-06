package se.j4j.argumentparser;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

public final class Callbacks
{
	private Callbacks()
	{
	}

	/**
	 * Runs several {@link Callback}s in the same order as they are
	 * given as arguments here.
	 * 
	 * @param first a {@link Callback}
	 * @param second another {@link Callback}
	 * @return a merged {@link Callback}
	 */
	@Nonnull
	public static <T> Callback<T> compound(@Nonnull Callback<T> first, @Nonnull Callback<T> second)
	{
		// Don't create a CompoundCallback when it's not needed
		if(first == noCallback())
			return second;
		else if(second == noCallback())
			return first;

		return new CompoundCallback<T>(ImmutableList.of(first, second));
	}

	@Nonnull
	public static <T> Callback<T> compound(@Nonnull Iterable<? extends Callback<T>> callbacks)
	{
		return new CompoundCallback<T>(ImmutableList.copyOf(callbacks));
	}

	public static <T> Callback<List<T>> forListValues(Callback<T> elementCallback)
	{
		if(elementCallback == noCallback())
			return noCallback();
		return new ListValueCallback<T>(elementCallback);
	}

	public static <T> Callback<Map<String, T>> forMapValues(Callback<T> elementCallback)
	{
		if(elementCallback == noCallback())
			return noCallback();
		return new MapValueCallback<T>(elementCallback);

	}

	public static <T> Callback<T> noCallback()
	{
		// Doesn't modify anything, i.e T is unused here
		@SuppressWarnings("unchecked")
		Callback<T> instance = (Callback<T>) NoCallback.INSTANCE;
		return instance;
	}

	/**
	 * Puts several {@link Callback}s together and runs them in sequence
	 * 
	 * @param <T> type of value that will be called back
	 */
	private static final class CompoundCallback<T> implements Callback<T>
	{
		@Nonnull private final List<Callback<T>> callbacks;

		private CompoundCallback(@Nonnull List<Callback<T>> callbacks)
		{
			this.callbacks = callbacks;
		}

		@Override
		public void parsedValue(@Nullable T parsedValue)
		{
			for(Callback<T> callback : callbacks)
			{
				callback.parsedValue(parsedValue);
			}
		}
	}

	private static final class ListValueCallback<T> implements Callback<List<T>>
	{
		private final Callback<T> elementCallback;

		private ListValueCallback(Callback<T> elementCallback)
		{
			this.elementCallback = elementCallback;
		}

		@Override
		public void parsedValue(List<T> parsedValues)
		{
			for(T value : parsedValues)
			{
				elementCallback.parsedValue(value);
			}
		}
	}

	private static final class MapValueCallback<T> implements Callback<Map<String, T>>
	{
		private final Callback<T> elementCallback;

		private MapValueCallback(Callback<T> elementCallback)
		{
			this.elementCallback = elementCallback;
		}

		@Override
		public void parsedValue(Map<String, T> parsedValues)
		{
			for(T value : parsedValues.values())
			{
				elementCallback.parsedValue(value);
			}
		}
	}

	/**
	 * A null object {@link Callback} for parsed values that doesn't do
	 * anything when values have been parsed
	 * 
	 * @param <T> the type of the parsed value
	 */
	private static final class NoCallback<T> implements Callback<T>
	{
		private static final Callback<?> INSTANCE = new NoCallback<Object>();

		@Override
		public void parsedValue(T parsedValue)
		{
		}
	}
}
