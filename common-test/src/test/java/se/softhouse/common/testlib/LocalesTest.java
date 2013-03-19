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
package se.softhouse.common.testlib;

import static java.util.Locale.CANADA;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.softhouse.common.testlib.Constants.EXPECTED_TEST_TIME_FOR_THIS_SUITE;
import static se.softhouse.common.testlib.Locales.SWEDISH;
import static se.softhouse.common.testlib.Locales.TURKISH;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;
import com.google.common.util.concurrent.Atomics;

public class LocalesTest
{
	@Test
	public void testThatResetDefaultLocaleResetsToTheOneBeforeSetDefaultWasCalled() throws Exception
	{
		Locale defaultLocale = Locale.getDefault();
		Locales.setDefault(Locales.SWEDISH);
		Locale after = Locales.resetDefaultLocale();
		assertThat(after).isEqualTo(defaultLocale);
	}

	@Test
	public void testThatInterruptedExceptionIsThrownForTimeoutsWhenWaitingForThreadToBeDoneWithLocale() throws Throwable
	{
		final String ownerThread = Thread.currentThread().getName();
		// A gross underestimation of how long the locale will be used
		Locales.setDefault(SWEDISH, 1, TimeUnit.NANOSECONDS);
		Thread thread = new Thread(){
			@Override
			public void run()
			{
				try
				{
					Locales.setDefault(TURKISH);
					fail("Should timeout as other thread isn't finished with using the default locale");
				}
				catch(InterruptedException expectedException)
				{
					String currentThreadDescription = Thread.currentThread().getName();
					String startOfMessage = "Thread (" + currentThreadDescription + ") " + "Waited for 0 seconds on";

					String endOfMessage = "[Locked by thread " + ownerThread + "] " + "to finish using " + CANADA + " but timed out";
					assertThat(expectedException.getMessage()).startsWith(startOfMessage).endsWith(endOfMessage);
					// Verify that this thread can't reset the locale
					assertThat(Locales.resetDefaultLocale()).isNull();
					assertThat(Locale.getDefault()).isEqualTo(CANADA);
				}
			}
		};

		// This thread should be allowed to set the locale several times during his "period"
		Locales.setDefault(CANADA);

		final AtomicReference<Throwable> failure = Atomics.newReference();

		thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler(){

			@Override
			public void uncaughtException(Thread t, Throwable e)
			{
				failure.set(e);
			}
		});

		thread.start();
		thread.join(EXPECTED_TEST_TIME_FOR_THIS_SUITE);
		if(failure.get() != null)
			throw failure.get();
	}

	@Test
	public void testThatNullContractsAreFollowed() throws Exception
	{
		new NullPointerTester().testStaticMethods(Locales.class, Visibility.PACKAGE);
	}
}
