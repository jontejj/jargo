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
package se.j4j.argumentparser.stringparsers;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.charArgument;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.StringParsers;

/**
 * Tests for {@link ArgumentFactory#charArgument(String...)} and {@link StringParsers#charParser()}
 */
public class CharArgumentTest
{
	@Test
	public void testValidCharacter() throws ArgumentException
	{
		Character z = charArgument("-c").parse("-c", "Z");

		assertThat(z).isEqualTo('Z');
	}

	@Test
	public void testInvalidLength()
	{
		try
		{
			charArgument("-c").parse("-c", "abc");
		}
		catch(ArgumentException e)
		{
			assertThat(e.getMessage()).isEqualTo("'abc' is not a valid character");
		}
	}

	@Test
	public void testDescription()
	{
		String usage = charArgument("-c").usage();
		assertThat(usage).contains("<character>: any unicode character");
		assertThat(usage).contains("Default: the Null character");

		assertThat(charArgument("-c").defaultValue(null).usage()).contains("Default: null");
		assertThat(charArgument("-c").defaultValue('A').usage()).contains("Default: A");
	}

	@Test
	public void testThatCharDefaultsToZero() throws ArgumentException
	{
		Character c = charArgument("-c").parse();
		assertThat(c.charValue()).isEqualTo((char) 0);
	}
}
