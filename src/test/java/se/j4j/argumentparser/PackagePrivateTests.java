package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.StringParsers.optionParser;
import static se.j4j.argumentparser.StringParsers.Radix.BINARY;

import org.junit.Test;

import se.j4j.argumentparser.CommandLineParser.ArgumentIterator;
import se.j4j.argumentparser.StringParsers.Radix;
import se.j4j.argumentparser.commands.Build;
import se.j4j.argumentparser.internal.NumberType;

/**
 * Tests implementation details that has no meaning in the public API but can serve other purposes
 * such as to ease debugging. These tests can't reside in the internal package for (obvious)
 * visibility problems.
 */
public class PackagePrivateTests
{
	@Test
	public void testArgumentIteratorToString()
	{
		assertThat(ArgumentIterator.forSingleArgument("foobar").toString()).isEqualTo("[foobar]");
	}

	@Test
	public void testNumberTypeToString()
	{
		assertThat(NumberType.INTEGER.toString()).isEqualTo("integer");
	}

	@Test
	public void testCommandToString()
	{
		Build command = new Build();
		assertThat(command.toString()).isEqualTo(command.commandName());
	}

	@Test
	public void testLimiterOkResponseToString()
	{
		assertThat(Limit.OK.toString()).isEqualTo("OK");
	}

	@Test
	public void testThatOptionalArgumentDefaultsToTrue()
	{
		assertThat(optionParser(true).defaultValue()).isTrue();
		assertThat(optionParser(false).defaultValue()).isFalse();
	}

	@Test(expected = IllegalStateException.class)
	public void testThatNoLimitsCanNotPrintValidValues()
	{
		// Argument should fall back to StringParser#descriptionOfValidValues() instead when no
		// limit is applied
		Limiters.noLimits().descriptionOfValidValues();
	}

	/**
	 * The compiler injects the valueOf method into the byte code for enums
	 * and the code coverage tool detects that
	 */
	@Test
	public void testEnumsForCodeCoverage()
	{
		assertThat(Radix.valueOf(BINARY.toString())).isEqualTo(BINARY);
	}

}
