package se.j4j.argumentparser.parsedvaluecallbacks;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.exceptions.ArgumentException;

public class TestParsedValueCallbacks
{
	@Test
	public void testThatParsedValueCallbackIsCalledForPropertyValues() throws ArgumentException
	{
		PrintStringsWhenParsed printer = new PrintStringsWhenParsed();

		Argument<Map<String, String>> strings = stringArgument("-S").parsedValueCallback(printer).asPropertyMap().build();

		ArgumentParser.forArguments(strings).parse("-Sfoo=hello", "-Sbar=world");

		assertThat(printer.printQueue).isEqualTo(Arrays.asList("hello", "world"));
	}

	@Test
	public void testThatParsedValueCallbackIsCalled() throws ArgumentException
	{
		PrintStringsWhenParsed printer = new PrintStringsWhenParsed();

		// TODO: test Splitter
		List<String> strings = stringArgument().parsedValueCallback(printer).consumeAll().parse("hello", "world");

		assertThat(printer.printQueue).isEqualTo(Arrays.asList("hello", "world"));
		try
		{
			strings.add("foo");
			fail("a list of repeated values should be unmodifiable");
		}
		catch(UnsupportedOperationException expected)
		{
		}
	}
}
