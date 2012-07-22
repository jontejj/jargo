package se.j4j.argumentparser.finalizers;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.limiters.FooLimiter.foos;

import java.util.ArrayList;
import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.Finalizer;
import se.j4j.argumentparser.Finalizers;

import com.google.common.collect.ImmutableList;

public class TestFinalizers
{
	@Test
	public void testThatFinalizersAreCalled() throws ArgumentException
	{
		assertThat(stringArgument().finalizeWith(new AddBar()).parse("foo")).isEqualTo("foobar");
	}

	@Test
	public void testThatFinalizersAreCalledBeforeLimiters() throws ArgumentException
	{
		assertThat(stringArgument().limitTo(foos()).finalizeWith(new AddFoo()).parse("")).isEqualTo("foo");
	}

	@Test
	public void testThatFinalizersAreCalledForPropertyValues() throws ArgumentException
	{
		Map<String, String> map = stringArgument("-N").finalizeWith(new AddBar()).asPropertyMap().parse("-Nfoo=bar", "-Nbar=foo");

		assertThat(map.get("foo")).isEqualTo("barbar");
		assertThat(map.get("bar")).isEqualTo("foobar");
	}

	@Test
	public void testThatFinalizersAreCalledBeforeLimitersForPropertyValues() throws ArgumentException
	{
		assertThat(stringArgument("-N").finalizeWith(new AddFoo()).limitTo(foos()).asPropertyMap().parse("-Nbar=").get("bar")).isEqualTo("foo");
	}

	@Test
	public void testThatFinalizersAreCalledForValuesInLists() throws ArgumentException
	{
		assertThat(stringArgument().finalizeWith(new AddBar()).variableArity().parse("foo", "bar")).isEqualTo(asList("foobar", "barbar"));
	}

	@Test
	public void testThatFinalizersAreCalledForRepeatedValues() throws ArgumentException
	{
		assertThat(stringArgument("-n").finalizeWith(new AddBar()).repeated().parse("-n", "foo", "-n", "foo")).isEqualTo(asList("foobar", "foobar"));
	}

	@Test
	public void testThatCallbacksAreCalledForValuesFromSplit() throws ArgumentException
	{
		assertThat(stringArgument().finalizeWith(new AddBar()).splitWith(",").parse("foo,bar")).isEqualTo(asList("foobar", "barbar"));
	}

	@Test
	public void testMultipleFinalizers() throws ArgumentException
	{
		Finalizer<String> compoundFinalizer = Finalizers.compound(ImmutableList.of(new AddFoo(), new AddBar()));

		assertThat(stringArgument().finalizeWith(compoundFinalizer).parse("_")).isEqualTo("_foobar");
	}

	@Test
	public void testThatFinalizersAreClearable() throws ArgumentException
	{
		assertThat(stringArgument("-n").finalizeWith(new AddBar()).clearFinalizers().parse("-n", "foo")).isEqualTo("foo");
	}

	@Test
	public void testThatDefaultValuesAreFinalized() throws ArgumentException
	{
		assertThat(stringArgument("-n").finalizeWith(new AddBar()).defaultValue("foo").parse()).isEqualTo("foobar");
	}

	@Test
	public void testThatDefaultValuesAreFinalizedForUsage()
	{
		assertThat(stringArgument("-n").finalizeWith(new AddFoo()).usage("")).contains("Default: foo");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThatEmptyListIsIllegal()
	{
		Finalizers.compound(new ArrayList<Finalizer<Object>>());
	}

}
