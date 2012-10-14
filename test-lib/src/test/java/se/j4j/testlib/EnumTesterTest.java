package se.j4j.testlib;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

public class EnumTesterTest
{
	@Test
	public void testThatValidEnumIsAccepted()
	{
		EnumTester.testThatToStringIsCompatibleWithValueOf(NiceEnum.class);
		EnumTester.testThatToStringIsCompatibleWithValueOfRegardlessOfVisibility(NiceEnum.class);
	}

	private enum NiceEnum
	{
		ONE,
		TWO;
	}

	@Test
	public void testThatInvalidEnumIsDetected()
	{
		try
		{
			EnumTester.testThatToStringIsCompatibleWithValueOf(InvalidEnum.class);
			fail("invalid toString of InvalidEnum should have been detected");
		}
		catch(AssertionError expected)
		{
		}
	}

	private enum InvalidEnum
	{
		ONE,
		TWO;

		@Override
		public String toString()
		{
			return TWO.name();
		}
	}

	@Test
	public void testThatInvalidToStringIsDetected()
	{
		try
		{
			EnumTester.testThatToStringIsCompatibleWithValueOf(InvalidValueOfEnum.class);
			fail("toString may not return null as it stops valueOf from working");
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected).hasMessage("valueOf failed for: null (the enum constant at index 1)");
			assertThat(expected.getCause()).isInstanceOf(InvocationTargetException.class);
		}
	}

	private enum InvalidValueOfEnum
	{
		ONE,
		TWO
		{
			@Override
			public String toString()
			{
				return null;
			}
		};
	}

	@Test
	public void testThatPackageProtectedValueOfIsNotCallable() throws ClassNotFoundException
	{
		try
		{
			Class<InvalidEnum> insideEnum = loadInvalidEnum("anotherpackage.", "NestedEnum$InsideEnum");
			EnumTester.testThatToStringIsCompatibleWithValueOf(insideEnum);
			fail("valueOf should not work reflectively for package private enums");
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected.getCause()).isInstanceOf(IllegalAccessException.class);
		}
	}

	@Test
	public void testThatNonEnumThrows() throws ClassNotFoundException
	{
		Class<InvalidEnum> insideEnum = loadInvalidEnum("", "EnumTesterTest");
		try
		{
			EnumTester.testThatToStringIsCompatibleWithValueOf(insideEnum);
			fail("class that's not an Enum wasn't detected");
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected).hasMessage("EnumTesterTest is not an Enum");
			assertThat(expected.getCause()).isInstanceOf(NoSuchMethodException.class);
		}
	}

	@SuppressWarnings("unchecked")
	private Class<InvalidEnum> loadInvalidEnum(String subPackageName, String className) throws ClassNotFoundException
	{
		String packageName = getClass().getPackage().getName() + "." + subPackageName;
		// Yeah, here we fool the type system
		return (Class<InvalidEnum>) Class.forName(packageName + className);
	}

	@Test
	public void testNullContracts()
	{
		new NullPointerTester().testStaticMethods(EnumTester.class, Visibility.PACKAGE);
	}
}
