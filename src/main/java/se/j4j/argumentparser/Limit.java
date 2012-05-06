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
	@Nonnull public static final Limit OK = new Limit(Descriptions.EMPTY_STRING);

	/**
	 * Produces a limit response that tells why a value wasn't within the limits
	 * 
	 * @param reason the reason why a {@link Limiter} didn't accept a value
	 * @return a newly created {@link Limit} instance
	 */
	@CheckReturnValue
	@Nonnull
	public static Limit notOk(@Nonnull String reason)
	{
		return new Limit(Descriptions.forString(reason));
	}

	/**
	 * <pre>
	 * Produces a limit response that tells why a value wasn't within the
	 * limits.
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

	@CheckReturnValue
	@Nonnull
	public String reason()
	{
		return reasonForNotBeingWithinLimits.description();
	}

	private Limit(Description reason)
	{
		this.reasonForNotBeingWithinLimits = reason;
	};
}
