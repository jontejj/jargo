package se.j4j.argumentparser.Validators;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;

import java.util.List;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.interfaces.ValueValidator;
import se.j4j.argumentparser.validators.PositiveInteger;

public class TestValidators
{
	@Test
	public void testPositiveInteger() throws ArgumentException
	{
		Argument<Integer> positiveArgument = integerArgument("-i", "--index").validator(new PositiveInteger()).build();

		ArgumentParser parser = ArgumentParser.forArguments(positiveArgument);
		try
		{
			parser.parse("-i", "-5");
			fail("-5 shouldn't be a valid positive integer");
		}
		catch (ArgumentException expected)
		{
		}
		ParsedArguments parsed =parser.parse("-i", "10");
		assertThat(parsed.get(positiveArgument)).isEqualTo(10);
	}

	@Test(expected = InvalidArgument.class)
	public void testRepeatedPositiveIntegers() throws ArgumentException
	{
		Argument<List<Integer>> positiveArgument = integerArgument("-i", "--index").validator(new PositiveInteger())
				.repeated().build();

		ArgumentParser.forArguments(positiveArgument).parse("-i", "10","-i", "-5");
	}

	@Test(expected = InvalidArgument.class)
	public void testArityOfPositiveIntegers() throws ArgumentException
	{
		Argument<List<Integer>> positiveArgument = integerArgument("-i", "--indices").validator(new PositiveInteger())
				.arity(2).build();

		ArgumentParser.forArguments(positiveArgument).parse("-i", "10", "-5");
	}

	@Test(expected = ClassCastException.class)
	public void testInvalidValidatorType() throws ArgumentException
	{
		Object validator = new ShortString();
		@SuppressWarnings("unchecked") //This is what's tested
		Argument<Integer> number = integerArgument("-n").validator((ValueValidator<Integer>) validator).build();
		ArgumentParser.forArguments(number).parse("-n", "1");

	}
}
