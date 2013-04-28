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
package se.softhouse.common.testlib;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

/**
 * Tests for {@link Constants}
 */
public class ConstantsTest
{
	@Test
	public void testThatOneSecondIsExpressedInMillis() throws Exception
	{
		assertThat(Constants.ONE_SECOND).isEqualTo(1000);
	}

	@Test
	public void testThatOneMinuteIsExpressedInMillis() throws Exception
	{
		assertThat(Constants.ONE_MINUTE).isEqualTo(60000);
	}
}
