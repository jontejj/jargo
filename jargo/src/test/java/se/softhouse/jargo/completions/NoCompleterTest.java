/* Copyright 2018 jonatanjonsson
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
package se.softhouse.jargo.completions;

import static org.fest.assertions.Assertions.assertThat;
import static se.softhouse.jargo.Arguments.stringArgument;

import org.junit.Test;

import se.softhouse.jargo.Argument;
import se.softhouse.jargo.CommandLineParser;
import se.softhouse.jargo.ParsedArguments;

/**
 * Tests for {@link Completers}
 */
public class NoCompleterTest
{
	@Test
	public void testThatNoCompletionsDoesNothing() throws Exception
	{
		Argument<String> arg = stringArgument("-j").build();
		ParsedArguments parsedArguments = CommandLineParser.withArguments(arg).noCompleter().parse("-j", "hello");
		assertThat(parsedArguments.get(arg)).isEqualTo("hello");
	}
}
