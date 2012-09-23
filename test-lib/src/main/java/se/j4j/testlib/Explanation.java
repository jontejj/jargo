package se.j4j.testlib;

/**
 * Sometimes tests makes things that the tested API tells it not to,
 * these strings explain some motivations about why certain test code looks like it does.
 */
public final class Explanation
{
	private Explanation()
	{
	}

	/**
	 * Often used as a justification of RV_RETURN_VALUE_IGNORED. It's ignored simply because it
	 * verifies that invalid arguments is handled directly instead of passively and thus doing
	 * something with the returned value would only be confusing and it could even
	 * make the test fail for the wrong reasons.
	 */
	public static final String FAIL_FAST = "fail-fast during configuration phase";
}
