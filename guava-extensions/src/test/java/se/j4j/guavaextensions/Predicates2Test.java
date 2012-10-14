package se.j4j.guavaextensions;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

import java.util.Arrays;

import org.junit.Test;

import se.j4j.testlib.UtilityClassTester;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

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
		assertThat(Predicates2.listPredicate(ABOVE_ZERO).apply(Arrays.asList(1, 2, 3))).isTrue();
	}

	@Test
	public void testThatListPredicateThrowsAndIndicatesTheFirstOffendingValue()
	{
		try
		{
			Predicates2.listPredicate(ABOVE_ZERO).apply(Arrays.asList(1, -1, -2));
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
		assertThat(Predicates2.listPredicate(ABOVE_ZERO).toString()).isEqualTo("above zero");
	}

	@Test
	public void testThatListPredicateReturnsAlwaysTrueWhenGivenAlwaysTrue()
	{
		assertThat(Predicates2.listPredicate(Predicates.alwaysTrue())).isSameAs(Predicates.alwaysTrue());
	}

	@Test
	public void testUtilityClassDesign()
	{
		new NullPointerTester().testStaticMethods(Predicates2.class, Visibility.PACKAGE);
		UtilityClassTester.testUtilityClassDesign(Predicates2.class);
	}
}
