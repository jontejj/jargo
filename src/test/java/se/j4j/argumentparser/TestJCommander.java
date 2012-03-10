package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class TestJCommander
{
	private static class TestRequiredParameter
	{
		@Parameter(names = "-test", description = "Test something", required = true)
		private int test;
	}

	@Test
	public void testJCommanderUsage()
	{
		TestRequiredParameter test = new TestRequiredParameter();
		JCommander commander = new JCommander(test);
		commander.parse("-test", "1");
		commander.usage();
		assertThat(test.test).isEqualTo(1);
	}

	public enum TestEnum
	{
		START,
		STOP
	}

	public class TestClass
	{
		@Parameter(names = "-enum", description = "Some enum")
		private TestEnum enumField;
	}
	@Test
	public void testJCommanderEnum()
	{
		TestClass test = new TestClass();
		JCommander commander = new JCommander(test);
		commander.parse("-enum", "Start");
		commander.usage();
		assertThat(test.enumField).isEqualTo(TestEnum.START);
	}
}
