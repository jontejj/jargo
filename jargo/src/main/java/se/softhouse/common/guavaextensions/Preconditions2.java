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
package se.softhouse.common.guavaextensions;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Additional {@link Preconditions pre-condition} checks
 */
public final class Preconditions2
{
	private Preconditions2()
	{
	}

	/**
	 * <pre>
	 * Returns {@code items} as a <i>modifiable</i> {@link List} that's guaranteed not to contain
	 * any nulls.
	 * Use {@link ImmutableList#copyOf(Iterable)} if the returned list doesn't have to be
	 * <i>modifiable</i> and a {@code message} isn't needed.
	 * 
	 * @throws NullPointerException with {@code message} (plus the index of the first
	 *             <code>null</code> element) if any element in {@code items} is <code>null</code>
	 * </pre>
	 */
	@Nonnull
	public static <T> List<T> checkNulls(Iterable<T> items, String message)
	{
		checkNotNull(message, "a message describing what it is that is containing the elements must be given");
		List<T> nullCheckedList = Lists.newArrayList(items);
		int index = 0;
		for(T element : nullCheckedList)
		{
			if(element == null)
				throw new NullPointerException(message + " (discovered one at index " + index + ")");
			index++;
		}
		return nullCheckedList;
	}
}
