/* Copyright 2018 jonatanjonsson
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
package se.softhouse.jargo;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

import org.junit.Test;

import se.softhouse.jargo.internal.Texts.UserErrors;

/**
 * Tests for {@link ArgumentBuilder#transform(java.util.function.Function)}
 */
public class TransformTest
{
	@Test
	public void testThatStringCanEasilyBeTransformed() throws Exception
	{
		int size = Arguments.stringArgument("--foo").transform(String::length).parse("--foo", "abcd");
		assertThat(size).isEqualTo(4);
	}

	@Test
	public void testThatStringCanBeLimitedAndThenTransformed() throws Exception
	{
		try
		{
			Arguments.stringArgument("--foo").limitTo(str -> str.length() < 10).transform(String::length).parse("--foo", "abcdsdasdasdas");
			fail("abcdsdasdasdas should be rejected as it is longer than 10 chars");
		}
		catch(ArgumentException expected)
		{
			// TODO(joj): "any string", it could be described with a ArgumentBuilder#describeValidValues(...)
			// method
			// as it's now the Predicate's toString needs to be overwritten
			assertThat(expected).hasMessage(String.format(UserErrors.DISALLOWED_VALUE, "abcdsdasdasdas", "any string"));
		}
	}

	@Test
	public void testThatStringCanBeLimitedAndThenTransformedAndThenLimited() throws Exception
	{
		Argument<Integer> tightInteger = Arguments.stringArgument("--foo").limitTo(str -> str.length() < 10) //
				.transform(String::length) //
				.limitTo((i) -> i >= 5).build();
		try
		{
			tightInteger.parse("--foo", "abcdsdasasdad");
			fail("abcdsdasasdad should be rejected as it is longer than 10 chars");
		}
		catch(ArgumentException expected)
		{
		}
		try
		{
			tightInteger.parse("--foo", "asdd");
			fail("asdd should be rejected as it is smaller than 5 chars");
		}
		catch(ArgumentException expectedTwo)
		{
		}
	}

	@Test
	public void testThatStringCanBeDefaultedAndThenTransformed() throws Exception
	{
		int defaultSize = Arguments.stringArgument("--foo").defaultValue("hej").transform(String::length).parse();
		assertThat(defaultSize).isEqualTo(3);
	}

	@Test
	public void testThatDefaultValuesCanBeDescribedAndTransformed() throws Exception
	{
		Usage usage = Arguments.stringArgument("--foo").defaultValue("hej") //
				.defaultValueDescription("hej is a good word") //
				.transform(String::length) //
				.usage();
		assertThat(usage.toString()).contains("hej is a good word");
	}

	@Test
	public void testThatLimitIsCheckedForDefaultValuesBeforeBeingTransformed() throws Exception
	{
		try
		{
			Arguments.stringArgument("--foo").defaultValue("hej").limitTo(s -> s.length() < 3).transform(String::length).parse();
			fail("hej is not less than 3 chars");
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected.getMessage()).startsWith("'hej' is not ");
		}
	}

	@Test
	public void testThatMetaDescriptionCanBeSpecifiedAfterOrBeforeTransformer() throws Exception
	{
		Usage usage = Arguments.stringArgument("--foo").metaDescription("<bar>").transform(String::length).usage();
		assertThat(usage.toString()).contains("<bar>");
	}

	@Test
	public void testThatArityCanThenBeTransformed() throws Exception
	{
		int size = Arguments.stringArgument("--foo").arity(2).transform(list -> list.stream().map(String::length).reduce(0, Integer::sum))
				.parse("--foo", "bar", "zooo");
		assertThat(size).isEqualTo(7);
	}
}
