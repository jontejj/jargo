package se.j4j.argumentparser.Validators;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.fileArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;

import java.io.File;

import org.fest.assertions.Fail;
import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.interfaces.ValueValidator;
import se.j4j.argumentparser.stringsplitters.Comma;
import se.j4j.argumentparser.validators.ExistingFile;
import se.j4j.argumentparser.validators.PositiveInteger;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
			Fail.fail("-5 shouldn't be a valid positive integer");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected.getMessage()).isEqualTo("'-5' is not a positive integer");
		}
		ParsedArguments parsed = parser.parse("-i", "10");
		assertThat(parsed.get(positiveArgument)).isEqualTo(10);
	}

	@Test
	public void testExistingFile()
	{
		Argument<File> file = fileArgument("--file").validator(new ExistingFile()).build();

		ArgumentParser parser = ArgumentParser.forArguments(file);
		try
		{
			parser.parse("--file", ".");
		}
		catch(ArgumentException e)
		{
			fail(". should be an existing file", e);
		}
		try
		{
			parser.parse("--file", "non_existing.file");
			Fail.fail("non_existing.file should not exist");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected.getMessage()).isEqualTo("'non_existing.file' doesn't exist");
		}
	}

	@Test(expected = InvalidArgument.class)
	public void testRepeatedPositiveIntegers() throws ArgumentException
	{
		integerArgument("-i", "--index").validator(new PositiveInteger()).repeated().parse("-i", "10", "-i", "-5");
	}

	@Test(expected = InvalidArgument.class)
	public void testArityOfPositiveIntegers() throws ArgumentException
	{
		integerArgument("-i", "--indices").validator(new PositiveInteger()).arity(2).parse("-i", "10", "-5");
	}

	@Test(expected = InvalidArgument.class)
	public void testSplittingAndValidating() throws ArgumentException
	{
		integerArgument("-n").separator("=").validator(new PositiveInteger()).splitWith(new Comma()).parse("-n=1,-2");
	}

	// This is what's tested
	@SuppressWarnings("unchecked")
	@Test(expected = ClassCastException.class)
	public void testInvalidValidatorType() throws ArgumentException
	{
		Object validator = new ShortString();
		integerArgument("-n").validator((ValueValidator<Integer>) validator).parse("-n", "1");
	}

	@Test(expected = RuntimeException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "fail-fast during build phase")
	public void testThatDefaultValuesAreValidatedDuringBuild()
	{
		integerArgument("-n").defaultValue(-1).validator(new PositiveInteger()).build();
	}

	@Test(expected = RuntimeException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "fail-fast during build phase")
	public void testThatDefaultValuesAreValidated()
	{
		integerArgument("-n").validator(new PositiveInteger()).defaultValue(-1).build();
	}

	@Test
	public void testThatValidatorIsntCalledTooOften() throws ArgumentException
	{
		ProfilingValidator<Integer> profiler = new ProfilingValidator<Integer>();

		integerArgument("-n").validator(profiler).repeated().parse("-n", "1", "-n", "-2");

		assertThat(profiler.validationsMade).isEqualTo(2);
	}

	private static final class ProfilingValidator<T> implements ValueValidator<T>
	{
		int validationsMade;

		@Override
		public void validate(T value) throws InvalidArgument
		{
			validationsMade++;
		}
	}
}
