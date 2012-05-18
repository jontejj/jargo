package se.j4j.argumentparser.exceptions;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentExceptions.forErrorCode;
import static se.j4j.argumentparser.ArgumentExceptions.ArgumentExceptionCodes.UNHANDLED_PARAMETER;

import org.junit.Test;

public class TestArgumentException
{
	@Test
	public void testThatMessageContainsErrorCode()
	{
		assertThat(forErrorCode(UNHANDLED_PARAMETER)).hasMessage("Error code: " + UNHANDLED_PARAMETER);
	}
}
