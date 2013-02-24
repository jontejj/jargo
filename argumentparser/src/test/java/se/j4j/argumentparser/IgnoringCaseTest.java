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
package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;

import java.util.Map;

import org.junit.Test;

/**
 * Tests for {@link ArgumentBuilder#ignoreCase()}
 */
public class IgnoringCaseTest
{
	@Test
	public void testIgnoringCase() throws ArgumentException
	{
		Argument<Boolean> help = optionArgument("-h", "--help", "-help", "?").ignoreCase().build();

		assertThat(help.parse("-H")).as("unhandled capital letter for ignore case argument").isTrue();
		assertThat(help.parse("-HELP")).isTrue();
		assertThat(help.parse("--help")).isTrue();
	}

	@Test
	public void testWithPropertyMap() throws ArgumentException
	{
		Map<String, Integer> numbers = integerArgument("-n").asPropertyMap().ignoreCase().parse("-nsmall=1", "-Nbig=5");

		assertThat(numbers.get("small")).isEqualTo(1);
		assertThat(numbers.get("big")).isEqualTo(5);
	}

	@Test(expected = ArgumentException.class)
	public void testThatLowerCaseArgumentIsNotReturnedWhenNotIgnoringCase() throws ArgumentException
	{
		optionArgument("-help").parse("-Help");
	}
}