package se.j4j.argumentparser.cache;

import org.junit.Test;

import se.j4j.argumentparser.Cache;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class TestCache
{
	@Test(expected = IllegalStateException.class)
	public void testThatNullValuesAreInvalidatedByCache()
	{
		new Cache<Object>(){
			@SuppressFBWarnings(value = "NP_NONNULL_RETURN_VIOLATION", justification = "Tests that @Nonnull is programmatically enforced")
			@Override
			protected Object createInstance()
			{
				return null;
			}
		}.getCachedInstance();
	}
}
