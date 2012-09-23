package se.j4j.argumentparser.argumentbuilder;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentBuilder;
import se.j4j.argumentparser.ArgumentException;

/**
 * Tests for subclassing {@link ArgumentBuilder}
 */
public class CustomArgumentBuilderTest
{
	/**
	 * The idea here is that all foos created from parsed arguments should use bar=5
	 */
	@Test
	public void testThatBarIsSettableOnFooBuilder() throws ArgumentException
	{
		// Note that .bar(5) can be called after description which is a method in ArgumentBuilder
		Foo foo = new FooBuilder().description("bar should even be callable after calls to ArgumentBuilder defined methods").bar(5).parse("foo");

		assertThat(foo.bar).isEqualTo(5);
	}
}
