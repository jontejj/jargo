package se.j4j.testlib;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import se.j4j.testlib.Launcher.LaunchedProgram;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

/**
 * Tests for {@link Launcher}
 */
public class LauncherTest
{
	@Test
	public void testThatOutputAndErrorIsCapturedFromLaunchedProgram() throws IOException, InterruptedException
	{
		LaunchedProgram program = Launcher.launch(HelloWorldProgram.class);
		assertThat(program.errors).isEqualTo("FooBar");
		assertThat(program.output).isEqualTo("HelloWorld");
	}

	private static class HelloWorldProgram
	{
		@SuppressWarnings("unused")
		public static void main(String[] args)
		{
			System.out.print("HelloWorld");
			System.err.print("FooBar");
		}
	}

	@Test
	public void testThatClassWithoutMainIsInvalidated() throws IOException, InterruptedException
	{
		try
		{
			Launcher.launch(InvalidMainClass.class);
			fail("InvalidMainClass should have to have a static main method to be launchable");
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected).hasMessage("No main method found on: " + InvalidMainClass.class.getName());
		}
	}

	private static class InvalidMainClass
	{
		// No main(String[] args)
	}

	@Test
	public void testThatClassWithStaticMainButWithWrongParametersIsInvalidated() throws IOException, InterruptedException
	{
		try
		{
			Launcher.launch(WrongArgParametersMainClass.class);
			fail("InvalidModifierMainClass should have to have a static main method to be launchable");
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected).hasMessage("No main method found on: " + WrongArgParametersMainClass.class.getName());
		}
	}

	private static class WrongArgParametersMainClass
	{
		@SuppressWarnings("unused")
		public void main(Object[] args) // Should be String[] args
		{

		}
	}

	@Test
	public void testThatClassWithoutStaticMainIsInvalidated() throws IOException, InterruptedException
	{
		try
		{
			Launcher.launch(InvalidModifierMainClass.class);
			fail("InvalidModifierMainClass should have to have a static main method to be launchable");
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected).hasMessage(InvalidModifierMainClass.class.getName()
													+ "'s main method needs to be static and public for it to be launchable");
		}
	}

	private static class InvalidModifierMainClass
	{
		@SuppressWarnings("unused")
		public void main(String[] args) // No static modifier
		{

		}
	}

	@Test
	public void testThatDebugInformationContainsJavaAndClassPathInformation() throws IOException, InterruptedException
	{
		String debug = Launcher.launch(HelloWorldProgram.class).debugInformation();
		assertThat(debug).contains("java").contains("classpath");
	}

	@Test
	public void testNullContracts()
	{
		new NullPointerTester().testStaticMethods(Launcher.class, Visibility.PACKAGE);
	}

	@Test
	public void testUtilityClassDesign()
	{
		UtilityClassTester.testUtilityClassDesign(Launcher.class);
	}
}
