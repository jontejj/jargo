package se.j4j.argumentparser.coverage;

import static junit.framework.Assert.assertNotNull;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.Radix.BINARY;
import static se.j4j.argumentparser.exceptions.ArgumentExceptionCodes.INVALID_PARAMETER;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.ArgumentFactory.Radix;
import se.j4j.argumentparser.Callbacks;
import se.j4j.argumentparser.DefaultValueProviders;
import se.j4j.argumentparser.Descriptions;
import se.j4j.argumentparser.Finalizers;
import se.j4j.argumentparser.Limiters;
import se.j4j.argumentparser.StringSplitters;
import se.j4j.argumentparser.exceptions.ArgumentExceptionCodes;
import se.j4j.argumentparser.internal.Lines;
import se.j4j.argumentparser.internal.ListUtil;
import se.j4j.argumentparser.internal.StringComparison;
import se.j4j.argumentparser.internal.StringsUtil;

/**
 * The reasoning behind testing code that doesn't do anything is to achieve 100%
 * code coverage and to notice when the code
 * coverage drops. Otherwise one could always think, hey I don't have 100% code
 * coverage anymore but it's PROBABLY because
 * of my private constructors (or any other code that's not used but needed).
 * This makes it easy to spot untested methods without having to check that it
 * just was a private constructor etc.
 */
public class TestForCodeCoverage
{
	/**
	 * Detects if a utility class isn't final or if its no-args constructor
	 * isn't private. Also calls the constructor to get code coverage for it.
	 * 
	 * @throws IOException
	 */
	@Test
	public void callPrivateConstructorsForCodeCoverage() throws NoSuchMethodException, InstantiationException, IllegalAccessException,
			InvocationTargetException, IOException
	{
		Class<?>[] classesToConstruct = {ArgumentFactory.class, Lines.class, ListUtil.class, StringsUtil.class, StringComparison.class,
				Descriptions.class, Limiters.class, Callbacks.class, Finalizers.class, StringSplitters.class, DefaultValueProviders.class};

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

	/**
	 * The compiler injects methods into the byte code for enums that
	 * the code coverage tool detects
	 */
	@Test
	public void testEnumsForCodeCoverage()
	{
		assertThat(ArgumentExceptionCodes.valueOf(INVALID_PARAMETER.toString())).isEqualTo(INVALID_PARAMETER);
		assertThat(Radix.valueOf(BINARY.toString())).isEqualTo(BINARY);
	}
}
