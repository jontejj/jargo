package se.j4j.testlib;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.junit.Test;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

public class SerializerTest
{
	@Test
	public void testThatChangesInDeserializedObjectDoesNotAffectSource()
	{
		SerializableObject source = new SerializableObject();
		SerializableObject deserialized = Serializer.clone(source);

		assertThat(deserialized.number).isZero();
		deserialized.number = 1;
		assertThat(source.number).isZero();
	}

	private static final class SerializableObject implements Serializable
	{
		private static final long serialVersionUID = 1L;

		int number = 0;
	}

	@Test
	public void testThatMissingClassThrows()
	{
		MissingClassForObject source = new MissingClassForObject();

		try
		{
			Serializer.clone(source);
			fail("Class should be missing");
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected).hasMessage("Failed to clone: foo class");
			assertThat(expected.getCause()).isSameAs(MissingClassForObject.EXCEPTION);
		}
	}

	private static final class MissingClassForObject implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private static final ClassNotFoundException EXCEPTION = new ClassNotFoundException("Missing class for serialized object");

		/**
		 * @param stream unused
		 * @throws ClassNotFoundException always
		 */
		private void readObject(ObjectInputStream stream) throws ClassNotFoundException
		{
			throw EXCEPTION;
		}

		@Override
		public String toString()
		{
			return "foo class";
		}
	}

	@Test
	public void testThatMissingBytesThrows()
	{
		MissingBytesForObject source = new MissingBytesForObject();

		try
		{
			Serializer.clone(source);
			fail("Bytes should be missing");
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected).hasMessage("Failed to clone: foo class");
			assertThat(expected.getCause()).isSameAs(MissingBytesForObject.EXCEPTION);
		}
	}

	private static final class MissingBytesForObject implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private static final IOException EXCEPTION = new IOException("missing bytes");

		/**
		 * @param stream unused
		 * @throws IOException always
		 */
		private void readObject(ObjectInputStream stream) throws IOException
		{
			throw EXCEPTION;
		}

		@Override
		public String toString()
		{
			return "foo class";
		}
	}

	@Test
	public void testNullContracts()
	{
		new NullPointerTester().testStaticMethods(Serializer.class, Visibility.PACKAGE);
	}
}
