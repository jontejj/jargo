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

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.ParsedArguments;

public class CommandWithOneIndexedArgument extends Command
{
	private static final Argument<Integer> NUMBER = integerArgument().required().build();

	public CommandWithOneIndexedArgument()
	{
		super(NUMBER);
	}

	@Override
	protected String commandName()
	{
		return "one_arg";
	}

	@Override
	protected void execute(ParsedArguments args)
	{
		assertThat(args.get(NUMBER)).isEqualTo(1);
	}
}
