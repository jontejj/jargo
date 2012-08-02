package se.j4j.argumentparser.describers;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.Describers.asFunction;
import static se.j4j.argumentparser.Describers.booleanAsEnabledDisabled;

import java.util.List;

import org.junit.Test;

import se.j4j.argumentparser.Describer;

/**
 * Tests for {@link Describer}
 */
public class DescriberTest
{
	@Test
	public void testDescriberAsAFunction()
	{
		List<Boolean> booleans = asList(true, false);
		List<String> describedBooleans = transform(booleans, asFunction(booleanAsEnabledDisabled()));
		assertThat(describedBooleans).isEqualTo(asList("enabled", "disabled"));
	}
}
