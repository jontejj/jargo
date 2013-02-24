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
package se.j4j.argumentparser.commands;

import static se.j4j.argumentparser.ArgumentFactory.fileArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;

import java.io.File;
import java.util.List;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.ParsedArguments;

import com.google.common.collect.Lists;

public class CommitCommand extends Command
{
	public final Repository repository;

	private static final Argument<Boolean> AMEND = optionArgument("--amend").build();
	private static final Argument<String> AUTHOR = stringArgument("--author").required().separator("=").build();
	private static final Argument<List<File>> FILES = fileArgument().variableArity().build();

	public CommitCommand(final Repository repo)
	{
		super(AMEND, AUTHOR, FILES);
		repository = repo;
	}

	@Override
	protected void execute(final ParsedArguments parsedArguments)
	{
		repository.commits.add(new Commit(parsedArguments));
	}

	@Override
	protected String commandName()
	{
		return "commit";
	}

	@Override
	public String description()
	{
		return "Commits to a repository";
	}

	public static class Repository
	{
		List<Commit> commits = Lists.newArrayList();

		int logLimit = 10;
	}

	public static class Commit
	{
		final List<File> files;
		final boolean amend;
		final String author;

		public Commit(final ParsedArguments arguments)
		{
			amend = arguments.get(AMEND);
			files = arguments.get(FILES);
			author = arguments.get(AUTHOR);
		}
	}
}
