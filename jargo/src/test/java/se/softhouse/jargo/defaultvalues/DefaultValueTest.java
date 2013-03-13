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
package se.softhouse.jargo.defaultvalues;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.softhouse.jargo.Arguments.booleanArgument;
import static se.softhouse.jargo.Arguments.fileArgument;
import static se.softhouse.jargo.Arguments.integerArgument;
import static se.softhouse.jargo.Arguments.stringArgument;
import static se.softhouse.jargo.Arguments.withParser;
import static se.softhouse.jargo.StringParsers.integerParser;
import static se.softhouse.jargo.StringParsers.stringParser;
import static se.softhouse.jargo.limiters.FooLimiter.foos;
import static se.softhouse.jargo.stringparsers.custom.ObjectParser.objectArgument;
import static se.softhouse.jargo.utils.ExpectedTexts.expected;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import se.softhouse.comeon.strings.Describer;
import se.softhouse.comeon.testlib.Explanation;
import se.softhouse.jargo.Argument;
import se.softhouse.jargo.ArgumentBuilder;
import se.softhouse.jargo.ArgumentBuilder.DefaultArgumentBuilder;
import se.softhouse.jargo.ArgumentException;
import se.softhouse.jargo.ForwardingStringParser;
import se.softhouse.jargo.StringParser;
import se.softhouse.jargo.internal.Texts.ProgrammaticErrors;
import se.softhouse.jargo.internal.Texts.UserErrors;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
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
			assertThat(e).hasMessage(format(ProgrammaticErrors.INVALID_DEFAULT_VALUE, format(UserErrors.DISALLOWED_VALUE, "bar", "foo")));
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
			assertThat(e).hasMessage(format(ProgrammaticErrors.INVALID_DEFAULT_VALUE, format(UserErrors.DISALLOWED_VALUE, "bar", "foo")));
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
	public void testThatDefaultValueIsNotUsedWhenParserReturnsNull() throws ArgumentException
	{
		// TODO: fix so that null can't be passed as an argument?
		Object actualValue = objectArgument().names("-n").defaultValue("defaultValue").parse("-n", null);
		assertThat(actualValue).isNull();
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
	public void testThatNullDefaultValueIsDescribedAsNull()
	{
		assertThat(stringArgument("-n").arity(2).defaultValue(null).usage()).contains("null");
		assertThat(integerArgument("-n").arity(2).defaultValue(null).usage()).contains("null");
		assertThat(integerArgument("-n").asPropertyMap().defaultValue(null).usage()).contains("null");
		assertThat(integerArgument("-n").defaultValue(null).usage()).contains("null");
		assertThat(fileArgument("-n").defaultValue(null).usage()).contains("null");
	}

	@Test
	public void testDefaultValuesForMultipleParametersForNamedArgument() throws ArgumentException
	{
		List<Integer> defaults = asList(5, 6);
		List<Integer> numbers = integerArgument("--numbers").variableArity().defaultValue(defaults).parse();

		assertThat(numbers).isEqualTo(defaults);
	}

	@Test
	public void testVariableArityDefaultsToOneElementListIfDefaultValueIsPreviouslySet() throws ArgumentException
	{
		List<Integer> numbers = integerArgument("--numbers").defaultValue(42).variableArity().parse();
		assertThat(numbers).isEqualTo(asList(42));
	}

	@Test
	public void testVariableArityDefaultsToZeroElementsIfDefaultValueIsNotPreviouslySet() throws ArgumentException
	{
		List<Integer> numbers = integerArgument("--numbers").variableArity().parse();
		assertThat(numbers).isEqualTo(emptyList());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testThatDefaultValueIsImmutableWhenUsedForEachValueInArityArgument() throws ArgumentException
	{
		integerArgument("-n").defaultValue(1).arity(2).parse().add(3);
	}

	@Test
	public void testThatEachDefaultValueIsDescribedInArityArgument()
	{
		String usage = integerArgument("-n").defaultValue(1).defaultValueDescription("One").arity(2).usage();
		assertThat(usage).isEqualTo(expected("arityOfDefaultValuesDescribed"));
	}

	@Test
	public void testThatDefaultValueIsDescribedWithEmptyListForARepeatableListEvenThoughADefaultValueDescriberHasBeenSet()
	{
		// When a defaultValueDescription has been set before repeated/arity/variableArity that
		// description is used for each value but if there is no value there is nothing to describe
		String usage = integerArgument("-n").defaultValueDescription("SomethingThatWillBeReplacedWithEmptyList").repeated().usage();
		assertThat(usage).contains("Default: Empty list [Supports Multiple occurrences]");
	}

	@Test
	public void testThatDefaultValuesCanBeDescribedWithObjectDescriber()
	{
		// If a describer can describe an Object, it can also describe an Integer
		Describer<Object> hashCodeDescriber = new Describer<Object>(){
			@Override
			public String describe(Object value, Locale inLocale)
			{
				return "(Hashcode) " + value.hashCode();
			}
		};
		String usage = integerArgument("-n").defaultValueDescriber(hashCodeDescriber).usage();
		assertThat(usage).contains("Default: (Hashcode) 0");
	}

	@Test
	public void testThatDefaultValuesCanBeSuppliedAsIntegersForObjectArgument()
	{
		Supplier<Integer> integerSupply = new Supplier<Integer>(){
			@Override
			public Integer get()
			{
				return 2;
			}
		};
		String usage = objectArgument().defaultValueSupplier(integerSupply).usage();
		assertThat(usage).contains("Default: 2");
	}

	@Test(expected = IllegalStateException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testThatSettingDefaultValueForValuesInAPropertyArgumentIsNotAllowed()
	{
		integerArgument("-n").defaultValue(1).asPropertyMap();
	}

	@Test(expected = IllegalStateException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testThatSettingDefaultValueForValuesInAKeyValueArgumentIsNotAllowed()
	{
		booleanArgument("-n").defaultValue(true).asKeyValuesWithKeyParser(integerParser());
	}

	@Test
	public void testThatSettingDefaultValueDescriberForValuesInAKeyValueArgumentIsUsedForValues()
	{
		Map<String, Integer> defaults = ImmutableMap.<String, Integer>builder().put("foo", 4000).build();
		String usage = integerArgument("-n").asPropertyMap().defaultValue(defaults).usage();
		assertThat(usage).isEqualTo(expected("defaultValuesInPropertyMapDescribedByDescriber"));
	}

	@Test
	public void testThatMapDescriberIsUsedAsDefaultValueDescriberForValuesInAKeyValueArgument()
	{
		String usage = stringArgument("-n").asPropertyMap().usage();
		assertThat(usage).contains("Empty map");
	}
}