/* Copyright 2013 Jonatan Jönsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package se.softhouse.common.testlib;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

public class UtilityClassTesterTest
{
	@Test
	public void testThatContructorNeedsToBePrivate()
	{
		UtilityClassTester.testUtilityClassDesign(	UtilityClassTester.class, EnumTester.class, Explanation.class, ResourceLoader.class,
													Serializer.class, Constants.class);
	}

	@Test
	public void testThatContructorWithoutArgumentsExists()
	{
		try
		{
			UtilityClassTester.testUtilityClassDesign(UtilityClassWithNoArgConstructor.class);
			fail("Missing no-arg constructor not detected");
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected).hasMessage(String.format("No no-arg constructor found for: %s", UtilityClassWithNoArgConstructor.class.getName()));
			assertThat(expected.getCause()).isInstanceOf(NoSuchMethodException.class);
		}
	}

	private static final class UtilityClassWithNoArgConstructor
	{
		// The test shall verify that this constructor shouldn't exist
		@SuppressWarnings("unused")
		UtilityClassWithNoArgConstructor(int dummy)
		{

		}
	}

	@Test
	public void testThatSafeUtilityIsAccepted()
	{
		// Utility classes are allowed to throw exceptions from their private constructors
		UtilityClassTester.testUtilityClassDesign(SafeUtility.class);
	}

	private static final class SafeUtility
	{
		private SafeUtility()
		{
			throw new IllegalArgumentException("You may not create instances of a SafeUtility");
		}
	}

	@Test
	public void testThatAbstractUtilitiesThrows()
	{
		try
		{
			UtilityClassTester.testUtilityClassDesign(AbstractUtility.class);
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected).hasMessage(String.format("Utility class %s may not be abstract", AbstractUtility.class.getName()));
			assertThat(expected.getCause()).isInstanceOf(InstantiationException.class);
		}
	}

	private static abstract class AbstractUtility
	{
		private AbstractUtility()
		{
		}
	}

	@Test
	public void testNullContracts()
	{
		new NullPointerTester().testStaticMethods(UtilityClassTester.class, Visibility.PACKAGE);
	}
}
