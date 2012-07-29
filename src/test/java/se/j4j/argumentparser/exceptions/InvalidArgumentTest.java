package se.j4j.argumentparser.exceptions;

import static junit.framework.Assert.fail;
import static se.j4j.argumentparser.ArgumentExceptions.forInvalidValue;

import org.junit.Test;

import se.j4j.argumentparser.Description;

public class InvalidArgumentTest
{
	@Test
	public void testThatDescriptionForInvalidArgumentIsNotCalledWhenNotNeeded()
	{
		forInvalidValue("", new Description(){
			@Override
			public String description()
			{
				fail("description Should not be called as no getMessage is called");
				return "";
			}
		});
	}

	@Test
	public void testThatInvalidArgumentDoesNotRunToStringWhenItIsNotNeeded()
	{
		forInvalidValue(new FailingToString(), "");
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
