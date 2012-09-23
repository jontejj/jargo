package se.j4j.testlib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Aids tests that needs to test how APIs behave when their objects are serialized
 */
public final class Serializer
{
	private Serializer()
	{
	}

	/**
	 * Serializes {@code objectToSerialize} and reads the serialized bytes into an object again.
	 * Requires that readReplace() isn't overridden to return a type not castable to {@code T}, for
	 * default implementations of {@link Serializable} this is true.
	 */
	public static <T extends Serializable> T clone(T objectToSerialize)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(objectToSerialize);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);

			@SuppressWarnings("unchecked")
			T deserializedObject = (T) ois.readObject();
			return deserializedObject;
		}
		catch(IOException e)
		{
			throw new IllegalArgumentException("Failed to clone: " + objectToSerialize, e);
		}
		catch(ClassNotFoundException e)
		{
			throw new IllegalArgumentException("Failed to clone: " + objectToSerialize, e);
		}
	}
}
