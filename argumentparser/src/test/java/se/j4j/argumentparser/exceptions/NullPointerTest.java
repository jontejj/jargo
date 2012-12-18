package se.j4j.argumentparser.exceptions;

import static junit.framework.Assert.fail;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentBuilder.DefaultArgumentBuilder;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentExceptions;
import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.CommandLineParser;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.ProgramInformation;
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
		npeTester.testStaticMethods(CommandLineParser.class, Visibility.PACKAGE);
		npeTester.testStaticMethods(ProgramInformation.class, Visibility.PACKAGE);
		npeTester.testStaticMethods(StringParsers.class, Visibility.PACKAGE);

		DefaultArgumentBuilder<Integer> builder = integerArgument("--name");
		npeTester.testInstanceMethods(builder, Visibility.PROTECTED);

		Argument<Integer> argument = builder.build();

		npeTester.testInstanceMethods(argument, Visibility.PROTECTED);

		CommandLineParser parser = CommandLineParser.withArguments(argument);
		npeTester.testInstanceMethods(parser, Visibility.PROTECTED);

		ProgramInformation info = ProgramInformation.programName("helloworld");
		npeTester.testInstanceMethods(info, Visibility.PROTECTED);

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
