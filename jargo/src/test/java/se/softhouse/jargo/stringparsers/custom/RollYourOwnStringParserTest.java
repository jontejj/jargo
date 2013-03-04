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
package se.softhouse.jargo.stringparsers.custom;

import static org.fest.assertions.Assertions.assertThat;
import static se.softhouse.jargo.ArgumentFactory.withParser;
import static se.softhouse.jargo.stringparsers.custom.DateTimeParser.dateArgument;
import static se.softhouse.jargo.utils.ExpectedTexts.expected;

import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Test;

import se.softhouse.jargo.ArgumentException;

public class RollYourOwnStringParserTest
{
	@Test
	public void testPort() throws ArgumentException
	{
		Port port = withParser(new PortParser()).names("--port").parse("--port", "8080");
		assertThat(port.port).isEqualTo(8080);
	}

	@Test
	public void testUniqueLetters() throws ArgumentException
	{
		Set<Character> letters = withParser(new UniqueLetters()).names("-l").parse("-l", "aabc");

		assertThat(letters).containsOnly('c', 'a', 'b');
	}

	@Test
	public void testDateArgument() throws ArgumentException
	{
		assertThat(dateArgument("--start").parse("--start", "2011-03-30")).isEqualTo(new DateTime("2011-03-30"));

		String usage = dateArgument("--start").usage();
		assertThat(usage).isEqualTo(expected("dateTime"));
	}
}
