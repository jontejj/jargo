package se.j4j.argumentparser.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class Serialization
{

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
		catch(IOException | ClassNotFoundException e)
		{
			throw new IllegalArgumentException("Failed to clone: " + objectToSerialize, e);
		}
	}
}
