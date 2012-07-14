package se.j4j.argumentparser.providers;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.Providers.cachingProvider;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.Provider;

public class TestProviders
{
	@Test
	public void testCachingOfProvidedValue() throws ArgumentException
	{
		Provider<Integer> cachingProvider = cachingProvider(new ChangingProvider());

		Argument<Integer> n = integerArgument("-n").defaultValueProvider(cachingProvider).build();

		assertThat(n.usage("")).contains("Default: 0");
		assertThat(n.parse()).isZero();
	}

	@Test
	public void testThatCachingOfProvidedValueIsOnlyDoneWhenNeeded()
	{
		Provider<Integer> provider = new ChangingProvider();
		Provider<Integer> cachingProvider = cachingProvider(provider);

		Integer first = provider.provideValue();
		Integer cachedValue = cachingProvider.provideValue();

		assertThat(first).isZero();
		assertThat(cachedValue).isEqualTo(1);
	}

	@Test
	public void testThatValuesAreOnlyCreatedOneTimeEvenDuringHighContention() throws InterruptedException, BrokenBarrierException, TimeoutException
	{
		SlowProvider slowProvider = new SlowProvider();
		final CyclicBarrier startup = new CyclicBarrier(1001);
		final CyclicBarrier finishLine = new CyclicBarrier(1001);
		final AtomicReference<String> failure = new AtomicReference<String>();

		final Provider<Integer> cachingProvider = cachingProvider(slowProvider);

		for(int i = 0; i < 1000; i++)
		{
			new Thread(){
				@Override
				public void run()
				{
					try
					{
						await(startup);
						assertThat(cachingProvider.provideValue()).isZero();
						await(finishLine);
					}
					catch(Exception e)
					{
						failure.set(e.getMessage());
					}
				}
			}.start();
		}

		await(startup);
		assertThat(cachingProvider.provideValue()).isZero();
		await(finishLine);
		String failMessage = failure.get();
		if(failMessage != null)
		{
			fail(failMessage);
		}
	}

	private static void await(CyclicBarrier barrier) throws InterruptedException, BrokenBarrierException, TimeoutException
	{
		barrier.await(10, SECONDS);
	}
}
