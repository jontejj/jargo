package se.j4j.argumentparser.utils;

import static com.google.common.base.Charsets.UTF_8;
import static org.fest.assertions.Fail.fail;

import java.io.IOException;
import java.net.URL;

import com.google.common.io.Resources;

public class UsageTexts
{

	/**
	 * Loads a usage text (from resources) named /usage_texts/<code>textName</code>.txt
	 * 
	 * @return the file as a string
	 */
	public static String expected(String textName)
	{
		String usageText = null;
		try
		{
			String filename = "/usage_texts/" + textName + ".txt";
			URL textURL = UsageTexts.class.getResource(filename);
			if(textURL == null)
			{
				fail(filename + " could not be found");
			}
			usageText = Resources.toString(textURL, UTF_8);
		}
		catch(IOException e)
		{
			fail("Failed to load " + textName, e);
		}
		return usageText;
	}
}
