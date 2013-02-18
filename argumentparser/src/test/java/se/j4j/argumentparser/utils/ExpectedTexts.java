package se.j4j.argumentparser.utils;

import se.j4j.classes.Classes;
import se.j4j.testlib.ResourceLoader;

public class ExpectedTexts
{
	/**
	 * Loads a usage text (from resources) named /usage_texts/{@code testName}.txt
	 * 
	 * @return the file as a string
	 */
	public static String expected(String testName)
	{
		String expectedUsage = ResourceLoader.get("/usage_texts/" + testName + ".txt");

		// Avoids having RemoteTestRunner or ForkedBooter in the .txt files. As the main class is
		// fetched by ProgramInformation.AUTO by default this avoids test runner specific code
		expectedUsage = expectedUsage.replaceFirst(testName, Classes.mainClassName());
		return expectedUsage;
	}
}
