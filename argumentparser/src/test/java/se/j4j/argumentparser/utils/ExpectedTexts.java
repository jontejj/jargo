package se.j4j.argumentparser.utils;

import se.j4j.testlib.ResourceLoader;

public class ExpectedTexts
{
	/**
	 * Loads a usage text (from resources) named /usage_texts/{@code textName}.txt
	 * 
	 * @return the file as a string
	 */
	public static String expected(String textName)
	{
		return ResourceLoader.get("/usage_texts/" + textName + ".txt");
	}
}
