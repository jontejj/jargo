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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * Gives you static access to additional implementations of the {@link Function} interface, as a
 * complement to {@link Functions}
 */
public final class Functions2
{
	private Functions2()
	{
	}

	/**
	 * Runs several {@link Function}s in the same order as they are
	 * given as arguments here.<br>
	 * If one of the {@link Function}s makes {@code T} {@link Immutable},
	 * make sure to pass it in last as it's hard to modify
	 * an {@link Immutable} value. This works exactly like
	 * {@link Functions#compose(Function, Function)} except that if {@code first} doesn't do
	 * anything, {@code second} is returned directly instead.
	 * 
	 * @param first a {@link Function}
	 * @param second another {@link Function}
	 * @return a merged {@link Function}
	 */
	@Nonnull
	public static <T> Function<T, T> compound(Function<T, T> first, Function<T, T> second)
	{
		checkNotNull(first);
		checkNotNull(second);
		if(first == Functions.identity())
			return second;

		return new CompoundFunction<T>(first, second);
	}

	private static final class CompoundFunction<T> implements Function<T, T>
	{
		@Nonnull private final Function<T, T> first;
		@Nonnull private final Function<T, T> second;

		private CompoundFunction(Function<T, T> first, Function<T, T> second)
		{
			this.first = first;
			this.second = second;
		}

		@Nullable
		@Override
		public T apply(@Nullable T value)
		{
			return second.apply(first.apply(value));
		}
	}

	/**
	 * Creates a {@link Function} that applies {@code elementTransformer} to each element of the
	 * input {@link List} and puts the new elements in a new , immutable, list and returns it.
	 */
	@Nonnull
	public static <E> Function<List<E>, List<E>> listTransformer(Function<E, E> elementTransformer)
	{
		checkNotNull(elementTransformer);
		if(elementTransformer == Functions.identity())
			return Functions.identity();
		return new ListTransformer<E>(elementTransformer);
	}

	private static final class ListTransformer<E> implements Function<List<E>, List<E>>
	{
		private final Function<E, E> elementTransformer;

		private ListTransformer(Function<E, E> elementTransformer)
		{
			this.elementTransformer = elementTransformer;
		}

		@Override
		public List<E> apply(List<E> values)
		{
			if(values == null)
				return null;
			return ImmutableList.copyOf(Lists.transform(values, elementTransformer));
		}
	}

	/**
	 * Creates a {@link Function} that applies {@code valueTransformer} to each value of the
	 * input {@link Map} and puts them in a new , immutable, map and returns it.
	 */
	@Nonnull
	public static <K, V> Function<Map<K, V>, Map<K, V>> mapValueTransformer(Function<V, V> valueTransformer)
	{
		if(valueTransformer == Functions.identity())
			return Functions.identity();
		return new MapValueTransformer<K, V>(valueTransformer);
	}

	private static final class MapValueTransformer<K, V> implements Function<Map<K, V>, Map<K, V>>
	{
		private final Function<V, V> valueTransformer;

		private MapValueTransformer(Function<V, V> valueTransformer)
		{
			this.valueTransformer = checkNotNull(valueTransformer);
		}

		@Override
		public Map<K, V> apply(Map<K, V> map)
		{
			if(map == null)
				return null;
			return ImmutableMap.copyOf(Maps.transformValues(map, valueTransformer));
		}
	}

	/**
	 * Creates a {@link Function} that wraps {@link List}s with
	 * {@link Collections#unmodifiableList(List)}
	 */
	@Nonnull
	public static <E> Function<List<E>, List<E>> unmodifiableList()
	{
		return new UnmodifiableListMaker<E>();
	}

	private static final class UnmodifiableListMaker<E> implements Function<List<E>, List<E>>
	{
		@Override
		public List<E> apply(List<E> value)
		{
			if(value == null)
				return null;
			return Collections.unmodifiableList(value);
		}
	}

	/**
	 * Creates a {@link Function} that wraps {@link Map}s with
	 * {@link Collections#unmodifiableMap(Map)}
	 */
	@Nonnull
	public static <K, V> Function<Map<K, V>, Map<K, V>> unmodifiableMap()
	{
		return new UnmodifiableMapMaker<K, V>();
	}

	private static final class UnmodifiableMapMaker<K, V> implements Function<Map<K, V>, Map<K, V>>
	{
		@Override
		public Map<K, V> apply(Map<K, V> value)
		{
			if(value == null)
				return null;
			return Collections.unmodifiableMap(value);
		}
	}

	/**
	 * Runs {@code function} {@code times} times.
	 * The result of the first call is passed to the next apply in the chain and so on.
	 * For instance:
	 * 
	 * <pre class="prettyprint">
	 * <code class="language-java">
	 * assertThat(Functions2.repeat(ADD_ONE, 2).apply(0)).isEqualTo(2);
	 * </code>
	 * </pre>
	 */
	@Nonnull
	public static <T> Function<T, T> repeat(Function<T, T> function, long times)
	{
		checkNotNull(function);
		checkArgument(times > 0, "Useless to repeat something zero times");
		return new FunctionRepeater<T>(function, times);
	}

	private static final class FunctionRepeater<T> implements Function<T, T>
	{
		private final long times;
		@Nonnull private final Function<T, T> function;

		private FunctionRepeater(Function<T, T> function, long times)
		{
			this.function = function;
			this.times = times;
		}

		@Override
		public T apply(T input)
		{
			T output = input;
			for(long i = 0; i < times; i++)
			{
				output = function.apply(output);
			}
			return output;
		}
	}

	/**
	 * Returns a {@link Function} that reads whole {@link File}s into {@link String}s using the
	 * {@link Charsets#UTF_8 UTF-8} charset.
	 */
	public static Function<File, String> fileToString()
	{
		return FileToString.INSTANCE;
	}

	private static final class FileToString implements Function<File, String>
	{
		private static final Function<File, String> INSTANCE = new FileToString();

		@Override
		public String apply(@Nonnull File input)
		{
			if(input.isDirectory())
				throw new IllegalArgumentException(input.getAbsolutePath() + " is a directory, not a file");
			try
			{
				return Files.toString(input, Charsets.UTF_8);
			}
			catch(IOException e)
			{
				throw new IllegalArgumentException("I/O error occured while reading: " + input.getAbsolutePath(), e);
			}
		}
	}
}
