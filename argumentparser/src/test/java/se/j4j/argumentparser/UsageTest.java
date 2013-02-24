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
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.utils.ExpectedTexts.expected;

import org.fest.assertions.Fail;
import org.junit.Test;

import se.j4j.argumentparser.ArgumentExceptions.UnexpectedArgumentException;
import se.j4j.argumentparser.internal.Texts.ProgrammaticErrors;
import se.j4j.argumentparser.internal.Texts.UsageTexts;
import se.j4j.classes.Classes;
import se.j4j.strings.Description;

/**
 * Tests for {@link CommandLineParser#usage()}, {@link Argument#usage()} and
 * {@link ArgumentBuilder#usage()}
 * 
 * @formatter:off
 */
public class UsageTest
{
	@Test
	public void testThatProgramNameDefaultsToMainClassName()
	{
		String usage = integerArgument("-n").usage();
		assertThat(usage).startsWith(UsageTexts.USAGE_HEADER + Classes.mainClassName());
	}

	@Test
	public void testUsageWithOptionalArguments()
	{
		String usage = optionArgument("-l", "--enable-logging").usage();
		assertThat(usage).isEqualTo(expected("optionalArgument"));
	}

	@Test
	public void testUsageWithOptionalArgumentWithDescription()
	{
		String usage = optionArgument("-l", "--enable-logging").description("Enable logging").usage();
		assertThat(usage).isEqualTo(expected("optionalArgumentWithDescription"));
	}

	@Test
	public void testUsageWithRepeatedArguments()
	{
		String usage = stringArgument("-s").repeated().metaDescription("greeting phrase")
				.description("A greeting phrase to greet new connections with").defaultValueDescription("Nothing").usage();

		assertThat(usage).isEqualTo(expected("repeatedArguments"));
	}

	@Test
	public void testUsageWithUnallowedRepeationOfArgument()
	{
		try
		{
			stringArgument("-s").parse("-s", "foo", "-s", "bar");
			fail("Didn't handle repeated argument by throwing");
		}
		catch(ArgumentException e)
		{
			assertThat(e.getMessageAndUsage()).isEqualTo(expected("unhandledRepition"));
		}
	}

	@Test
	public void testUsageForNoArguments()
	{
		String usage = CommandLineParser.withArguments().programName("NoArguments").usage();
		assertThat(usage).isEqualTo("Usage: NoArguments");
	}

	@Test
	public void testUsageForNoVisibleArguments()
	{
		String usage = CommandLineParser.withArguments(integerArgument().hideFromUsage().build()).programName("NoVisibleArguments").usage();
		assertThat(usage).isEqualTo("Usage: NoVisibleArguments");
	}

	@Test
	public void testUsageWithArguments()
	{
		String usage = stringArgument().usage();
		assertThat(usage).startsWith("Usage: ");
	}

	@Test
	public void testThatHiddenArgumentsAreHidden()
	{
		Argument<String> hiddenArgument = stringArgument("--hidden").hideFromUsage().build();
		Argument<String> visibleArgument = stringArgument("--visible").build();
		CommandLineParser parser = CommandLineParser.withArguments(hiddenArgument, visibleArgument);
		String usage = parser.usage();

		assertThat(usage).isEqualTo(expected("hiddenArguments"));
	}

	@Test
	public void testThatHiddenArgumentsIsParsable() throws ArgumentException
	{
		assertThat(stringArgument("--hidden").hideFromUsage().parse("--hidden", "hello")).isEqualTo("hello");
	}

	@Test
	public void testUsageTextForDefaultList()
	{
		String usage = integerArgument().defaultValue(1).repeated().usage();
		assertThat(usage).contains("Default: 1");
	}

	@Test
	public void testArgumentNameSuggestions()
	{
		try
		{
			CommandLineParser.withArguments(integerArgument("--name").build(), integerArgument("--number").build(),
											integerArgument("--nothing").build()).parse("--namr");
			fail("--namr should not be a valid argument");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected.getMessageAndUsage()).isEqualTo(expected("argumentNameSuggestions"));
		}
	}

	@Test
	public void testThatUsageOnArgumentExceptionThrowsWhenNoUsageIsAvailable()
	{
		try
		{
			throw ArgumentExceptions.withMessage("");
		}
		catch(ArgumentException e)
		{
			try
			{
				e.getMessageAndUsage();
				fail("getMessageAndUsage should throw when not enough information is available to produce a sane usage text");
			}
			catch(NullPointerException expected)
			{
				assertThat(expected).hasMessage(ProgrammaticErrors.NO_USAGE_AVAILABLE);
			}
		}
	}

	@Test
	public void testSortingOrderForIndexedArguments()
	{
		Argument<String> indexOne = stringArgument().description("IndexOne").build();
		Argument<String> indexTwo = stringArgument().description("IndexTwo").build();
		Argument<String> indexThree = stringArgument().description("IndexThree").build();
		Argument<String> namedOne = stringArgument("-S").build();
		Argument<String> namedTwo = stringArgument("-T").build();
		String usage = CommandLineParser.withArguments(indexOne, indexTwo, namedOne, indexThree, namedTwo).usage();

		assertThat(usage).isEqualTo(expected("indexedArgumentsSortingOrder"));
	}

	@Test
	public void testUnexpectedArgument() throws ArgumentException
	{
		try
		{
			integerArgument("--number").parse("--number", "1", "foo");
			fail("foo should cause a throw as it's not handled");
		}
		catch(UnexpectedArgumentException e)
		{
			assertThat(e.getMessageAndUsage()).isEqualTo(expected("unexpectedArgument"));
		}
	}

	@Test
	public void testUnexpectedArgumentWithoutPreviousArgument() throws ArgumentException
	{
		try
		{
			integerArgument("--number").parse("foo");
			fail("foo should cause a throw as it's not handled");
		}
		catch(UnexpectedArgumentException e)
		{
			assertThat(e.getMessageAndUsage()).isEqualTo(expected("unexpectedArgumentWithoutPrevious"));
		}
	}

	@Test
	public void testProgramDescriptionInUsage()
	{
		String usage = CommandLineParser.withArguments(integerArgument().build()).programName("ProgramName")
				.programDescription("Program description of ProgramName").usage();

		assertThat(usage).isEqualTo(expected("programDescription"));
	}

	@Test
	public void testThatDescriptionsAreLazilyInitialized()
	{
		String usage = integerArgument("-n").description(new Description(){
			@Override
			public String description()
			{
				return "foo";
			}
		}).usage();
		assertThat(usage).contains("foo");
	}

	@Test
	public void testThatDescriptionsAreNotLazilyInitializedWhenNotNeeded()
	{
		try
		{
			integerArgument("-n").description(new FailingDescription()).parse("-n", "foo");
			fail("foo should cause a throw as it's an invalid integer");
		}
		catch(ArgumentException expected)
		{
		}
	}

	private static final class FailingDescription implements Description
	{
		@Override
		public String description()
		{
			Fail.fail("Description should not be called as no usage was printed");
			return "";
		}

	}
}
