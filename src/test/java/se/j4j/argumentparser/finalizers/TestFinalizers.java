package se.j4j.argumentparser.finalizers;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.Limiters.positiveInteger;
import static se.j4j.argumentparser.StringSplitters.comma;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.Finalizer;
import se.j4j.argumentparser.Finalizers;
import se.j4j.argumentparser.exceptions.ArgumentException;

import com.google.common.collect.ImmutableList;

public class TestFinalizers
{
	@Test
	public void testThatFinalizersAreCalled() throws ArgumentException
	{
		assertThat(integerArgument().finalizeWith(new AddOne()).parse("1")).isEqualTo(2);
	}

	@Test
	public void testThatFinalizersAreCalledBeforeLimiters() throws ArgumentException
	{
		assertThat(integerArgument().limitTo(positiveInteger()).finalizeWith(new AddOne()).parse("-1")).isZero();
	}

	@Test
	public void testThatFinalizersAreCalledForPropertyValues() throws ArgumentException
	{
		Map<String, Integer> map = integerArgument("-N").finalizeWith(new AddOne()).asPropertyMap().parse("-Nfoo=1", "-Nbar=2");

		assertThat(map.get("foo")).isEqualTo(2);
		assertThat(map.get("bar")).isEqualTo(3);
	}

	@Test
	public void testThatFinalizersAreCalledBeforeLimitersForPropertyValues() throws ArgumentException
	{
		assertThat(integerArgument("-N").finalizeWith(new AddOne()).limitTo(positiveInteger()).asPropertyMap().parse("-Nfoo=-1").get("foo")).isZero();
	}

	@Test
	public void testThatFinalizersAreCalledForValuesInLists() throws ArgumentException
	{
		assertThat(integerArgument().finalizeWith(new AddOne()).consumeAll().parse("1", "2")).isEqualTo(Arrays.asList(2, 3));
	}

	@Test
	public void testThatFinalizersAreCalledForRepeatedValues() throws ArgumentException
	{
		assertThat(integerArgument("-n").finalizeWith(new AddOne()).repeated().parse("-n", "1", "-n", "2")).isEqualTo(Arrays.asList(2, 3));
	}

	@Test
	public void testThatCallbacksAreCalledForValuesFromSplit() throws ArgumentException
	{
		assertThat(integerArgument().finalizeWith(new AddOne()).splitWith(comma()).parse("1,2")).isEqualTo(Arrays.asList(2, 3));
	}

	@Test
	public void testMultipleFinalizers() throws ArgumentException
	{
		Finalizer<Integer> compoundFinalizer = Finalizers.compound(ImmutableList.of(new AddOne(), new MultiplyByFive()));

		assertThat(integerArgument().finalizeWith(compoundFinalizer).parse("1")).isEqualTo(10);
		assertThat(integerArgument().finalizeWith(Finalizers.compound(new AddOne(), null)).parse("2")).isEqualTo(3);
	}

	@Test
	public void testThatFinalizersAreClearable() throws ArgumentException
	{
		assertThat(integerArgument("-n").finalizeWith(new AddOne()).clearFinalizers().parse("-n", "0")).isZero();
	}
}
