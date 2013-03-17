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

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.softhouse.common.guavaextensions.Suppliers2;
import se.softhouse.common.testlib.UtilityClassTester;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

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
		new NullPointerTester().testStaticMethods(Suppliers2.class, Visibility.PACKAGE);
		UtilityClassTester.testUtilityClassDesign(Suppliers2.class);
	}
}
