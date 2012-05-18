package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.StringSplitters.comma;

import org.junit.Test;

public class TestOptionalArguments
{
	@Test
	public void testDescription()
	{
		String usage = optionArgument("--enable-logging").usage("OptionArgument");
		assertThat(usage).contains("Default: false");
	}

	@Test
	public void testThatOptionalIsTrueWhenArgumentIsGiven() throws ArgumentException
	{
		assertThat(optionArgument("--disable-logging").parse("--disable-logging")).isTrue();
	}

	@Test
	public void testForDefaultTrue() throws ArgumentException
	{
		String usage = optionArgument("--disable-logging").defaultValue(true).usage("OptionArgument");
		assertThat(usage).contains("Default: true");

		assertThat(optionArgument("--disable-logging").defaultValue(true).parse()).isTrue();
	}

	@Test
	public void testThatOptionalArgumentsDefaultsToFalse() throws ArgumentException
	{
		assertThat(optionArgument("-l").parse()).isFalse();
		assertThat(StringParsers.optionParser(false).defaultValue()).isFalse();
	}

	@SuppressWarnings("deprecation")
	@Test(expected = IllegalStateException.class)
	public void testThatOptionalArgumentsCantHaveSeparators()
	{
		optionArgument("-l").separator("=");
	}

	@SuppressWarnings("deprecation")
	@Test(expected = IllegalStateException.class)
	public void testThatOptionalArgumentsCantHaveArity()
	{
		optionArgument("-l").arity(2);
	}

	@SuppressWarnings("deprecation")
	@Test(expected = IllegalStateException.class)
	public void testThatOptionalArgumentsCantConsumeAllArguments()
	{
		optionArgument("-l").consumeAll();
	}

	@SuppressWarnings("deprecation")
	@Test(expected = IllegalStateException.class)
	public void testThatOptionalArgumentsCantBeSplit()
	{
		optionArgument("-l").splitWith(comma());
	}
}
