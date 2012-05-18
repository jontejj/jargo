package se.j4j.argumentparser;

import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.StringSplitters.comma;

import org.junit.Test;

public class TestArityArguments
{
	// This is what's being tested
	@SuppressWarnings("deprecation")
	@Test(expected = IllegalStateException.class)
	public void testThatArityAndSplitWithIncompabilityIsEnforced()
	{
		integerArgument().arity(2).splitWith(comma());
	}
}
