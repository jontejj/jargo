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
package se.softhouse.comeon.testlib;

import static org.fest.assertions.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * The reasoning behind testing code that doesn't do anything is to achieve 100%
 * code coverage and to notice when the code
 * coverage drops. Otherwise one could always think, hey I don't have 100% code
 * coverage anymore but it's PROBABLY because of my private constructors
 * (or any other code that's not used but preferred to have).
 * This makes it easy to spot untested methods without having to check that it
 * only was a private constructor etc.
 */
public final class UtilityClassTester
{
	private UtilityClassTester()
	{
	}

	/**
	 * Detects if a utility class isn't final or if its no-args constructor
	 * isn't private. Also calls the constructor to get code coverage for it.
	 * The constructor is allowed to throw exceptions.
	 */
	public static void testUtilityClassDesign(Class<?> ... utilityClasses)
	{
		for(Class<?> clazz : utilityClasses)
		{
			try
			{
				Constructor<?> constructor = clazz.getDeclaredConstructor();
				assertThat(constructor.getModifiers() & Modifier.PRIVATE).as("Constructor for " + clazz + " should be private.")
						.isEqualTo(Modifier.PRIVATE);
				constructor.setAccessible(true);
				assertThat(constructor.newInstance()).isNotNull();
				assertThat(clazz.getModifiers() & Modifier.FINAL).as("Utility class " + clazz + " not final").isEqualTo(Modifier.FINAL);
			}
			catch(NoSuchMethodException e)
			{
				throw new IllegalArgumentException("No no-arg constructor found for: " + clazz.getName(), e);
			}
			catch(InvocationTargetException e)
			{
				// Some utility classes like to throw from their private constructor just to be on
				// the safe side, let's accept that
			}
			catch(InstantiationException e)
			{
				throw new IllegalArgumentException("Utility class " + clazz.getName() + " may not be abstract", e);
			}
			catch(IllegalAccessException e)
			{
				// Let's just say that if we're not able to create an instance it's fine, after all
				// that's what we're testing here
			}
		}
	}
}
