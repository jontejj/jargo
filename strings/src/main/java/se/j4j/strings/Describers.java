package se.j4j.strings;

import static com.google.common.base.Preconditions.checkNotNull;
import static se.j4j.strings.StringsUtil.NEWLINE;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import se.j4j.texts.Texts;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

/**
 * Gives you static access to implementations of the {@link Describer} interface.
 */
public final class Describers
{
	private Describers()
	{
	}

	/**
	 * Always describes any value of type {@code T} with the given {@code constant}. For instance,
	 * if you have implemented a time related parser and don't want different times
	 * in the usage depending on when the usage is printed you could pass in "The current time",
	 * then this describer would describe any time object with "The current time".
	 */
	@Nonnull
	@CheckReturnValue
	public static <T> Describer<T> withConstantString(final String constant)
	{
		return new ConstantStringDescriber<T>(constant);
	}

	private static final class ConstantStringDescriber<T> implements Describer<T>
	{
		private final String constant;

		private ConstantStringDescriber(final String constant)
		{
			this.constant = constant;
		}

		@Override
		public String describe(T value)
		{
			return constant;
		}
	}

	/**
	 * Describes {@link Character}s by printing explanations for unprintable characters.
	 */
	@Nonnull
	@CheckReturnValue
	public static Describer<Character> characterDescriber()
	{
		return CharDescriber.INSTANCE;
	}

	private static final class CharDescriber implements Describer<Character>
	{
		private static final Describer<Character> INSTANCE = new CharDescriber();

		@Override
		public String describe(Character value)
		{
			if(value == null)
				return "null";
			return ((int) value == 0) ? "the Null character" : value.toString();
		}
	}

	/**
	 * Describes {@link File}s with {@link File#getAbsolutePath()} instead of {@link File#getPath()}
	 * as {@link File#toString()} does.
	 */
	@Nonnull
	@CheckReturnValue
	public static Describer<File> fileDescriber()
	{
		return FileDescriber.INSTANCE;
	}

	private static final class FileDescriber implements Describer<File>
	{
		private static final Describer<File> INSTANCE = new FileDescriber();

		@Override
		public String describe(File file)
		{
			return file.getAbsolutePath();
		}
	}

	/**
	 * Describes a boolean as enabled when true and disabled when false
	 */
	@Nonnull
	@CheckReturnValue
	public static Describer<Boolean> booleanAsEnabledDisabled()
	{
		return BooleanDescribers.ENABLED_DISABLED;
	}

	/**
	 * Describes a boolean as on when true and off when false
	 */
	@Nonnull
	@CheckReturnValue
	public static Describer<Boolean> booleanAsOnOff()
	{
		return BooleanDescribers.ON_OFF;
	}

	@VisibleForTesting
	enum BooleanDescribers implements Describer<Boolean>
	{
		ENABLED_DISABLED
		{
			@Override
			public String describe(Boolean value)
			{
				return value ? "enabled" : "disabled";
			}
		},
		ON_OFF
		{
			@Override
			public String describe(Boolean value)
			{
				return value ? "on" : "off";
			}
		};
	}

	/**
	 * Describes what a key=value in a {@link Map} means by fetching a description from
	 * {@code descriptions} for each key in a given map. So {@code descriptions} need to have
	 * values (descriptions) for all keys in any given map, otherwise a {@link NullPointerException}
	 * is thrown.
	 * For example:
	 * 
	 * <pre class="prettyprint">
	 * <code class="language-java">
	 * Map<String, Integer> defaults = newLinkedHashMap();
	 * defaults.put("population", 42);
	 * defaults.put("hello", 1);
	 * 
	 * Map<String, String> descriptions = newLinkedHashMap();
	 * descriptions.put("population", "The number of citizens in the world");
	 * descriptions.put("hello", "The number of times to say hello");
	 * Describer<Map<String, Integer>> d = mapDescriber(descriptions);
	 * 
	 * String describedMap = d.describe(defaults);
	 * </code>
	 * 
	 * would return:
	 * population=42
	 * 	The number of citizens in the world
	 * hello=1
	 * 	The number of times to say hello
	 * </pre>
	 * 
	 * You could even implement a
	 * {@code addProperty(String key, int defaultValue, String description)} method to enforce the
	 * use of descriptions.
	 */
	@Nonnull
	@CheckReturnValue
	public static <K, V> Describer<Map<K, V>> mapDescriber(Map<K, String> descriptions)
	{
		return new MapDescriber<K, V>(ImmutableMap.copyOf(descriptions));
	}

	// TODO: introduce <K,V> mapDescriber(Map<K, String> descriptions, Describer<V> valueDescriber)

	private static final class MapDescriber<K, V> implements Describer<Map<K, V>>
	{
		private final Map<K, String> descriptions;

		private MapDescriber(Map<K, String> descriptions)
		{
			this.descriptions = descriptions;
		}

		@Override
		public String describe(Map<K, V> values)
		{
			StringBuilder result = new StringBuilder();
			for(Entry<K, V> entry : values.entrySet())
			{
				K key = entry.getKey();
				result.append(key);
				result.append("=");
				result.append(entry.getValue());
				String descriptionForEntry = descriptions.get(key);
				checkNotNull(descriptionForEntry, Texts.UNDESCRIBED_KEY, key);
				result.append(NEWLINE).append(" ").append(descriptionForEntry).append(NEWLINE);
			}
			return result.toString();
		}
	}

	/**
	 * <pre>
	 * Exposes a {@link Describer} as a Guava {@link Function}.
	 * <b>Note:</b>This method may be removed in the future if Guava is removed as a dependency.
	 * 
	 * @param describer the describer to convert to a {@link Function}
	 * @return a {@link Function} that applies {@link Describer#describe(Object)} to input values.
	 * </pre>
	 */
	@Beta
	@Nonnull
	@CheckReturnValue
	public static <T> Function<T, String> asFunction(final Describer<T> describer)
	{
		checkNotNull(describer);
		return new Function<T, String>(){
			@Override
			public String apply(T input)
			{
				return describer.describe(input);
			}
		};
	}

	/**
	 * <pre>
	 * Exposes a {@link Function} as a {@link Describer}.
	 * <b>Note:</b>This method may be removed in the future if Guava is removed as a dependency.
	 * 
	 * @param describerFunction a function that can convert {@code T} values into {@link String}s
	 * @return a {@link Describer} that applies {@link Function#apply(Object)} to describe input values.
	 * </pre>
	 */
	@Beta
	@Nonnull
	@CheckReturnValue
	public static <T> Describer<T> usingFunction(final Function<T, String> describerFunction)
	{
		checkNotNull(describerFunction);
		return new Describer<T>(){
			@Override
			public String describe(T value)
			{
				return describerFunction.apply(value);
			}
		};
	}

	/**
	 * Describes values in lists with {@code valueDescriber}.
	 * Lists with {@link List#size()} = zero is described with the string "Empty List".
	 */
	@Nonnull
	@CheckReturnValue
	public static <T> Describer<List<T>> listDescriber(Describer<T> valueDescriber)
	{
		checkNotNull(valueDescriber);
		return new ListDescriber<T>(valueDescriber);
	}

	private static final class ListDescriber<T> implements Describer<List<T>>
	{
		private final Describer<T> valueDescriber;

		ListDescriber(Describer<T> valueDescriber)
		{
			this.valueDescriber = valueDescriber;
		}

		@Override
		public String describe(List<T> value)
		{
			if(value.isEmpty())
				return "Empty list";

			Iterator<T> values = value.iterator();
			String firstValue = valueDescriber.describe(values.next());

			StringBuilder sb = new StringBuilder(value.size() * firstValue.length());

			sb.append('[').append(firstValue);
			while(values.hasNext())
			{
				sb.append(", ").append(valueDescriber.describe(values.next()));
			}
			sb.append(']');
			return sb.toString();
		}
	}
}
