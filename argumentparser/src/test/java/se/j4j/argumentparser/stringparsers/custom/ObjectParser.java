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
package se.j4j.argumentparser.stringparsers.custom;

import static se.j4j.argumentparser.ArgumentFactory.withParser;

import java.util.Locale;

import se.j4j.argumentparser.ArgumentBuilder.DefaultArgumentBuilder;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.StringParser;

public class ObjectParser implements StringParser<Object>
{
	@Override
	public Object parse(String argument, Locale locale) throws ArgumentException
	{
		return argument;
	}

	@Override
	public String descriptionOfValidValues(Locale locale)
	{
		return "Any string";
	}

	@Override
	public Object defaultValue()
	{
		return 0;
	}

	@Override
	public String metaDescription()
	{
		return "<an-object>";
	}

	public static DefaultArgumentBuilder<Object> objectArgument()
	{
		return withParser(new ObjectParser());
	}

}
