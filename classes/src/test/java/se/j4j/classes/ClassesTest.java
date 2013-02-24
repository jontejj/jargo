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
package se.j4j.classes;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import se.j4j.testlib.Launcher;
import se.j4j.testlib.Launcher.LaunchedProgram;
import se.j4j.testlib.UtilityClassTester;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

public class ClassesTest
{
	@Test
	public void testThatMainClassNameIsExampleProgram() throws IOException, InterruptedException
	{
		LaunchedProgram threadedProgram = Launcher.launch(ExampleProgram.class);

		assertThat(threadedProgram.errors).as(	"Errors detected in subprogram: " + threadedProgram.errors + ". Debuginfo:"
														+ threadedProgram.debugInformation()).isEmpty();
		assertThat(threadedProgram.output).isEqualTo("ExampleProgram");
	}

	@Test
	public void testThatFetchingMainClassNameWorksFromANewThread() throws IOException, InterruptedException
	{
		LaunchedProgram threadedProgram = Launcher.launch(ThreadedProgram.class);

		assertThat(threadedProgram.errors).as(	"Errors detected in subprogram: " + threadedProgram.errors + ". Debuginfo:"
														+ threadedProgram.debugInformation()).isEmpty();
		assertThat(threadedProgram.output).isEqualTo("ThreadedProgram");
	}

	@Test
	public void testThatProgramWithDeadMainThreadCausesException() throws IOException, InterruptedException
	{
		LaunchedProgram noMain = Launcher.launch(NoMainAvailable.class);
		assertThat(noMain.errors).isEqualTo("No main method found in the stack traces, could it be that the main thread has been terminated?");
	}

	@Test
	public void testUtilityClassDesign()
	{
		new NullPointerTester().testStaticMethods(Classes.class, Visibility.PACKAGE);
		UtilityClassTester.testUtilityClassDesign(Classes.class);
	}
}
