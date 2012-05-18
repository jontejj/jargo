package se.j4j.argumentparser;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link Cache} that once it has been initialized always returns the same value.
 * Can be used to lazily initialize a singleton value.
 * 
 * @param <T> the type that this cache contains
 */
@ThreadSafe
public abstract class Cache<T>
{
	private T cachedValue;
	private boolean hasInitializedCache = false;

	/**
	 * @return the instance this {@link Cache} manages.
	 */
	@Nonnull
	public final synchronized T getCachedInstance()
	{
		if(!hasInitializedCache)
		{
			cachedValue = createInstance();
			// TODO: check null and throw IllegalStateException
			hasInitializedCache = true;
		}
		return cachedValue;
	}

	/**
	 * @return the instance this {@link Cache} should manage.
	 *         Only called once and only when the instance is requested.
	 */
	@Nonnull
	protected abstract T createInstance();
}
