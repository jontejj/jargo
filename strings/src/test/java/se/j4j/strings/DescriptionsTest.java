package se.j4j.strings;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.strings.Descriptions.cache;

import org.junit.Test;

import se.j4j.testlib.Serializer;
import se.j4j.testlib.UtilityClassTester;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

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
	public void testThatCachedDescriptionIsOnlyCreatedOnce()
	{
		ProfilingDescription description = new ProfilingDescription();
		Description cachedDescription = cache(description);
		assertThat(cachedDescription.description()).isEqualTo("foo");
		assertThat(cachedDescription.description()).isEqualTo("foo");
		assertThat(cachedDescription.toString()).isEqualTo("foo");
		assertThat(description.timesCalled).isEqualTo(1);
	}

	private static class ProfilingDescription implements Description
	{
		int timesCalled;

		@Override
		public String description()
		{
			timesCalled++;
			return "foo";
		}
	}

	@Test
	public void testUtilityClassDesign()
	{
		new NullPointerTester().testStaticMethods(Descriptions.class, Visibility.PACKAGE);
		UtilityClassTester.testUtilityClassDesign(Descriptions.class);
	}
}
