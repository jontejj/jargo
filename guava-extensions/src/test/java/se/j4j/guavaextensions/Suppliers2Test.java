package se.j4j.guavaextensions;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.j4j.testlib.UtilityClassTester;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class Suppliers2Test
{
	@Test
	public void testThatInstanceOfIsSuppliedAlready()
	{
		assertThat(Suppliers2.isSuppliedAlready(Suppliers.ofInstance("foo"))).isTrue();
	}

	@Test
	public void testThatInstanceOfIsSuppliedAlreadyForList()
	{
		Supplier<List<String>> listSupplier = Suppliers2.ofRepeatedElements(Suppliers.ofInstance("foo"), 3);
		assertThat(Suppliers2.isSuppliedAlready(listSupplier)).isTrue();
	}

	@Test
	public void testThatInstanceOfIsNotSuppliedAlreadyForOtherSuppliers()
	{
		assertThat(Suppliers2.isSuppliedAlready(FOO_SUPPLIER)).isFalse();
	}

	private static final Supplier<String> FOO_SUPPLIER = new Supplier<String>(){
		@Override
		public String get()
		{
			return "foo";
		}
	};

	@Test
	public void testThatInstanceOfIsNotSuppliedForArbitrarySupplierInList()
	{
		Supplier<List<String>> listSupplier = Suppliers2.ofRepeatedElements(FOO_SUPPLIER, 3);
		assertThat(Suppliers2.isSuppliedAlready(listSupplier)).isFalse();
	}

	@Test
	public void testThatRepeatedElementsReturnCorrectNumberOfInstances()
	{
		Supplier<List<String>> listSupplier = Suppliers2.ofRepeatedElements(FOO_SUPPLIER, 3);
		assertThat(listSupplier.get()).isEqualTo(Arrays.asList("foo", "foo", "foo"));
	}

	@Test
	public void testUtilityClassDesign()
	{
		UtilityClassTester.testUtilityClassDesign(Suppliers2.class);
	}
}
