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
package se.j4j.argumentparser.exceptions;

import static org.junit.Assert.fail;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentBuilder.DefaultArgumentBuilder;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentExceptions;
import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.CommandLineParser;
import se.j4j.argumentparser.ParsedArguments;
import se.j4j.argumentparser.StringParsers;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

public class NullPointerTest
{
	@Test
	public void testThatNullContractsAreCheckedEagerly() throws ArgumentException
	{
		NullPointerTester npeTester = new NullPointerTester();
		npeTester.testStaticMethods(ArgumentExceptions.class, Visibility.PACKAGE);
		npeTester.testStaticMethods(ArgumentFactory.class, Visibility.PACKAGE);
		npeTester.testStaticMethods(StringParsers.class, Visibility.PACKAGE);
		npeTester.testStaticMethods(CommandLineParser.class, Visibility.PACKAGE);

		DefaultArgumentBuilder<Integer> builder = integerArgument("--name");
		npeTester.testInstanceMethods(builder, Visibility.PROTECTED);

		Argument<Integer> argument = builder.build();

		npeTester.testInstanceMethods(argument, Visibility.PROTECTED);

		CommandLineParser parser = CommandLineParser.withArguments(argument);
		npeTester.testInstanceMethods(parser, Visibility.PROTECTED);

		ParsedArguments result = parser.parse();
		npeTester.testInstanceMethods(result, Visibility.PROTECTED);

		try
		{
			parser.parse("--a");
			fail("--a should have been unhandled");
		}
		catch(ArgumentException expected)
		{
			npeTester.testAllPublicInstanceMethods(expected);
		}
	}
}
