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
package se.softhouse.jargo.stringparsers;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.softhouse.jargo.StringParsers.asFunction;
import static se.softhouse.jargo.StringParsers.integerParser;

import java.util.List;

import org.junit.Test;

import se.softhouse.jargo.StringParser;
import se.softhouse.jargo.StringParsers;

/**
 * Tests for {@link StringParsers#asFunction(StringParser)}
 */
public class StringParserAsFunctionTest
{
	@Test
	public void testAsFunction()
	{
		List<Integer> expected = asList(1, 3, 2);

		List<Integer> result = transform(asList("1", "3", "2"), asFunction(integerParser()));
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void testInvalidInputToFunction()
	{
		try
		{
			asFunction(integerParser()).apply("a1");
			fail("a1 should cause an exception");
		}
		catch(IllegalArgumentException e)
		{
			assertThat(e.getMessage()).startsWith("'a1' is not a valid integer");
		}
	}
}
