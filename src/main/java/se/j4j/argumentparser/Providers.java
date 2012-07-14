package se.j4j.argumentparser;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Gives you static access to implementations of the {@link Provider} interface.
 */
public final class Providers
{
	private Providers()
	{
	}

	/**
	 * Returns a {@link Provider} that will save the result of {@link Provider#provideValue()} and
	 * return the same value for subsequent calls. Only calls {@link Provider#provideValue()} when
	 * it's actually needed. This wrapper could be useful if the {@link Provider#provideValue()} is
	 * costly for <code>valueProvider</code> or if the {@link Provider#provideValue()} may return
	 * different values when called at different times and this isn't wanted behavior.
	 */
	@Nonnull
	@CheckReturnValue
	public static <T> Provider<T> cachingProvider(@Nonnull Provider<T> valueProvider)
	{
		return new CachingProvider<T>(valueProvider);
	}

	@Nonnull
	@CheckReturnValue
	public static <T> Provider<T> nonLazyProvider(@Nullable T valueToProvide)
	{
		return new NonLazyValueProvider<T>(valueToProvide);
	}

	@Nonnull
	@CheckReturnValue
	public static <T> Provider<List<T>> listWithOneValue(@Nonnull Provider<T> elementProvider)
	{
		return new ListProvider<T>(elementProvider);
	}

	static final class NonLazyValueProvider<T> implements Provider<T>
	{
		private final T valueToProvide;

		private NonLazyValueProvider(final T valueToProvide)
		{
			this.valueToProvide = valueToProvide;
		}

		@Override
		public T provideValue()
		{
			return valueToProvide;
		}

		@Override
		public String toString()
		{
			return String.valueOf(valueToProvide);
		}
	}

	private static final class ListProvider<T> implements Provider<List<T>>
	{
		private final Provider<T> elementProvider;

		private ListProvider(Provider<T> elementProvider)
		{
			this.elementProvider = elementProvider;
		}

		@Override
		public List<T> provideValue()
		{
			List<T> list = new ArrayList<T>(1);
			list.add(elementProvider.provideValue());
			return unmodifiableList(list);
		}
	}

	private static final class CachingProvider<T> extends Cache<T> implements Provider<T>
	{
		private final Provider<T> valueProvider;

		private CachingProvider(Provider<T> aValueProvider)
		{
			this.valueProvider = aValueProvider;
		}

		@Override
		public T provideValue()
		{
			return getCachedInstance();
		}

		@Override
		public T createInstance()
		{
			return valueProvider.provideValue();
		}
	}
}
