package se.j4j.argumentparser.callbacks;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.StringSplitters.comma;

import java.util.Arrays;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.Callback;
import se.j4j.argumentparser.Callbacks;

import com.google.common.collect.ImmutableList;

public class TestCallbacks
{
	@Test
	public void testThatCallbacksAreCalled() throws ArgumentException
	{
		PrintStringsWhenParsed printer = new PrintStringsWhenParsed();
		stringArgument().callbackForValues(printer).parse("hello");

		assertThat(printer.printQueue).isEqualTo(Arrays.asList("hello"));
	}

	@Test
	public void testThatCallbacksAreCalledForPropertyValues() throws ArgumentException
	{
		PrintStringsWhenParsed printer = new PrintStringsWhenParsed();
		stringArgument("-S").callbackForValues(printer).asPropertyMap().parse("-Sfoo=hello", "-Sbar=world");

		assertThat(printer.printQueue).isEqualTo(Arrays.asList("hello", "world"));
	}

	@Test
	public void testThatCallbacksAreCalledForValuesInLists() throws ArgumentException
	{
		PrintStringsWhenParsed printer = new PrintStringsWhenParsed();

		stringArgument().callbackForValues(printer).consumeAll().parse("hello", "world");

		assertThat(printer.printQueue).isEqualTo(Arrays.asList("hello", "world"));
	}

	@Test
	public void testThatCallbacksAreCalledForValuesFromSplitter() throws ArgumentException
	{
		PrintStringsWhenParsed printer = new PrintStringsWhenParsed();

		stringArgument().callbackForValues(printer).splitWith(comma()).parse("hello,world");

		assertThat(printer.printQueue).isEqualTo(Arrays.asList("hello", "world"));
	}

	private static final class ProfilingCallback implements Callback<String>
	{
		int callsMade;

		@Override
		public void parsedValue(String parsedValue)
		{
			callsMade++;
		}
	}

	@Test
	public void testMultipleCallbacks() throws ArgumentException
	{
		ProfilingCallback profiler = new ProfilingCallback();

		stringArgument().callbackForValues(Callbacks.compound(ImmutableList.of(profiler, profiler))).parse("foo");
		assertThat(profiler.callsMade).isEqualTo(2);

		profiler.callsMade = 0;
		stringArgument().callbackForValues(Callbacks.compound(profiler, Callbacks.<String>noCallback())).parse("bar");
		assertThat(profiler.callsMade).isEqualTo(1);

		profiler.callsMade = 0;
		stringArgument().callbackForValues(Callbacks.compound(profiler, profiler)).parse("zoo");
		assertThat(profiler.callsMade).isEqualTo(2);
	}

	@Test
	public void testThatCallbacksAreClearable() throws ArgumentException
	{
		ProfilingCallback profiler = new ProfilingCallback();

		stringArgument("-n").callbackForValues(profiler).repeated().clearCallbacks().parse("-n", "1", "-n", "-2");

		assertThat(profiler.callsMade).isZero();
	}
}
