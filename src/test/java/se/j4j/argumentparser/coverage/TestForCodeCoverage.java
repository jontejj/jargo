package se.j4j.argumentparser.coverage;

import static junit.framework.Assert.assertNotNull;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.exceptions.ArgumentExceptionCodes.INVALID_PARAMTER;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.ArgumentExceptionCodes;
import se.j4j.argumentparser.utils.Lines;
import se.j4j.argumentparser.utils.ListUtil;
import se.j4j.argumentparser.utils.StringComparison;
import se.j4j.argumentparser.utils.Strings;

/**
 * The reasoning behind testing code that doesn't do anything is to achieve 100%
 * code coverage and to notice when the code
 * coverage drops. Otherwise one could always think, hey I don't have 100% code
 * coverage anymore but it's PROBABLY because
 * of my private constructors (or any other code that's not used but needed).
 * This makes it easy to spot untested methods without having to check that it
 * just was a
 * private constructor etc.
 */
public class TestForCodeCoverage
{
	/**
	 * IMO it's best to use reflection here since otherwise you would have to
	 * either get a better code coverage tool that
	 * ignores these constructors or somehow tell the code coverage tool to
	 * ignore the method
	 * (perhaps an Annotation or a configuration file) because then you would be
	 * stuck with a specific code coverage tool.
	 * In a perfect world all code coverage tools would ignore private
	 * constructors that belong to a final class
	 * because the constructor is there as a "security" measure nothing else:)
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void callPrivateConstructorsForCodeCoverage() throws NoSuchMethodException, InstantiationException, IllegalAccessException,
			InvocationTargetException
	{
		Class<?>[] classesToConstruct = {ArgumentFactory.class, Lines.class, ListUtil.class, Strings.class, StringComparison.class};

		for(Class<?> clazz : classesToConstruct)
		{
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			assertNotNull(constructor.newInstance());
		}
	}

	/**
	 * Apparently the compiler injects methods into the byte code for enums that
	 * the code coverage tool detects
	 */
	@Test
	public void testEnumsForCodeCoverage()
	{
		assertThat(ArgumentExceptionCodes.valueOf(INVALID_PARAMTER.toString())).isEqualTo(INVALID_PARAMTER);
	}

	/**
	 * Calls toString methods for objects that's used during debugging
	 * 
	 * @throws ArgumentException
	 */
	@Test
	public void testDebugCodeForCodeCoverage() throws ArgumentException
	{
	}
}
