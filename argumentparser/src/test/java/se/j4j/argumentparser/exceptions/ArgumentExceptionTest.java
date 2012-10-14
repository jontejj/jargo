package se.j4j.argumentparser.exceptions;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentExceptions.withMessage;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.CommandLineParser;
import se.j4j.strings.Description;
import se.j4j.strings.Descriptions;
import se.j4j.testlib.Serializer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Tests for {@link ArgumentException}
 */
public class ArgumentExceptionTest
{
	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Testing that invocation doesn't call description")
	public void testThatDescriptionIsNotCalledWhenNotNeeded()
	{
		withMessage(new Description(){
			@Override
			public String description()
			{
				fail("description Should not be called as no getMessage is called");
				return "";
			}
		});
	}

	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Testing that argument exceptions lazily loads the description")
	public void testThatToStringIsNotRunWhenItIsNotNeeded()
	{
		withMessage(Descriptions.format("%s", new FailingToString()));
	}

	@Test
	public void testThatToStringIsUsedByGetMessage()
	{
		assertThat(withMessage(new Object(){
			@Override
			public String toString()
			{
				return "foobar";
			}
		})).hasMessage("foobar");
	}

	@Test
	public void testThatUsageIsAvailableAfterSerialization()
	{
		Argument<Integer> number = integerArgument("-n").build();
		Argument<String> string = stringArgument("-s").build();

		try
		{
			CommandLineParser.withArguments(number, string).parse("-n");
			fail("-n argument should require an integer parameter");
		}
		catch(ArgumentException expected)
		{
			String usageBeforeSerialization = expected.getMessageAndUsage("SerializationTest");
			ArgumentException revivedException = Serializer.clone(expected);
			assertThat(revivedException.getMessageAndUsage("SerializationTest")).isEqualTo(usageBeforeSerialization);
		}
	}

	@Test
	public void testThatDifferentProgramNamesWorksAfterSerialization()
	{
		Argument<Integer> number = integerArgument("-n").build();
		try
		{
			CommandLineParser.withArguments(number).parse("-n");
			fail("-n argument should require an integer parameter");
		}
		catch(ArgumentException expected)
		{
			ArgumentException revivedException = Serializer.clone(expected);
			assertThat(revivedException.getMessageAndUsage("SerializationTestOne")).contains("SerializationTestOne");
			assertThat(revivedException.getMessageAndUsage("SerializationTestTwo")).contains("SerializationTestTwo");
		}
	}

	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Testing that argument exceptions lazily loads the toString")
	public void testThatWithMessageDoesNotRunToStringWhenItIsNotNeeded()
	{
		withMessage(new FailingToString());
	}

	private static final class FailingToString
	{
		@Override
		public String toString()
		{
			fail("toString Should not be called as no getMessage is called");
			return "";
		}
	}
}
