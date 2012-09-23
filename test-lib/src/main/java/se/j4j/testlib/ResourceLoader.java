package se.j4j.testlib;

import static com.google.common.base.Charsets.UTF_8;

import java.io.IOException;
import java.net.URL;

import com.google.common.io.Resources;

/**
 * Aids tests with loading resource texts. These texts are often the expected result from some
 * operation.
 */
public final class ResourceLoader
{
	private ResourceLoader()
	{
	}

	/**
	 * Loads a file and reads it as an {@link com.google.common.base.Charsets#UTF_8 UTF_8} file.
	 * {@code resourceName} must start with a "/"
	 * 
	 * @return the file as a string
	 * @throws IllegalArgumentException if {@code resourceName} can't be found/loaded
	 */
	public static String get(String resourceName)
	{
		URL resourcePath = Resources.getResource(ResourceLoader.class, resourceName);
		String fileContent = null;
		try
		{
			fileContent = Resources.toString(resourcePath, UTF_8);
		}
		catch(IOException e)
		{
			throw new IllegalArgumentException("Failed to load: " + resourcePath, e);
		}
		return fileContent;
	}
}
