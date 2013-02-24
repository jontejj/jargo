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
package se.j4j.testlib;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.ByteStreams.toByteArray;
import static java.lang.reflect.Modifier.isStatic;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

public final class Launcher
{
	public static class LaunchedProgram
	{
		/**
		 * The {@link System#err} in {@link Charsets#UTF_8 UTF-8} from the launched program
		 */
		public String errors;
		/**
		 * The {@link System#out} in {@link Charsets#UTF_8 UTF-8} from the launched program
		 */
		public String output;
		private final String jvm;
		private final String classPath;

		private LaunchedProgram(String errors, String output, String jvm, String classPath)
		{
			this.errors = errors;
			this.output = output;
			this.classPath = classPath;
			this.jvm = jvm;
		}

		public String debugInformation()
		{
			return "\njvm: " + jvm + "\nclasspath: " + classPath;
		}
	}

	private Launcher()
	{
	}

	/**
	 * Runs {@code classWithMainMethod} in a separate process using the system property
	 * {@code java.home} to find java and {@code java.class.path} for the class path. This method
	 * will wait until program execution has finished.
	 * 
	 * @param classWithMainMethod a class with a static "main" method
	 * @param programArguments optional arguments to pass to the program
	 * @return output/errors from the executed program
	 * @throws IOException if an I/O error occurs while starting {@code classWithMainMethod} as a
	 *             process
	 * @throws InterruptedException if the thread starting the program is
	 *             {@link Thread#interrupted()}
	 * @throws IllegalArgumentException if {@code classWithMainMethod} doesn't have a static main
	 *             method
	 * @throws SecurityException if it's not possible to validate the existence of a main method in
	 *             {@code classWithMainMethod}
	 */
	public static LaunchedProgram launch(Class<?> classWithMainMethod, String ... programArguments) throws IOException, InterruptedException
	{
		checkNotNull(classWithMainMethod);
		checkNotNull(programArguments);
		try
		{

			int modifiers = classWithMainMethod.getDeclaredMethod("main", String[].class).getModifiers();
			boolean validModifiers = isStatic(modifiers);// && isPublic(modifiers);
			checkArgument(validModifiers, "%s's main method needs to be static and public for it to be launchable", classWithMainMethod.getName());
		}
		catch(NoSuchMethodException e)
		{
			throw new IllegalArgumentException("No main method found on: " + classWithMainMethod.getName());
		}
		String jvm = new File(new File(System.getProperty("java.home"), "bin"), "java").getAbsolutePath();
		String classPath = System.getProperty("java.class.path");

		List<String> args = Lists.newArrayList(jvm, "-cp", classPath, classWithMainMethod.getName());
		args.addAll(Arrays.asList(programArguments));

		Process program = new ProcessBuilder().command(args).start();
		String output = new String(toByteArray(program.getInputStream()), Charsets.UTF_8);
		String errors = new String(toByteArray(program.getErrorStream()), Charsets.UTF_8);

		program.waitFor();
		return new LaunchedProgram(errors, output, jvm, classPath);
	}
}
