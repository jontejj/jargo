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

import org.joda.time.DateTime;

import se.j4j.argumentparser.ArgumentBuilder.DefaultArgumentBuilder;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentExceptions;
import se.j4j.argumentparser.StringParser;

public class DateTimeParser implements StringParser<DateTime>
{
	@Override
	public String descriptionOfValidValues(Locale locale)
	{
		return "an ISO8601 date, such as 2011-02-28";
	}

	@Override
	public DateTime parse(final String value, Locale locale) throws ArgumentException
	{
		try
		{
			return DateTime.parse(value);
		}
		catch(IllegalArgumentException wrongDateFormat)
		{
			throw ArgumentExceptions.withMessage(wrongDateFormat.getLocalizedMessage());
		}
	}

	@Override
	public DateTime defaultValue()
	{
		return DateTime.now();
	}

	public static DefaultArgumentBuilder<DateTime> dateArgument(String ... names)
	{
		return withParser(new DateTimeParser()).defaultValueDescription("Current time").names(names);
	}

	@Override
	public String metaDescription()
	{
		return "<date>";
	}
}
