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

import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.ParsedArguments;
import se.j4j.argumentparser.commands.Build.BuildTarget;

public class Clean extends Command
{
	final BuildTarget target;

	public Clean()
	{
		this.target = new BuildTarget();
	}

	public Clean(BuildTarget target)
	{
		this.target = target;
	}

	@Override
	protected String commandName()
	{
		return "clean";
	}

	@Override
	public String description()
	{
		return "Cleans a target";
	}

	@Override
	protected void execute(ParsedArguments parsedArguments)
	{
		target.clean();
	}
}
