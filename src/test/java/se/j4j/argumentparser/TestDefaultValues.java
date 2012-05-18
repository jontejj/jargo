package se.j4j.argumentparser;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.withParser;
import static se.j4j.argumentparser.Limiters.positiveInteger;
import static se.j4j.argumentparser.StringParsers.integerParser;
import static se.j4j.argumentparser.StringParsers.Radix.DECIMAL;
import static se.j4j.argumentparser.StringSplitters.comma;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentFactory.RadixiableArgumentBuilder;
import se.j4j.argumentparser.providers.ChangingProvider;
import se.j4j.argumentparser.providers.NegativeValueProvider;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class TestDefaultValues
{
	@Test
	public void testThatNonRequiredAndNonDefaultedIntegerArgumentDefaultsToZero() throws ArgumentException
	{
		assertThat(integerArgument("-n").parse()).isZero();
	}

	@Test
	public void testThatNonRequiredAndNonDefaultedRepeatedIntegerArgumentDefaultsToEmptyList() throws ArgumentException
	{
		List<Integer> numbers = integerArgument("-n").repeated().parse();
		assertThat(numbers).isEmpty();
	}

	@Test(expected = RuntimeException.class)
	public void testThatInvalidDefaultValueFromStringParserIsInvalidated() throws ArgumentException
	{
		withParser(new ForwardingStringParser<Integer>(integerParser(DECIMAL)){
			@Override
			public Integer defaultValue()
			{
				return -1;
			}
		}).limitTo(positiveInteger()).parse();
	}

	@Test(expected = RuntimeException.class)
	public void testThatInvalidDefaultValueProviderValuesAreInvalidated() throws ArgumentException
	{
		// Throws because -1 (which is given by NegativeValueProvider) isn't
		// positive
		integerArgument("-n").defaultValueProvider(new NegativeValueProvider()).limitTo(positiveInteger()).parse();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testThatDefaultValuesProvidedToRepeatedArgumentsAreImmutable() throws ArgumentException
	{
		// Should throw because defaultValue makes its argument Immutable
		integerArgument("-n").repeated().defaultValue(asList(1, 2)).parse().add(3);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testThatDefaultValuesProvidedToListArgumentsAreImmutable() throws ArgumentException
	{
		// Should throw because defaultValue makes its argument Immutable
		integerArgument("-n").arity(2).defaultValue(asList(1, 2)).parse().add(3);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testThatDefaultValuesProvidedToSplitArgumentsAreImmutable() throws ArgumentException
	{
		// Should throw because defaultValue makes its argument Immutable
		integerArgument("-n").splitWith(comma()).defaultValue(asList(1, 2)).parse().add(3);
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

		number = builder.splitWith(comma()).build();

		testUnmodifiableDefaultList(number);
	}

	private void testUnmodifiableDefaultList(Argument<List<Integer>> number) throws ArgumentException
	{
		List<Integer> defaultValue = number.parse();
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

	@Test
	public void testThatDefaultValueProviderIsAskedForEachArgumentParsing() throws ArgumentException
	{
		Provider<Integer> provider = new ChangingProvider();

		Argument<Integer> n = integerArgument("-n").defaultValueProvider(provider).build();

		assertThat(n.parse()).isNotEqualTo(n.parse());
	}
}
