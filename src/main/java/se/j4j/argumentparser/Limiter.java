package se.j4j.argumentparser;

import java.util.ListIterator;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * <pre>
 * Allows for only allowing a subset of values parsed by {@link ArgumentHandler#parse(ListIterator)} implementations.
 * 
 * As an example, look at {@link Limiters#positiveInteger()} and {@link Limiters#existingFile()}.
 * 
 * Values have been passed through any {@link Finalizer#finalizeValue(Object)} before {@link #withinLimits(Object)} is called.
 * 
 * @param <T> the type to limit
 * </pre>
 */
public interface Limiter<T>
{
	/**
	 * <pre>
	 * Limits a value parsed by {@link ArgumentHandler#parse(ListIterator)} to be within some arbitrary limits. 
	 * 
	 * @param value the value to check if it's within the limits of this limiter
	 * @return {@link Limit#OK} if the value is within the limits, i.e if it's an allowed value. 
	 * If it's not within the limits {@link Limit#notOk(String)} or
	 * {@link Limit#notOk(Description)} should be used to describe why it wasn't.
	 */
	@CheckReturnValue
	@Nonnull
	Limit withinLimits(@Nullable T value);

	// TODO: print this pro active in usage somehow:
	// String validValuesDescription();
}
