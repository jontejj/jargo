package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.command;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;

import org.junit.Test;

/**
 * Tests for a batch of short-named optional arguments.
 * For instance when "-fs" is used instead of "-f -s"
 */
public class CombinedOptionsTest
{
	@Test
	public void testSeveralOptionsInOneArgument() throws ArgumentException
	{
		Argument<Boolean> logging = optionArgument("-l").build();
		Argument<Boolean> promiscousMode = optionArgument("-p").build();

		ParsedArguments args = CommandLineParser.withArguments(logging, promiscousMode).parse("-pl");

		assertThat(args.get(logging)).isTrue();
		assertThat(args.get(promiscousMode)).isTrue();
	}

	@Test(expected = ArgumentException.class)
	public void testSeveralOptionsInOneArgumentWithOneNonOptionArgument() throws ArgumentException
	{
		Argument<Boolean> logging = optionArgument("-l").build();
		Argument<Integer> number = integerArgument("-n").build();

		// TODO: allow -ln 100 ?

		CommandLineParser.withArguments(logging, number).parse("-ln");
	}

	@Test(expected = ArgumentException.class)
	public void testSeveralOptionsInOneArgumentWithOneDuplicate() throws ArgumentException
	{
		Argument<Boolean> logging = optionArgument("-l").build();
		Argument<Boolean> promiscousMode = optionArgument("-p").build();

		CommandLineParser.withArguments(logging, promiscousMode).parse("-ppl");
	}

	@Test(expected = ArgumentException.class)
	public void testSeveralOptionsInOneArgumentWithOneUnknownCharacter() throws ArgumentException
	{
		optionArgument("-l").parse("-pl");
	}

	@Test(expected = ArgumentException.class)
	public void testThatOnlyAHyphenDoesNotMatchAnyOptions() throws ArgumentException
	{
		optionArgument("-l").parse("-");
	}

	private static Argument<Boolean> subOption = optionArgument("-l").build();

	@Test
	public void testThatNoOptionArgumentIsParsedWhenOneCharacterIsUnhandled() throws ArgumentException
	{
		Argument<Integer> programArgument = integerArgument("-lp").build();
		Argument<String> command = command(new Command(subOption){

			@Override
			protected void execute(ParsedArguments args)
			{
				assertThat(args.get(subOption)).as("Invocation with '-pl' lead to '-l' being parsed").isFalse();
			}

			@Override
			protected String commandName()
			{
				return "test";
			}
		}).build();

		ParsedArguments args = CommandLineParser.withArguments(programArgument, command).parse("test", "-lp", "1");

		// Also verify that the argument is parsed by it's rightful recipient
		assertThat(args.get(programArgument)).isEqualTo(1);
	}
}
