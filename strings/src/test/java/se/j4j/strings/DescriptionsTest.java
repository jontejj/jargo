package se.j4j.strings;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import se.j4j.testlib.Serializer;
import se.j4j.testlib.UtilityClassTester;

/**
 * Tests for {@link Descriptions}
 */
public class DescriptionsTest
{
	@Test
	public void testStaticStringAsDescription()
	{
		assertThat(Descriptions.withString("foo").toString()).isEqualTo("foo");
	}

	@Test
	public void testThatEmptyStringEqualsEmptyString()
	{
		assertThat(Descriptions.EMPTY_STRING.description()).isEqualTo("");
	}

	@Test
	public void testFormatDescription()
	{
		assertThat(Descriptions.format("hello %s %s", "foo", "bar").toString()).isEqualTo("hello foo bar");
	}

	@Test
	public void testToStringDescription()
	{
		assertThat(Descriptions.toString(42).toString()).isEqualTo("42");
	}

	@Test
	public void testDescriptionAsSerializable()
	{
		Description fortyTwo = Descriptions.toString(42);

		Description deserialized = Serializer.clone(Descriptions.asSerializable(fortyTwo));

		assertThat(deserialized.toString()).isEqualTo(fortyTwo.toString());
	}

	@Test
	public void testDescriptionInException()
	{
		Description fortyTwo = Descriptions.toString(42);
		assertThat(Descriptions.illegalArgument(fortyTwo)).hasMessage(fortyTwo.toString());
	}

	@Test
	public void testDescriptionInExceptionWithCause()
	{
		Description fortyTwo = Descriptions.toString(42);
		Exception cause = new Exception();
		assertThat(Descriptions.illegalArgument(fortyTwo, cause).getCause()).isEqualTo(cause);
	}

	@Test
	public void testUtilityClassDesign()
	{
		UtilityClassTester.testUtilityClassDesign(Descriptions.class);
	}
}
