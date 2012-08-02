package se.j4j.argumentparser.stringparsers;

import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.enumArgument;

import java.util.List;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.StringParsers;

/**
 * Tests for {@link ArgumentFactory#enumArgument(Class, String...)} and
 * {@link StringParsers#enumParser(Class)}
 */
public class EnumArgumentTest
{
	enum Action
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

	enum ShouldNotInitialize
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
		assertThat(staticEnumCodeHaveBeenRun).isFalse();
	}

	@Test
	public void testInvalidEnumArgument()
	{
		try
		{
			enumArgument(Action.class).parse("break");
		}
		catch(ArgumentException e)
		{
			assertThat(e.getMessage()).isEqualTo("'break' is not a valid Option, Expecting one of [start | stop | restart]");
		}
	}

	@Test
	public void testThatValidEnumOptionsAreNotConstructedIfNotNeeded()
	{
		try
		{
			enumArgument(NefariousToString.class).parse("TWO");
			fail("TWO should be an invalid enum value");
		}
		catch(ArgumentException invalidEnumValue)
		{
			try
			{
				invalidEnumValue.getMessage();
				fail("Nefarious toString not detected");
			}
			catch(IllegalStateException expectingNefariousBehavior)
			{

			}
		}
	}

	enum NefariousToString
	{
		ONE;

		@Override
		public String toString()
		{
			throw new IllegalStateException("Nefarious behavior not avoided");
		};
	}
}
