/* Copyright 2013 Jonatan Jönsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package se.softhouse.common.strings;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.softhouse.common.strings.StringsUtil.NEWLINE;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import se.softhouse.common.testlib.Locales;
import se.softhouse.common.testlib.UtilityClassTester;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;
import com.google.common.util.concurrent.Atomics;

/**
 * Tests for {@link Lines}
 */
public class LinesTest
{
	@Test
	public void testThatNullContractsAreFollowed() throws Exception
	{
		new NullPointerTester().testStaticMethods(Lines.class, Visibility.PACKAGE);
	}

	@Test
	public void testThatUtilityClassDesignIsCorrect()
	{
		UtilityClassTester.testUtilityClassDesign(Lines.class);
	}

	private static final String LONG_TEXT = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, "
			+ "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, "
			+ "quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. "
			+ "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. "
			+ "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

	private static final String TURKISH_RESULT = "Lorem ipsum dolor sit amet, " + NEWLINE //
			+ "consectetur adipisicing elit, sed do eiusmod " + NEWLINE //
			+ "tempor incididunt ut labore et dolore magna " + NEWLINE //
			+ "aliqua. Ut enim ad minim veniam, quis nostrud " + NEWLINE //
			+ "exercitation ullamco laboris nisi ut aliquip ex ea " + NEWLINE //
			+ "commodo consequat. Duis aute irure dolor in " + NEWLINE //
			+ "reprehenderit in voluptate velit esse cillum dolore " + NEWLINE //
			+ "eu fugiat nulla pariatur. Excepteur sint " + NEWLINE //
			+ "occaecat cupidatat non proident, sunt in culpa " + NEWLINE //
			+ "qui officia deserunt mollit anim id est " + NEWLINE //
			+ "laborum.";

	private static final String SWEDISH_RESULT = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod " + NEWLINE //
			+ "tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis " + NEWLINE //
			+ "nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis " + NEWLINE //
			+ "aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat " + NEWLINE //
			+ "nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui " + NEWLINE //
			+ "officia deserunt mollit anim id est laborum.";

	/**
	 * Amount of test harness
	 */
	private static final int ITERATION_COUNT = 3000;

	/**
	 * The threads should fight for CPU time
	 */
	private static final int RUNNERS_PER_PROCESSOR = 10;

	private static final int nrOfConcurrentRunners = Runtime.getRuntime().availableProcessors() * RUNNERS_PER_PROCESSOR;

	private final CountDownLatch activeWorkers = new CountDownLatch(nrOfConcurrentRunners);
	private final CyclicBarrier startupDone = new CyclicBarrier(nrOfConcurrentRunners);
	private final AtomicReference<Throwable> failure = Atomics.newReference();

	@Test
	public void testThatLineBreakerIsThreadSafe() throws Throwable
	{
		// TODO(jontejj): move out common test code with ConcurrentTest into ConcurrencyTester
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nrOfConcurrentRunners);
		for(int i = 0; i < nrOfConcurrentRunners; i++)
		{
			if(i % 2 == 0)
			{
				executor.execute(wrapSomeText(LONG_TEXT, SWEDISH_RESULT, 0, Locales.SWEDISH));
			}
			else
			{
				executor.execute(wrapSomeText(LONG_TEXT, TURKISH_RESULT, 40, Locales.TURKISH));
			}
		}

		try
		{
			if(!activeWorkers.await(60, TimeUnit.SECONDS))
			{
				executor.shutdownNow();
				fail("Timeout waiting for concurrency test to finish");
			}
		}
		catch(InterruptedException e)
		{
			Thread.interrupted();
			if(failure.get() != null)
			{
				executor.shutdownNow();
				throw failure.get();
			}
		}
		assertThat(executor.shutdownNow()).isEmpty();
	}

	private Runnable wrapSomeText(final String textToWrap, final String expectedResult, final int startingIndex, final Locale locale)
	{
		final Thread originThread = Thread.currentThread();
		return new Runnable(){
			@Override
			public void run()
			{
				try
				{
					startupDone.await(10, TimeUnit.SECONDS);
					for(int i = 0; i < ITERATION_COUNT; i++)
					{
						assertThat(Lines.wrap(textToWrap, startingIndex, 80, locale).toString()).isEqualTo(expectedResult);
					}
					activeWorkers.countDown();
				}
				catch(Throwable e)
				{
					// Don't report secondary failures
					if(failure.compareAndSet(null, e))
					{
						originThread.interrupt();
					}
				}
			}
		};
	}
}
