package se.j4j.testlib;

import static org.fest.assertions.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Calls the compiler generated valueOf/toString methods for code coverage
 */
public final class EnumTester
{
	private EnumTester()
	{
	}

	/**
	 * Tests that the {@link Object#toString()} for each enum constant in {@code enumClass} returns
	 * the exact same as {@link Enum#name()} (default behavior).
	 * This guarantees that {@link Enum#valueOf(Class, String)} will work for each enum constant.
	 */
	public static <T extends Enum<T>> void testThatToStringIsCompatibleWithValueOf(Class<T> enumClass)
	{
		testThatToStringIsCompatibleWithValueOf(enumClass, false);
	}

	/**
	 * A variant of {@link #testThatToStringIsCompatibleWithValueOf(Class)} that doesn't care about
	 * the visibility of the {@code enumClass}
	 */
	public static <T extends Enum<T>> void testThatToStringIsCompatibleWithValueOfRegardlessOfVisibility(Class<T> enumClass)
	{
		testThatToStringIsCompatibleWithValueOf(enumClass, true);
	}

	private static <T extends Enum<T>> void testThatToStringIsCompatibleWithValueOf(Class<T> enumClass, boolean setAccessible)
	{
		Method compilerGeneratedMethod;
		try
		{
			compilerGeneratedMethod = enumClass.getMethod("valueOf", String.class);
			compilerGeneratedMethod.setAccessible(setAccessible);
		}
		catch(NoSuchMethodException e)
		{
			throw new IllegalArgumentException(enumClass.getSimpleName() + " is not an Enum", e);
		}
		int index = 0;
		for(T enumConstant : enumClass.getEnumConstants())
		{
			try
			{
				Object viaValueOf = compilerGeneratedMethod.invoke(null, enumConstant.toString());
				assertThat(viaValueOf).isSameAs(enumConstant);
			}
			catch(IllegalAccessException e)
			{
				throw new IllegalArgumentException("Failed to access valueOf method on " + enumClass.getName(), e);
			}
			catch(InvocationTargetException e)
			{
				throw new IllegalArgumentException("valueOf failed for: " + enumConstant + " (the enum constant at index " + index + ")", e);
			}
			index++;
		}
	}
}
