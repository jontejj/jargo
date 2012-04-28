package se.j4j.argumentparser;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.customArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.builders.RadixiableArgumentBuilder;
import se.j4j.argumentparser.defaultproviders.NegativeValueProvider;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.interfaces.StringConverter;
import se.j4j.argumentparser.internal.Comma;
import se.j4j.argumentparser.validators.PositiveInteger;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class TestDefaultValues
{
	@Test
	public void testThatNonRequiredAndNonDefaultedIntegerArgumentDefaultsToZero() throws ArgumentException
	{
		Argument<Integer> number = integerArgument("-n").build();

		ParsedArguments parsed = ArgumentParser.forArguments(number).parse();
		assertThat(parsed.get(number)).isZero();
	}

	@Test
	public void testThatNonRequiredAndNonDefaultedRepeatedIntegerArgumentDefaultsToEmptyList() throws ArgumentException
	{
		Argument<List<Integer>> numbers = integerArgument("-n").repeated().build();

		ParsedArguments parsed = ArgumentParser.forArguments(numbers).parse();
		assertThat(parsed.get(numbers)).isEmpty();
	}

	@Test(expected = RuntimeException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED",
			justification = "As -1 isn't lazily constructed it can be verified already in the build phase")
	public void testThatInvalidDefaultValuesAreInvalidated()
	{
		integerArgument("-n").defaultValue(-1).validator(new PositiveInteger()).build();
	}

	@Test(expected = RuntimeException.class)
	public void testThatInvalidDefaultValuesFromHandlersAreInvalidated() throws ArgumentException
	{
		Argument<Integer> number = customArgument(new StringConverter<Integer>(){

			@Override
			public Integer convert(String argument) throws ArgumentException
			{
				return null;
			}

			@Override
			public String descriptionOfValidValues()
			{
				return "";
			}

			@Override
			public Integer defaultValue()
			{
				return -1;
			}
		}).validator(new PositiveInteger()).build();

		ArgumentParser.forArguments(number).parse().get(number);
	}

	@Test(expected = RuntimeException.class)
	public void testThatInvalidDefaultValueProviderValuesAreInvalidated() throws ArgumentException
	{
		Argument<Integer> number = integerArgument("-n").defaultValueProvider(new NegativeValueProvider()).validator(new PositiveInteger()).build();

		ParsedArguments parsed = ArgumentParser.forArguments(number).parse();
		// Throws because -1 (which is given by NegativeValueProvider) isn't
		// positive
		assertThat(parsed.get(number)).isZero();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testThatDefaultValuesProvidedToRepeatedArgumentsAreImmutable() throws ArgumentException
	{
		Argument<List<Integer>> number = integerArgument("-n").repeated().defaultValue(asList(1, 2)).build();

		ParsedArguments parsed = ArgumentParser.forArguments(number).parse();
		// Should throw because defaultValue makes its argument Immutable
		parsed.get(number).add(3);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testThatDefaultValuesProvidedToListArgumentsAreImmutable() throws ArgumentException
	{
		Argument<List<Integer>> number = integerArgument("-n").arity(2).defaultValue(asList(1, 2)).build();

		ParsedArguments parsed = ArgumentParser.forArguments(number).parse();
		// Should throw because defaultValue makes its argument Immutable
		parsed.get(number).add(3);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testThatDefaultValuesProvidedToSplitArgumentsAreImmutable() throws ArgumentException
	{
		Argument<List<Integer>> number = integerArgument("-n").splitWith(new Comma()).defaultValue(asList(1, 2)).build();

		ParsedArguments parsed = ArgumentParser.forArguments(number).parse();
		// Should throw because defaultValue makes its argument Immutable
		parsed.get(number).add(3);
	}

	@Test(expected = RuntimeException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "fail-fast during configuration phase")
	public void testThatRequiredArgumentsCantHaveADefaultValueProvider()
	{
		integerArgument("-n").required().defaultValueProvider(new NegativeValueProvider());
	}

	@Test
	public void testThatDefaultValueProviderAreMovedBetweenBuilders() throws ArgumentException
	{
		ArgumentBuilder<RadixiableArgumentBuilder<Integer>, Integer> builder = integerArgument("-n")
				.defaultValueProvider(new NegativeValueProvider());

		Argument<List<Integer>> number = builder.repeated().build();

		testUnmodifiableDefaultList(number);

		number = builder.splitWith(new Comma()).build();

		testUnmodifiableDefaultList(number);
	}

	private void testUnmodifiableDefaultList(Argument<List<Integer>> number) throws ArgumentException
	{
		ParsedArguments parsed = ArgumentParser.forArguments(number).parse();

		List<Integer> defaultValue = parsed.get(number);
		assertThat(defaultValue).isEqualTo(Arrays.asList(-1));
		try
		{
			defaultValue.add(-2);
			fail("Lists with default values in them should be unmodifiable");
		}
		catch(UnsupportedOperationException expected)
		{

		}
	}
}
