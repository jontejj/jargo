package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.utils.UsageTexts.expected;

import org.fest.assertions.Fail;
import org.junit.Test;

import se.j4j.argumentparser.ArgumentExceptions.UnexpectedArgumentException;
import se.j4j.strings.Description;

/**
 * Tests for {@link CommandLineParser#usage(String)}, {@link Argument#usage(String)} and
 * {@link ArgumentBuilder#usage(String)}
 * 
 * @formatter:off
 */
public class UsageTextTest
{
	@Test
	public void testUsageWithRequiredArguments()
	{
		String usage = stringArgument("-s").required().usage("RequiredArgumentDescription");
		assertThat(usage).isEqualTo(expected("requiredArgument"));
	}

	@Test
	public void testUsageWithOptionalArguments()
	{
		String usage = optionArgument("-l", "--enable-logging").usage("OptionalArgumentDescription");
		assertThat(usage).isEqualTo(expected("optionalArgument"));
	}

	@Test
	public void testUsageWithOptionalArgumentWithDescription()
	{
		String usage = optionArgument("-l", "--enable-logging").description("Enable logging").usage("OptionalArgumentDescriptionWithDescription");
		assertThat(usage).isEqualTo(expected("optionalArgumentWithDescription"));
	}

	@Test
	public void testUsageWithRepeatedArguments()
	{
		String usage = stringArgument("-s").repeated().metaDescription("greeting phrase")
				.description("A greeting phrase to greet new connections with").defaultValueDescription("Nothing")
				.usage("RepeatedArgumentDescription");

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
			assertThat(e.getMessageAndUsage("NonAllowedRepition")).isEqualTo(expected("unhandledRepition"));
		}
	}

	@Test
	public void testUsageForNoArguments()
	{
		// TODO: add possibility to add a description of the program as a whole
		// Introduce ProgramInformation class on top of String
		// ProgramInformation.name("NoArguments").description("What NoArguments does").printOneLiner()
		String usage = CommandLineParser.withArguments().usage("NoArguments");
		assertThat(usage).isEqualTo("Usage: NoArguments");
	}

	@Test
	public void testUsageForNoVisibleArguments()
	{
		String usage = CommandLineParser.withArguments(integerArgument().hideFromUsage().build()).usage("NoVisibleArguments");
		assertThat(usage).isEqualTo("Usage: NoVisibleArguments");
	}

	@Test
	public void testUsageWithArguments()
	{
		String usage = stringArgument().usage("SomeArguments");
		assertThat(usage).startsWith("Usage: SomeArguments [Options]");
	}

	@Test
	public void testThatHiddenArgumentsAreHidden()
	{
		Argument<String> hiddenArgument = stringArgument("--hidden").hideFromUsage().build();
		Argument<String> visibleArgument = stringArgument("--visible").build();
		CommandLineParser parser = CommandLineParser.withArguments(hiddenArgument, visibleArgument);
		String usage = parser.usage("HiddenArgument");

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
		String usage = integerArgument().defaultValue(1).repeated().usage("DefaultList");
		assertThat(usage).contains("Default: [1]");
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
				e.getUsage("ProgramName");
				fail("getUsage should throw when not enough information is available to produce a sane usage text");
			}
			catch(IllegalStateException illegalState)
			{
				assertThat(illegalState).hasMessage("No originParser set for ArgumentException. No usage available for ProgramName");
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
		String usage = CommandLineParser.withArguments(indexOne, indexTwo, namedOne, indexThree, namedTwo).usage("SortingOfIndexedArguments");

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
			assertThat(e.getMessageAndUsage("DidNotExpectFoo")).isEqualTo(expected("unexpectedArgument"));
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
			assertThat(e.getMessageAndUsage("DidNotExpectFoo")).isEqualTo(expected("unexpectedArgumentWithoutPrevious"));
		}
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
		}).usage("DescriptionTest");
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
