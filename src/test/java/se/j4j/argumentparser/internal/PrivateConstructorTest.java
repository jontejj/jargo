package se.j4j.argumentparser.internal;

import static junit.framework.Assert.assertNotNull;
import static org.fest.assertions.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentExceptions;
import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.Describers;
import se.j4j.argumentparser.Descriptions;
import se.j4j.argumentparser.StringParsers;

/**
 * The reasoning behind testing code that doesn't do anything is to achieve 100%
 * code coverage and to notice when the code
 * coverage drops. Otherwise one could always think, hey I don't have 100% code
 * coverage anymore but it's PROBABLY because of my private constructors
 * (or any other code that's not used but preferred to have).
 * This makes it easy to spot untested methods without having to check that it
 * only was a private constructor etc.
 */
public class PrivateConstructorTest
{
	/**
	 * Detects if a utility class isn't final or if its no-args constructor
	 * isn't private. Also calls the constructor to get code coverage for it.
	 */
	public static void callPrivateConstructors(Class<?> ... classesToConstruct) throws Exception
	{
		for(Class<?> clazz : classesToConstruct)
		{
			assertThat(clazz.getModifiers() & Modifier.FINAL).as("Utility class " + clazz + " not final").isEqualTo(Modifier.FINAL);

			Constructor<?> constructor = clazz.getDeclaredConstructor();
			assertThat(constructor.getModifiers() & Modifier.PRIVATE).as("Constructor for " + clazz + " should be private.")
					.isEqualTo(Modifier.PRIVATE);
			constructor.setAccessible(true);
			assertNotNull(constructor.newInstance());
		}
	}

	@Test
	public void callForUtilityClasses() throws Exception
	{
		callPrivateConstructors(ArgumentFactory.class, Platform.class, StringsUtil.class, Descriptions.class, Predicates2.class, Finalizers.class,
								StringParsers.class, ArgumentExceptions.class, Describers.class, Texts.class);
	}
}
