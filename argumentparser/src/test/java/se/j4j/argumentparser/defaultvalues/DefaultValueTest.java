package se.j4j.argumentparser.defaultvalues;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.booleanArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.ArgumentFactory.withParser;
import static se.j4j.argumentparser.StringParsers.integerParser;
import static se.j4j.argumentparser.StringParsers.stringParser;
import static se.j4j.argumentparser.limiters.FooLimiter.foos;
import static se.j4j.argumentparser.utils.ExpectedTexts.expected;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentBuilder;
import se.j4j.argumentparser.ArgumentBuilder.DefaultArgumentBuilder;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ForwardingStringParser;
import se.j4j.argumentparser.StringParser;
import se.j4j.argumentparser.internal.Texts.ProgrammaticErrors;
import se.j4j.argumentparser.internal.Texts.UserErrors;
import se.j4j.testlib.Explanation;

import com.google.common.base.Supplier;
import com.google.common.collect.Ranges;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Tests for {@link StringParser#defaultValue()}, {@link ArgumentBuilder#defaultValue(Object)} and
 * {@link ArgumentBuilder#defaultValueSupplier(Supplier)}
 */
public class DefaultValueTest
{
	@Test
	public void testThatNonRequiredAndNonDefaultedArgumentDefaultsToZero() throws ArgumentException
	{
		assertThat(integerArgument("-n").parse()).isZero();
	}

	@Test
	public void testThatNonRequiredAndNonDefaultedRepeatedArgumentDefaultsToEmptyList() throws ArgumentException
	{
		List<Integer> numbers = integerArgument("-n").repeated().parse();
		assertThat(numbers).isEmpty();
	}

	@Test(expected = IllegalStateException.class)
	public void testThatInvalidDefaultValueFromStringParserIsInvalidated() throws ArgumentException
	{
		withParser(new ForwardingStringParser.SimpleForwardingStringParser<String>(stringParser()){
			@Override
			public String defaultValue()
			{
				return "bar";
			}
		}).limitTo(foos()).parse();
	}

	@Test
	public void testThatInvalidDefaultValueSupplierValuesAreInvalidated() throws ArgumentException
	{
		try
		{
			// Throws because bar (which is given by BarSupplier) isn't foo
			stringArgument("-n").defaultValueSupplier(new BarSupplier()).limitTo(foos()).parse();
			fail("only foo should be allowed, not bar");
		}
		catch(IllegalStateException e)
		{
			assertThat(e).hasMessage(format(ProgrammaticErrors.INVALID_DEFAULT_VALUE, format(UserErrors.UNALLOWED_VALUE, "bar", "foo")));
		}
	}

	@Test
	public void testThatDefaultValueSupplierIsNotUsedWhenArgumentIsGiven() throws ArgumentException
	{
		ProfilingSupplier profiler = new ProfilingSupplier();
		int one = integerArgument().defaultValueSupplier(profiler).limitTo(Ranges.closed(0, 10)).parse("1");
		assertThat(one).isEqualTo(1);
		assertThat(profiler.callsToGet).isZero();
	}

	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testThatInvalidDefaultValueInRepeatedArgumentIsInvalidatedDuringBuild()
	{
		try
		{
			// Throws because bar (which is given by BarSupplier) isn't foo
			stringArgument("-n").defaultValue("bar").limitTo(foos()).repeated().build();
			fail("only foo should be allowed, not bar");
		}
		catch(IllegalStateException e)
		{
			assertThat(e).hasMessage(format(ProgrammaticErrors.INVALID_DEFAULT_VALUE, format(UserErrors.UNALLOWED_VALUE, "bar", "foo")));
			assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
		}
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
		integerArgument("-n").splitWith(",").defaultValue(asList(1, 2)).parse().add(3);
	}

	@Test(expected = RuntimeException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testThatRequiredArgumentsCantHaveADefaultValueSupplier()
	{
		stringArgument("-n").required().defaultValueSupplier(new BarSupplier());
	}

	@Test
	public void testThatADefaultValueSupplierIsMovedBetweenBuilders() throws ArgumentException
	{
		DefaultArgumentBuilder<String> builder = stringArgument("-n").defaultValueSupplier(new BarSupplier());

		Argument<List<String>> argument = builder.repeated().build();

		testUnmodifiableDefaultList(argument);

		argument = builder.splitWith(",").build();

		testUnmodifiableDefaultList(argument);
	}

	private void testUnmodifiableDefaultList(Argument<List<String>> argument) throws ArgumentException
	{
		List<String> defaultValue = argument.parse();
		assertThat(defaultValue).isEqualTo(Arrays.asList("bar"));
		try
		{
			defaultValue.add("foo");
			fail("Lists with default values in them should be unmodifiable");
		}
		catch(UnsupportedOperationException expected)
		{

		}
	}

	@Test
	public void testNullAsDefaultValue() throws ArgumentException
	{
		assertThat(integerArgument("-n").defaultValue(null).parse()).isNull();
	}

	@Test
	public void testThatDefaultValueProviderIsAskedForEachArgumentParsing() throws ArgumentException
	{
		Supplier<Integer> supplier = new ChangingSupplier();

		Argument<Integer> n = integerArgument("-n").defaultValueSupplier(supplier).build();

		assertThat(n.parse()).isNotEqualTo(n.parse());
	}

	@Test
	public void testThatDefaultValueIsUsedForEachValueInArityArgument() throws ArgumentException
	{
		assertThat(integerArgument("-n").defaultValue(1).arity(2).parse()).isEqualTo(asList(1, 1));
	}

	@Test
	public void testDefaultValuesForMultipleParametersForNamedArgument() throws ArgumentException
	{
		List<Integer> defaults = asList(5, 6);
		List<Integer> numbers = integerArgument("--numbers").variableArity().defaultValue(defaults).parse();

		assertThat(numbers).isEqualTo(defaults);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testThatDefaultValueIsImmutableWhenUsedForEachValueInArityArgument() throws ArgumentException
	{
		integerArgument("-n").defaultValue(1).arity(2).parse().add(3);
	}

	@Test
	public void testThatEachDefaultValueIsDescribedInArityArgument()
	{
		String usage = integerArgument("-n").defaultValue(1).defaultValueDescription("One").arity(2).usage("OnePrintedTwoTimes");
		assertThat(usage).isEqualTo(expected("arityOfDefaultValuesDescribed"));
	}

	@Test
	public void testThatDefaultValueIsDescribedWithEmptyListForARepeatableListEvenThoughADefaultValueDescriberHasBeenSet()
	{
		// When a defaultValueDescription has been set before repeated/arity/variableArity that
		// description is used for each value but if there is no value there is nothing to describe
		String usage = integerArgument("-n").defaultValueDescription("SomethingThatWillBeReplacedWithEmptyList").repeated().usage("DefaultEmptyList");
		assertThat(usage).contains("Default: Empty list [Supports Multiple occurrences]");
	}

	@Test(expected = IllegalStateException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testThatSettingDefaultValueForValuesInAPropertyArgumentIsNotAllowed()
	{
		integerArgument("-n").defaultValue(1).asPropertyMap();
	}

	@Test(expected = IllegalStateException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testThatSettingDefaultValueDescriptionForValuesInAPropertyArgumentIsNotAllowed()
	{
		integerArgument("-n").defaultValueDescription("InvalidInvocation").asPropertyMap();
	}

	@Test(expected = IllegalStateException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testThatSettingDefaultValueForValuesInAKeyValueArgumentIsNotAllowed()
	{
		booleanArgument("-n").defaultValue(true).asKeyValuesWithKeyParser(integerParser());
	}

	@Test(expected = IllegalStateException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testThatSettingDefaultValueDescriptionForValuesInAKeyValueArgumentIsNotAllowed()
	{
		booleanArgument("-n").defaultValueDescription("InvalidInvocation").asKeyValuesWithKeyParser(integerParser());
	}
}
