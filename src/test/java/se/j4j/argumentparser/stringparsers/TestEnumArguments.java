package se.j4j.argumentparser.stringparsers;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.enumArgument;

import java.util.List;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentExceptions.InvalidArgument;

public class TestEnumArguments
{
	public enum Action
	{
		start,
		stop,
		restart
	}

	@Test
	public void testEnumArgument() throws ArgumentException
	{
		Action action = enumArgument(Action.class).parse("stop");
		assertThat(action).isEqualTo(Action.stop);
	}

	public enum NoPossibleValues
	{
	}

	@Test
	public void testThatEnumsWithoutPossibleValuesAreInvalidated()
	{
		try
		{
			enumArgument(NoPossibleValues.class, "-UselessParameter");
			fail("Enum wasn't invalidated");
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected).hasMessage("NoPossibleValues has no possible values defined");
		}
	}

	@Test
	public void testEnumArgumentWithNames() throws ArgumentException
	{
		List<Action> action = enumArgument(Action.class, "-a", "--action").repeated().parse("-a", "stop", "--action", "start");
		assertThat(action).isEqualTo(asList(Action.stop, Action.start));
	}

	@Test
	public void testEnumArgumentUsage()
	{
		String usageText = enumArgument(Action.class).usage("");
		assertThat(usageText).contains("<Action>: [start | stop | restart]");
		assertThat(usageText).contains("Default: null");
	}

	public enum ShouldNotInitialize
	{
		VALUE;

		static
		{
			staticEnumCodeHaveBeenRun = true;
		};
	}

	static boolean staticEnumCodeHaveBeenRun = false;

	@Test
	public void testThatEnumIsNotInitializedUntilParse()
	{
		enumArgument(ShouldNotInitialize.class, "-UselessParameter");
		// TODO: how should this be solved?
		// assertThat(staticEnumCodeHaveBeenRun).isFalse();
	}

	@Test
	public void testInvalidEnumArgument() throws ArgumentException
	{
		try
		{
			enumArgument(Action.class).parse("break");
		}
		catch(InvalidArgument e)
		{
			assertThat(e.getMessage()).isEqualTo("'break' is not a valid Option, Expecting one of [start | stop | restart]");
		}
	}
}
