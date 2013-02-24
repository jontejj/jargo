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
import static se.j4j.argumentparser.ArgumentFactory.booleanArgument;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.StringParsers;

/**
 * Tests for {@link ArgumentFactory#booleanArgument(String...)} and
 * {@link StringParsers#booleanParser()}
 */
public class BooleanArgumentTest
{
	@Test
	public void testThatBooleanParsesTrueOk() throws ArgumentException
	{
		boolean result = booleanArgument("-b").parse("-b", "true");
		assertThat(result).isTrue();
	}

	@Test
	public void testDescription()
	{
		String usage = booleanArgument("-b").usage();
		assertThat(usage).contains("<boolean>: true or false");
	}

	@Test
	public void testThatInvalidValuesIsTreatedAsFalse() throws ArgumentException
	{
		boolean result = booleanArgument("-b").parse("-b", "true or wait, no false");
		assertThat(result).isFalse();
	}

	@Test
	public void testThatBooleanDefaultsToFalse() throws ArgumentException
	{
		boolean result = booleanArgument("-b").parse();
		assertThat(result).isFalse();
	}
}
