package se.j4j.strings;

import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.strings.Describers.asFunction;
import static se.j4j.strings.Describers.booleanAsEnabledDisabled;
import static se.j4j.strings.Describers.booleanAsOnOff;
import static se.j4j.strings.Describers.characterDescriber;
import static se.j4j.strings.Describers.fileDescriber;
import static se.j4j.strings.Describers.mapDescriber;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.j4j.strings.Describers.BooleanDescribers;
import se.j4j.testlib.ResourceLoader;
import se.j4j.testlib.UtilityClassTester;

/**
 * Tests for {@link Describers}
 */
public class DescribersTest
{
	@Test
	public void testDescriberWithConstant()
	{
		Describer<Integer> constant = Describers.withConstantString("42");
		assertThat(constant.describe(1)).isEqualTo("42");
		assertThat(constant.describe(2)).isEqualTo("42");
		assertThat(constant.describe(42)).isEqualTo("42");
	}

	@Test
	public void testThatToStringDescriberWorksForNulls()
	{
		assertThat(Describers.toStringDescriber().describe(null)).isEqualTo("null");
	}

	@Test
	public void testThatToStringDescriberCallsToString()
	{
		assertThat(Describers.toStringDescriber().describe(42)).isEqualTo("42");
	}

	@Test
	public void testCharacterDescriber()
	{
		assertThat(characterDescriber().describe(null)).isEqualTo("null");
		assertThat(characterDescriber().describe((char) 0)).isEqualTo("the Null character");
		assertThat(characterDescriber().describe('a')).isEqualTo("a");
	}

	@Test
	public void testFileDescriber()
	{
		File file = new File("");
		assertThat(fileDescriber().describe(file)).isEqualTo(file.getAbsolutePath());
	}

	@Test
	public void testThatListDescriberDescribesEachValueWithTheValueDescriber()
	{
		Describer<Integer> valueDescriber = Describers.withConstantString("42");
		Describer<List<Integer>> listDescriber = Describers.listDescriber(valueDescriber);

		assertThat(listDescriber.describe(Arrays.asList(1, 2, 3))).isEqualTo("[42, 42, 42]");
		assertThat(listDescriber.describe(Collections.<Integer>emptyList())).isEqualTo("Empty list");
	}

	@Test
	public void testDescriberAsAFunction()
	{
		List<Boolean> booleans = asList(true, false);
		List<String> describedBooleans = transform(booleans, asFunction(booleanAsEnabledDisabled()));
		assertThat(describedBooleans).isEqualTo(asList("enabled", "disabled"));
	}

	@Test
	public void testFunctionAsADescriber()
	{
		String describedBoolean = Describers.usingFunction(asFunction(booleanAsEnabledDisabled())).describe(false);
		assertThat(describedBoolean).isEqualTo("disabled");
	}

	@Test
	public void testBooleanAsOnOff()
	{
		List<Boolean> booleans = asList(true, false);
		List<String> describedBooleans = transform(booleans, asFunction(booleanAsOnOff()));
		assertThat(describedBooleans).isEqualTo(asList("on", "off"));
	}

	@Test
	public void testMapDescriber()
	{
		Map<String, Integer> defaults = newLinkedHashMap();
		defaults.put("population", 42);
		defaults.put("hello", 1);

		Map<String, String> descriptions = newLinkedHashMap();
		descriptions.put("population", "The number of citizens in the world");
		descriptions.put("hello", "The number of times to say hello");
		Describer<Map<String, Integer>> d = mapDescriber(descriptions);

		String describedMap = d.describe(defaults);

		assertThat(describedMap).isEqualTo(ResourceLoader.get("/described-map.txt"));
	}

	@Test
	public void testThatMissingKeyThrows()
	{
		Map<String, Integer> map = newLinkedHashMap();
		map.put("population", 42);

		Map<String, String> noDescriptions = newLinkedHashMap(); // No description of population
		Describer<Map<String, Integer>> describer = mapDescriber(noDescriptions);
		try
		{
			describer.describe(map);
			fail("population should have to be described");
		}
		catch(NullPointerException expected)
		{
			assertThat(expected).hasMessage("Undescribed key: population");
		}
	}

	@Test
	public void testValueOfAndToStringForEnums()
	{
		assertThat(BooleanDescribers.valueOf("ON_OFF").toString()).isEqualTo("ON_OFF");
	}

	@Test
	public void testUtilityClassDesign()
	{
		UtilityClassTester.testUtilityClassDesign(Describers.class);
	}
}
