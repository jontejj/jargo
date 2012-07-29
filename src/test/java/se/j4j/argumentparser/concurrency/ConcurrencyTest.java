package se.j4j.argumentparser.concurrency;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.bigIntegerArgument;
import static se.j4j.argumentparser.ArgumentFactory.booleanArgument;
import static se.j4j.argumentparser.ArgumentFactory.byteArgument;
import static se.j4j.argumentparser.ArgumentFactory.charArgument;
import static se.j4j.argumentparser.ArgumentFactory.doubleArgument;
import static se.j4j.argumentparser.ArgumentFactory.fileArgument;
import static se.j4j.argumentparser.ArgumentFactory.floatArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.longArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.shortArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.stringparsers.custom.DateTimeParser.dateArgument;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.fest.assertions.Description;
import org.joda.time.DateTime;
import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.CommandLineParser;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.utils.UsageTexts;

public class ConcurrencyTest
{
	final Argument<Boolean> enableLogging = optionArgument("-l", "--enable-logging").description("Output debug information to standard out").build();

	final Argument<Integer> port = integerArgument("-p", "--listen-port").required().description("The port to start the server on.").build();

	final Argument<String> greetingPhrase = stringArgument().required().description("A greeting phrase to greet new connections with").build();

	final Argument<Long> longArgument = longArgument("--long").build();

	final Argument<BigInteger> bigInteger = bigIntegerArgument("--big").build();

	final Argument<DateTime> date = dateArgument("--date").build();

	final Argument<Double> doubleArgument = doubleArgument("--double").build();

	final Argument<Short> shortArgument = shortArgument("--short").build();

	final Argument<Byte> byteArgument = byteArgument("--byte").build();

	final Argument<File> file = fileArgument("--file").defaultValueDescription("The current directory").build();

	final Argument<String> string = stringArgument("--string").build();

	final Argument<Character> charArgument = charArgument("--char").build();

	final Argument<Boolean> bool = booleanArgument("--bool").build();

	final Argument<Map<String, Boolean>> propertyArgument = booleanArgument("-B").asPropertyMap().build();

	final Argument<List<Boolean>> arityArgument = booleanArgument("--arity").arity(6).build();

	final Argument<List<Integer>> repeatedArgument = integerArgument("--repeated").repeated().build();

	final Argument<List<Float>> splittedArgument = floatArgument("--split").separator("=").splitWith(",").build();

	// The shared instance that the different threads will use
	final CommandLineParser parser = CommandLineParser.forArguments(greetingPhrase, enableLogging, port, longArgument, bigInteger, date,
																	doubleArgument, shortArgument, byteArgument, file, string, charArgument, bool,
																	propertyArgument, arityArgument, repeatedArgument, splittedArgument);

	final String expectedUsageText = UsageTexts.expected("allFeaturesInUsage");

	// Amount of test harness
	private static final int ITERATION_COUNT = 300;

	private static final int RUNNERS_PER_PROCESSOR = 3; // We want the threads
														// to fight for CPU time

	private static final int nrOfConcurrentRunners = Runtime.getRuntime().availableProcessors() * RUNNERS_PER_PROCESSOR;

	/**
	 * Used by other threads to report failure
	 */
	private final AtomicReference<Throwable> failure = new AtomicReference<Throwable>(null);
	private CountDownLatch activeWorkers;
	private CyclicBarrier startup;
	private CyclicBarrier parseDone;

	@Test
	public void test()
	{
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nrOfConcurrentRunners);
		startup = new CyclicBarrier(nrOfConcurrentRunners);
		parseDone = new CyclicBarrier(nrOfConcurrentRunners);
		activeWorkers = new CountDownLatch(nrOfConcurrentRunners);
		for(int i = 0; i < nrOfConcurrentRunners; i++)
		{
			executor.execute(new ArgumentParseRunner(i));
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
				fail("Failed in a thread in concurrency test: " + failure.get());
			}
		}
		assertThat(executor.shutdownNow()).isEmpty();
	}

	private final class ArgumentParseRunner implements Runnable
	{
		private final int offset;
		private final Thread originThread;
		private ParsedArguments arguments;

		public ArgumentParseRunner(int offset)
		{
			this.offset = offset;
			originThread = Thread.currentThread();
		}

		@Override
		public void run()
		{
			int portNumber = 8090 + offset;

			DateTime time = DateTime.parse(new DateTime("2010-01-01").plusMillis(offset).toString());
			short shortNumber = (short) (1232 + offset);
			byte byteNumber = (byte) (123 + offset);
			long longNumber = 1234567890L + offset;
			BigInteger bigNumber = BigInteger.valueOf(12312313212323L + offset);
			double doubleNumber = 5.344343 + offset;
			String str = "TjosanHejsan" + offset;

			Map<String, Boolean> propertyMap = new HashMap<String, Boolean>();
			propertyMap.put("foo", true);
			propertyMap.put("bar", false);

			String inputArguments = "-l -p " + portNumber + " Hello --long " + longNumber + " --big " + bigNumber + " --date " + time + " --double "
					+ doubleNumber + " --short " + shortNumber + " --byte " + byteNumber + " --file /Users/ --string " + str
					+ " --char T --bool true -Bfoo=true -Bbar=false" + " --arity true false true false true false --repeated 1 --repeated " + offset
					+ " --split=1.234," + (2.4343f + offset) + ",5.23232";

			try
			{
				String[] args = inputArguments.split(" ");
				// Let all threads prepare the input and start processing at the
				// same time
				startup.await(10, TimeUnit.SECONDS);

				for(int i = 0; i < ITERATION_COUNT; i++)
				{
					arguments = parser.parse(args);

					// Let all threads assert at the same time
					parseDone.await(10, TimeUnit.SECONDS);

					checkThat(enableLogging).isEqualTo(true);
					checkThat(port).isEqualTo(portNumber);
					checkThat(greetingPhrase).isEqualTo("Hello");
					checkThat(longArgument).isEqualTo(longNumber);
					checkThat(bigInteger).isEqualTo(bigNumber);
					checkThat(date).isEqualTo(time);
					checkThat(doubleArgument).isEqualTo(doubleNumber);
					checkThat(shortArgument).isEqualTo(shortNumber);
					checkThat(byteArgument).isEqualTo(byteNumber);
					checkThat(file).isEqualTo(new File("/Users/"));
					checkThat(string).isEqualTo(str);
					checkThat(charArgument).isEqualTo('T');
					checkThat(bool).isEqualTo(true);
					checkThat(arityArgument).isEqualTo(asList(true, false, true, false, true, false));
					checkThat(repeatedArgument).isEqualTo(asList(1, offset));
					checkThat(splittedArgument).isEqualTo(asList(1.234f, 2.4343f + offset, 5.23232f));
					checkThat(propertyArgument).isEqualTo(propertyMap);

					if(i % 10 == 0) // As usage is expensive to create only test this sometimes
					{
						String usage = parser.usage("HelloWorld");
						assertThat(usage).isEqualTo(expectedUsageText);
					}
				}
			}
			catch(AssertionError e)
			{
				e.fillInStackTrace();
				failure.set(e);
				originThread.interrupt();
				return;
			}
			catch(Exception e)
			{
				e.fillInStackTrace();
				failure.set(e);
				originThread.interrupt();
				return;
			}
			activeWorkers.countDown();
		}

		/**
		 * Verifies that an argument received an expected value
		 */
		public <T> Checker<T> checkThat(Argument<T> argument)
		{
			return new Checker<T>(argument);
		}

		private class Checker<T>
		{
			Argument<T> arg;

			public Checker(Argument<T> argument)
			{
				arg = argument;
			}

			public void isEqualTo(final T expectation)
			{
				final T parsedValue = arguments.get(arg);
				Description description = new Description(){
					// In a concurrency test it makes a big performance difference
					// with lazily created descriptions
					@Override
					public String value()
					{
						return "Failed to match: " + arg + ", actual: " + parsedValue + ", expected: " + expectation;
					}
				};
				assertThat(parsedValue).as(description).isEqualTo(expectation);
			}
		}
	}
}
