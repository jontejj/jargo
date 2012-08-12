package se.j4j.argumentparser;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Response object for {@link Limiter#withinLimits(Object)}
 */
public final class Limit
{
	@Nonnull private final Description reasonForNotBeingWithinLimits;

	/**
	 * The singleton OK instance indicating that the value is within the
	 * acceptable limits
	 */
	@Nonnull public static final Limit OK = new Limit(Descriptions.withString("OK"));

	/**
	 * <pre>
	 * Produces a limit response that tells why a value isn't within the limits.
	 * This is the error message that is displayed when
	 * {@link ArgumentException#getMessage()} is called
	 * 
	 * @param reason the reason why a {@link Limiter} didn't accept a value
	 * @return a newly created {@link Limit} instance
	 * </pre>
	 */
	@CheckReturnValue
	@Nonnull
	public static Limit notOk(@Nonnull String reason)
	{
		return new Limit(Descriptions.withString(reason));
	}

	/**
	 * <pre>
	 * Produces a limit response that tells why a value wasn't within the
	 * limits.
	 * 
	 * This is the error message that is displayed when
	 * {@link ArgumentException#getMessage()} is called
	 * 
	 * Works just like {@link #notOk(String)} except that it allows the
	 * descriptions to be lazily created instead.
	 * 
	 * @param reason the reason why a {@link Limiter} didn't accept a value
	 * @return a newly created {@link Limit} instance
	 * </pre>
	 */
	@CheckReturnValue
	@Nonnull
	public static Limit notOk(@Nonnull Description reason)
	{
		return new Limit(reason);
	}

	/**
	 * Returns the string "OK" if the value was within the limits, otherwise this returns the
	 * {@code reason} supplied to {@link #notOk(String)}.
	 */
	@Override
	@CheckReturnValue
	@Nonnull
	public String toString()
	{
		return reasonForNotBeingWithinLimits.description();
	}

	Description reason()
	{
		return reasonForNotBeingWithinLimits;
	}

	private Limit(Description reason)
	{
		this.reasonForNotBeingWithinLimits = reason;
	};
}
