package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.booleanArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;

import org.junit.Test;

import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.internal.Texts;

/**
 * <pre>
 * An example of how to create a <b>hard to understand</b> command line invocation:
 * java program true 8090 Hello
 * Note that the order of the arguments matter and who knows what true
 * means? These are called indexed arguments in argumentparser.
 * 
 * This feature is allowed only because there exists some use cases
 * where indexed arguments makes sense, one example is:<br>
 * echo "Hello World"
 * </pre>
 */
public class IndexedArgumentTest
{
	@Test
	public void testIndexedArguments() throws ArgumentException
	{
		String[] args = {"true", "8090", "Hello"};

		Argument<Boolean> enableLogging = booleanArgument().description("Output debug information to standard out").build();

		Argument<Integer> port = integerArgument().defaultValue(8080).description("The port to start the server on.").build();

		Argument<String> greetingPhrase = stringArgument().description("A greeting phrase to greet new connections with").build();

		ParsedArguments arguments = CommandLineParser.forArguments(enableLogging, port, greetingPhrase).parse(args);

		assertThat(arguments.get(enableLogging)).isTrue();
		assertThat(arguments.get(port)).isEqualTo(8090);
		assertThat(arguments.get(greetingPhrase)).isEqualTo("Hello");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThatIndexedArgumentThatIsRequiredIsGivenFirstBeforeAnyOptionalIndexedArguments()
	{
		// string is required but the integer before it is optional,
		// this is a fault because indexed & required arguments should be given first
		// and lastly indexed & optional arguments can be given
		Argument<Integer> integer = integerArgument().build();
		Argument<String> string = stringArgument().required().build();

		CommandLineParser.forArguments(integer, string);
	}

	@Test
	public void testThatRequiredIndexedArgumentsHaveUniqueMetaDescriptions()
	{
		Argument<Integer> port = integerArgument().required().build();
		Argument<Integer> number = integerArgument().required().build();

		try
		{
			CommandLineParser.forArguments(port, number);
			fail("Non-unique meta description not detected");
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected).hasMessage(String.format(Texts.UNIQUE_METAS, port.metaDescriptionInRightColumn()));
		}
	}
}
