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
package se.softhouse.common.guavaextensions;

import static com.google.common.base.Predicates.alwaysTrue;
import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.softhouse.common.guavaextensions.Predicates2.listPredicate;

import java.util.List;

import org.junit.Test;

import se.softhouse.common.testlib.UtilityClassTester;

import com.google.common.base.Predicate;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

/**
 * Tests for {@link Predicates2}
 */
public class Predicates2Test
{
	static final Predicate<Integer> ABOVE_ZERO = new Predicate<Integer>(){
		@Override
		public boolean apply(Integer input)
		{
			return input > 0;
		}

		@Override
		public String toString()
		{
			return "above zero";
		}
	};

	@Test
	public void testListPredicate()
	{
		assertThat(listPredicate(ABOVE_ZERO).apply(asList(1, 2, 3))).isTrue();
	}

	@Test
	public void testThatListPredicateThrowsAndIndicatesTheFirstOffendingValue()
	{
		List<Integer> numbers = asList(1, -1, -2);

		try
		{
			listPredicate(ABOVE_ZERO).apply(numbers);
			fail("-1 should not be above zero");
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected).hasMessage("'-1' is not above zero");
		}
	}

	@Test
	public void testThatListPredicateToStringDelegatesToElementPredicateToString()
	{
		assertThat(listPredicate(ABOVE_ZERO).toString()).isEqualTo("above zero");
	}

	@Test
	public void testThatListPredicateReturnsAlwaysTrueWhenGivenAlwaysTrue()
	{
		assertThat(listPredicate(alwaysTrue())).isSameAs(alwaysTrue());
	}

	@Test
	public void testThatNullContractsAreFollowed() throws Exception
	{
		new NullPointerTester().testStaticMethods(Predicates2.class, Visibility.PACKAGE);
	}

	@Test
	public void testThatUtilityClassDesignIsCorrect()
	{
		UtilityClassTester.testUtilityClassDesign(Predicates2.class);
	}
}
