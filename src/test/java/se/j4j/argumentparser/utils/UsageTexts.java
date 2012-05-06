package se.j4j.argumentparser.utils;

import static org.fest.assertions.Fail.fail;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class UsageTexts
{

	public static String get(String filename)
	{
		String usageText = null;
		try
		{
			usageText = Resources.toString(UsageTexts.class.getResource("/usage_texts/" + filename), Charsets.UTF_8);
		}
		catch(IOException e)
		{
			fail("Failed to load " + filename, e);
		}
		return usageText;
	}
}
