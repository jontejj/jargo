package se.j4j.strings;

import static com.google.common.base.Preconditions.checkNotNull;
import static se.j4j.strings.StringsUtil.NEWLINE;

import java.io.File;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

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
	 * if you have implemented a time related parser and don't want different times in the
	 * usage depending on when the usage is printed you could pass in "The current time", then this
	 * describer would describe any time object with "The current time".
	 */
	@Nonnull
	@CheckReturnValue
	public static <T> Describer<T> withConstantString(final String constant)
	{
		checkNotNull(constant);
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
		public String describe(T value, Locale inLocale)
		{
			return constant;
		}
	}

	/**
	 * Calls {@link String#valueOf(Object)} for input values. As this goes
	 * against the very purpose of the {@link Describer} interface it may seem
	 * odd but this {@link Describer} works really well as a null object, it can
	 * also act as a functor for calling toString.
	 */
	@Nonnull
	@CheckReturnValue
	public static <T> Describer<T> toStringDescriber()
	{
		@SuppressWarnings("unchecked")
		Describer<T> toStringDescriber = (Describer<T>) ToStringDescriber.INSTANCE;
		return toStringDescriber;
	}

	private static final class ToStringDescriber implements Describer<Object>
	{
		private static final Describer<Object> INSTANCE = new ToStringDescriber();

		private ToStringDescriber()
		{
		}

		@Override
		public String describe(Object value, Locale inLocale)
		{
			return String.valueOf(value);
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
		public String describe(Character value, Locale inLocale)
		{
			if(value == null)
				return "null";
			// TODO: describe more characters? All ASCII characters perhaps?
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
		public String describe(File file, Locale inLocale)
		{
			if(file == null)
				return "null";
			return file.getAbsolutePath();
		}
	}

	/**
	 * Describes a boolean as enabled when {@code true} and disabled when {@code false}
	 */
	@Nonnull
	@CheckReturnValue
	public static Describer<Boolean> booleanAsEnabledDisabled()
	{
		return BooleanDescribers.ENABLED_DISABLED;
	}

	/**
	 * Describes a boolean as on when {@code true} and off when {@code false}
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
			public String describe(Boolean value, Locale inLocale)
			{
				return value ? "enabled" : "disabled";
			}
		},
		ON_OFF
		{
			@Override
			public String describe(Boolean value, Locale inLocale)
			{
				return value ? "on" : "off";
			}
		};
	}

	/**
	 * Describes {@link Number}s in a {@link Locale} sensitive manner using {@link NumberFormat}.
	 */
	public static Describer<Number> numberDescriber()
	{
		return NumberDescriber.INSTANCE;
	}

	private static final class NumberDescriber implements Describer<Number>
	{
		private static final Describer<Number> INSTANCE = new NumberDescriber();

		@Override
		public String describe(Number number, Locale locale)
		{
			if(number == null)
				return "null";
			return NumberFormat.getInstance(locale).format(number);
		}

		@Override
		public String toString()
		{
			return "NumberDescriber";
		}
	}

	/**
	 * Describes what a key=value in a {@link Map} means by fetching a
	 * description from {@code descriptions} for each key in a given map. So {@code descriptions}
	 * need to have values (descriptions) for all keys in any given map,
	 * otherwise a {@link NullPointerException} is thrown. For example:
	 * 
	 * <pre class="prettyprint">
	 * <code class="language-java">
	 * Map&lt;String, Integer&gt; defaults = newLinkedHashMap();
	 * defaults.put("population", 42);
	 * defaults.put("hello", 1);
	 * 
	 * Map&lt;String, String&gt; descriptions = newLinkedHashMap();
	 * descriptions.put("population", "The number of citizens in the world");
	 * descriptions.put("hello", "The number of times to say hello");
	 * Describer&lt;Map&lt;String, Integer&gt;&gt; d = mapDescriber(descriptions);
	 * 
	 * String describedMap = d.describe(defaults);
	 * </code>
	 * </pre>
	 * 
	 * would return:
	 * 
	 * <pre>
	 * <code>
	 * population=42
	 * 	The number of citizens in the world
	 * hello=1
	 * 	The number of times to say hello
	 * </code>
	 * </pre>
	 * 
	 * You could even implement a
	 * {@code addProperty(String key, int defaultValue, String description)} method to enforce the
	 * use of descriptions at compile-time.
	 */
	@Nonnull
	@CheckReturnValue
	public static <K, V> Describer<Map<K, V>> mapDescriber(Map<K, String> descriptions)
	{
		return new MapDescription<K, V>(ImmutableMap.copyOf(descriptions), Describers.<K>toStringDescriber());
	}

	/**
	 * Works like {@link #mapDescriber(Map)} but it describes keys in any given {@link Map} with
	 * {@code keyDescriber} instead of with {@link #toString()}
	 * 
	 * @param descriptions a map with strings describing what each key in it means
	 * @param keyDescriber {@link Describer} used to transform keys into {@link String}s with
	 * @return a {@link Describer} that can describe a map of the type {@code Map<K, V>} and where
	 *         each key must have a corresponding description in {@code descriptions}
	 */
	public static <K, V> Describer<Map<K, V>> mapDescriber(Map<K, String> descriptions, Describer<K> keyDescriber)
	{
		return new MapDescription<K, V>(ImmutableMap.copyOf(descriptions), checkNotNull(keyDescriber));
	}

	private static final class MapDescription<K, V> implements Describer<Map<K, V>>
	{
		private final Map<K, String> descriptions;
		private final Describer<K> keyDescriber;

		private MapDescription(Map<K, String> descriptions, Describer<K> keyDescriber)
		{
			this.descriptions = descriptions;
			this.keyDescriber = keyDescriber;
		}

		@Override
		public String describe(Map<K, V> values, Locale inLocale)
		{
			StringBuilder result = new StringBuilder();
			for(Entry<K, V> entry : values.entrySet())
			{
				K key = entry.getKey();
				result.append(keyDescriber.describe(key, inLocale));
				result.append("=");
				result.append(entry.getValue());
				String descriptionForEntry = descriptions.get(key);
				checkNotNull(descriptionForEntry, "Undescribed key: %s", key);
				result.append(NEWLINE).append(" ").append(descriptionForEntry).append(NEWLINE);
			}
			return result.toString();
		}
	}

	/**
	 * Describes key values in a {@link Map}. Keys are described with
	 * {@link Describers#toStringDescriber()} and values with {@code valueDescriber}. "=" is used as
	 * the separator between key and value. {@link StringsUtil#NEWLINE} separates entries.
	 */
	@CheckReturnValue
	@Nonnull
	public static <K, V> Describer<Map<K, V>> mapDescriber(Describer<V> valueDescriber)
	{
		return new MapDescriber<K, V>(Describers.<K>toStringDescriber(), valueDescriber, "=");
	}

	/**
	 * Describes key values in a {@link Map}. Keys are described with
	 * {@link Describers#toStringDescriber()} and values with {@code valueDescriber}.
	 * {@code valueSeparator} is used as the separator between key and value.
	 * {@link StringsUtil#NEWLINE} separates entries.
	 */
	@CheckReturnValue
	@Nonnull
	public static <K, V> Describer<Map<K, V>> mapDescriber(Describer<V> valueDescriber, String valueSeparator)
	{
		return new MapDescriber<K, V>(Describers.<K>toStringDescriber(), valueDescriber, valueSeparator);
	}

	/**
	 * Describes key values in a {@link Map}. Keys are described with {@code keyDescriber} and
	 * values with {@code valueDescriber}.
	 * "=" is used as the separator between key and value. {@link StringsUtil#NEWLINE} separates
	 * entries.
	 */
	@CheckReturnValue
	@Nonnull
	public static <K, V> Describer<Map<K, V>> mapDescriber(Describer<K> keyDescriber, Describer<V> valueDescriber)
	{
		return new MapDescriber<K, V>(keyDescriber, valueDescriber, "=");
	}

	/**
	 * Describes key values in a {@link Map}. Keys are described with {@code keyDescriber} and
	 * values with {@code valueDescriber}. {@code valueSeparator} is used as the separator between
	 * key and value. {@link StringsUtil#NEWLINE} separates entries.
	 */
	@CheckReturnValue
	@Nonnull
	public static <K, V> Describer<Map<K, V>> mapDescriber(Describer<K> keyDescriber, Describer<V> valueDescriber, String valueSeparator)
	{
		return new MapDescriber<K, V>(keyDescriber, valueDescriber, valueSeparator);
	}

	private static final class MapDescriber<K, V> implements Describer<Map<K, V>>
	{
		private final Describer<V> valueDescriber;
		private final Describer<K> keyDescriber;
		private final String valueSeparator;

		private MapDescriber(Describer<K> keyDescriber, Describer<V> valueDescriber, String valueSeparator)
		{
			this.keyDescriber = checkNotNull(keyDescriber);
			this.valueDescriber = checkNotNull(valueDescriber);
			this.valueSeparator = checkNotNull(valueSeparator);
		}

		@Override
		public String describe(Map<K, V> values, Locale inLocale)
		{
			if(values == null)
				return "null";
			Iterator<Entry<K, V>> iterator = values.entrySet().iterator();
			if(!iterator.hasNext())
				return "Empty map";

			StringBuilder firstKeyValue = new StringBuilder();
			describeEntry(iterator.next(), inLocale, firstKeyValue);

			// TODO: what if it's expensive to call size()?
			StringBuilder result = new StringBuilder(firstKeyValue.length() * values.size());
			result.append(firstKeyValue);

			while(iterator.hasNext())
			{
				result.append(NEWLINE);
				describeEntry(iterator.next(), inLocale, result);
			}
			return result.toString();
		}

		private void describeEntry(Entry<K, V> entry, Locale inLocale, StringBuilder output)
		{
			output.append(keyDescriber.describe(entry.getKey(), inLocale));
			output.append(valueSeparator);
			output.append(valueDescriber.describe(entry.getValue(), inLocale));
		}
	}

	/**
	 * <pre>
	 * Exposes a {@link Describer} as a Guava {@link Function}.
	 * <b>Note:</b>This method may be removed in the future if Guava is removed as a dependency.
	 * 
	 * @param describer the describer to convert to a {@link Function}
	 * @return a {@link Function} that applies {@link Describer#describe(Object, Locale)} to input values.
	 * </pre>
	 */
	@Beta
	@Nonnull
	@CheckReturnValue
	public static <T> Function<? super T, String> asFunction(final Describer<T> describer)
	{
		checkNotNull(describer);
		return new Function<T, String>(){
			@Override
			public String apply(@Nonnull T input)
			{
				// TODO: document locale
				return describer.describe(input, Locale.getDefault());
			}
		};
	}

	/**
	 * <pre>
	 * Exposes a {@link Function} as a {@link Describer}.
	 * <b>Note:</b>This method may be removed in the future if Guava is removed as a dependency.
	 * 
	 * @param describerFunction a function that can convert {@code T} values into {@link String}s
	 * @return a {@link Describer} that applies {@link Function#apply(Object)} to {@link Describer#describe(Object, Locale)} input values.
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
			public String describe(T value, Locale inLocale)
			{
				return describerFunction.apply(value);
			}
		};
	}

	/**
	 * Describes values in lists with {@code valueDescriber} and separates
	 * elements with ", ". Lists with {@link List#size()} = zero is described
	 * with the string "Empty List".
	 */
	@Nonnull
	@CheckReturnValue
	public static <T> Describer<List<? extends T>> listDescriber(Describer<T> valueDescriber)
	{
		checkNotNull(valueDescriber);
		return new ListDescriber<T>(valueDescriber, ", ");
	}

	/**
	 * Describes values in lists with {@code valueDescriber} and separates
	 * values with {@code valueSeperator}. Lists with {@link List#size()} = zero
	 * is described with the string "Empty List".
	 */
	@Nonnull
	@CheckReturnValue
	public static <T> Describer<List<? extends T>> listDescriber(Describer<T> valueDescriber, String valueSeparator)
	{
		checkNotNull(valueDescriber);
		checkNotNull(valueSeparator);
		return new ListDescriber<T>(valueDescriber, valueSeparator);
	}

	private static final class ListDescriber<T> implements Describer<List<? extends T>>
	{
		private final Describer<T> valueDescriber;
		private final String valueSeparator;

		ListDescriber(Describer<T> valueDescriber, String valueSeparator)
		{
			this.valueDescriber = valueDescriber;
			this.valueSeparator = valueSeparator;
		}

		@Override
		public String describe(List<? extends T> value, Locale inLocale)
		{
			if(value == null)
				return "null";
			if(value.isEmpty())
				return "Empty list";

			Iterator<? extends T> values = value.iterator();
			String firstValue = valueDescriber.describe(values.next(), inLocale);

			StringBuilder sb = new StringBuilder(value.size() * firstValue.length());

			sb.append(firstValue);
			while(values.hasNext())
			{
				sb.append(valueSeparator).append(valueDescriber.describe(values.next(), inLocale));
			}
			return sb.toString();
		}
	}
}
