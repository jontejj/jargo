package se.j4j.argumentparser.exceptions;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentExceptions.withMessage;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.Description;
import se.j4j.argumentparser.Descriptions;

/**
 * Tests for {@link ArgumentException}
 */
public class ArgumentExceptionTest
{
	@Test
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
