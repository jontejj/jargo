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
package se.softhouse.jargo.exceptions;

import static org.junit.Assert.fail;
import static se.softhouse.jargo.ArgumentFactory.integerArgument;

import org.junit.Test;

import se.softhouse.jargo.Argument;
import se.softhouse.jargo.ArgumentException;
import se.softhouse.jargo.ArgumentExceptions;
import se.softhouse.jargo.ArgumentFactory;
import se.softhouse.jargo.CommandLineParser;
import se.softhouse.jargo.ParsedArguments;
import se.softhouse.jargo.StringParsers;
import se.softhouse.jargo.ArgumentBuilder.DefaultArgumentBuilder;

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
