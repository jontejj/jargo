package se.j4j.argumentparser;

import static java.math.BigInteger.ZERO;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static se.j4j.argumentparser.ArgumentExceptions.forErrorCode;
import static se.j4j.argumentparser.ArgumentExceptions.forInvalidValue;
import static se.j4j.argumentparser.ArgumentExceptions.forUnhandledRepeatedArgument;
import static se.j4j.argumentparser.ArgumentExceptions.withDescription;
import static se.j4j.argumentparser.ArgumentExceptions.ArgumentExceptionCodes.MISSING_PARAMETER;
import static se.j4j.argumentparser.internal.StringsUtil.toLowerCase;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentExceptions.InvalidArgument;
import se.j4j.argumentparser.internal.ListUtil;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.primitives.UnsignedLongs;

/**
 * Gives you static access to implementations of the {@link StringParser} interface.
 * All methods return {@link Immutable} parsers.
 * Most methods here returns the same instance for every call.
 */
@Immutable
public final class StringParsers
{
	private StringParsers()
	{
	}

	/**
	 * @return the simplest possible parser, it simply returns the strings it's given to parse.
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<String> stringParser()
	{
		return DefaultStringParser.INSTANCE;
	}

	/**
	 * @return a parser that parse strings with {@link Boolean#valueOf(String)}
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Boolean> booleanParser()
	{
		return BooleanParser.INSTANCE;
	}

	/**
	 * @return a parser that passes the strings to {@link File#File(String)}
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<File> fileParser()
	{
		return FileParser.INSTANCE;
	}

	/**
	 * @return a parser that uses the first character in a {@link String} as a {@link Character} and
	 *         that throws {@link InvalidArgument} for any given {@link String} with more than one
	 *         {@link Character}
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Character> charParser()
	{
		return CharParser.INSTANCE;
	}

	/**
	 * @param enumToHandle the {@link Class} literal for the {@link Enum} to parse strings into
	 * @return a {@link StringParser} that parse strings into enum values of the type <code>T</code>
	 */
	@CheckReturnValue
	@Nonnull
	public static <T extends Enum<T>> StringParser<T> enumParser(final Class<T> enumToHandle)
	{
		return new EnumParser<T>(enumToHandle);
	}

	/**
	 * @return a parser that uses <code>radix</code> to parse {@link String}s into {@link Integer}s
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Integer> integerParser(Radix radix)
	{
		if(radix.equals(Radix.DECIMAL))
			return IntegerParser.DECIMAL_INSTANCE;
		return new IntegerParser(radix);
	}

	/**
	 * @return a parser that uses {@link Radix#DECIMAL} to parse {@link String}s into
	 *         {@link Integer}s
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Integer> integerParser()
	{
		return IntegerParser.DECIMAL_INSTANCE;
	}

	/**
	 * @return a parser that uses <code>radix</code> to parse {@link String}s into {@link Byte}s
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Byte> byteParser(Radix radix)
	{
		return new ByteParser(radix);
	}

	/**
	 * @return a parser that uses <code>radix</code> to parse {@link String}s into {@link Short}s
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Short> shortParser(Radix radix)
	{
		return new ShortParser(radix);
	}

	/**
	 * @return a parser that uses <code>radix</code> to parse {@link String}s into {@link Long}s
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Long> longParser(Radix radix)
	{
		return new LongParser(radix);
	}

	/**
	 * @return a parser that parse {@link String}s into {@link Double}s
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Double> doubleParser()
	{
		return DoubleParser.INSTANCE;
	}

	/**
	 * @return a parser that parse {@link String}s into {@link Float}s
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Float> floatParser()
	{
		return FloatParser.INSTANCE;
	}

	/**
	 * @return a parser that parse {@link String}s into {@link BigInteger}s
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<BigInteger> bigIntegerParser()
	{
		return BigIntegerParser.INSTANCE;
	}

	/**
	 * <pre>
	 * Makes it possible to use the given <code>parser</code> as a {@link Function}.
	 * For example:<code>
	 * List&lt;Integer&gt; result = transform(asList("1", "3", "2"), asFunction(integerParser()));
	 * </code>
	 * 
	 * @param parser the parser to expose as a {@link Function}
	 * @return <code>parser</code> exposed in a {@link Function} that
	 * throws {@link IllegalArgumentException} when given faulty values.
	 * </pre>
	 */
	@Nonnull
	@CheckReturnValue
	public static <T> Function<String, T> asFunction(@Nonnull final StringParser<T> parser)
	{
		return new Function<String, T>(){
			@Override
			public T apply(String input)
			{
				try
				{
					return parser.parse(input);
				}
				catch(final ArgumentException e)
				{
					throw withDescription(new Description(){
						@Override
						public String description()
						{
							return e.getMessage();
						}
					}, e);
				}
			}
		};
	}

	@CheckReturnValue
	@Nonnull
	static InternalStringParser<Boolean> optionParser(final boolean defaultValue)
	{
		if(defaultValue)
			return OptionParser.DEFAULT_TRUE;
		return OptionParser.DEFAULT_FALSE;
	}

	/**
	 * Simple returns the strings it's given to parse
	 */
	private static final class DefaultStringParser implements StringParser<String>
	{
		private static final StringParser<String> INSTANCE = new DefaultStringParser();

		@Override
		public String parse(final String value)
		{
			return value;
		}

		@Override
		public String descriptionOfValidValues()
		{
			return "Any string";
		}

		@Override
		public String defaultValue()
		{
			return "";
		}
	}

	/**
	 * Returns a Boolean with a value represented by the next string in the arguments.
	 * The Boolean returned represents a true value if the string argument is
	 * equal, ignoring case, to the string "true".
	 */
	private static final class BooleanParser extends OneParameterParser<Boolean>
	{
		private static final BooleanParser INSTANCE = new BooleanParser();

		@Override
		public Boolean parse(final String value)
		{
			return Boolean.valueOf(value);
		}

		@Override
		public String descriptionOfValidValues()
		{
			return "true or false";
		}

		@Override
		public Boolean defaultValue()
		{
			return false;
		}
	}

	private static final class FileParser extends OneParameterParser<File>
	{
		private static final FileParser INSTANCE = new FileParser();

		@Override
		public File parse(final String value)
		{
			return new File(value);
		}

		@Override
		public String descriptionOfValidValues()
		{
			return "a file path";
		}

		@Override
		public File defaultValue()
		{
			return new File("");
		}

		@Override
		public String describeValue(File value)
		{
			return value.getAbsolutePath();
		}
	}

	private static final class CharParser extends OneParameterParser<Character>
	{
		private static final CharParser INSTANCE = new CharParser();

		@Override
		public Character parse(final String value) throws ArgumentException
		{
			if(value.length() != 1)
				throw forInvalidValue(value, " is not a valid character");

			return value.charAt(0);
		}

		@Override
		public String descriptionOfValidValues()
		{
			return "Any unicode character";
		}

		@Override
		public Character defaultValue()
		{
			return 0;
		}
	}

	private static final class EnumParser<T extends Enum<T>> implements StringParser<T>
	{
		private final Class<T> enumType;

		private EnumParser(final Class<T> enumToHandle)
		{
			enumType = enumToHandle;
		}

		@Override
		public T parse(final String value) throws ArgumentException
		{
			try
			{
				// TODO: add possibility to convert strings to uppercase before finding the enum
				// value
				return Enum.valueOf(enumType, value);
			}
			catch(IllegalArgumentException noEnumFound)
			{
				throw forInvalidValue(value, " is not a valid Option, Expecting one of " + descriptionOfValidValues());
			}
		}

		@Override
		public String descriptionOfValidValues()
		{
			return EnumSet.allOf(enumType).toString();
		}

		@Override
		public T defaultValue()
		{
			return null;
		}
	}

	private static final class OptionParser extends InternalStringParser<Boolean>
	{
		private static final OptionParser DEFAULT_FALSE = new OptionParser(false);
		private static final OptionParser DEFAULT_TRUE = new OptionParser(true);

		private final Boolean defaultValue;
		private final Boolean enabledValue;

		private OptionParser(final boolean defaultValue)
		{
			this.defaultValue = defaultValue;
			this.enabledValue = !defaultValue;
		}

		@Override
		public Boolean defaultValue()
		{
			return defaultValue;
		}

		/**
		 * Only the existence of the flag matters, no specific value.
		 * Use {@link ArgumentBuilder#description(String)} to describe this
		 * argument.
		 */
		@Override
		public String descriptionOfValidValues()
		{
			return "";
		}

		@Override
		Boolean parse(ListIterator<String> currentArgument, Boolean oldValue, Argument<?> argumentDefinition) throws ArgumentException
		{
			return enabledValue;
		}

		@Override
		String describeValue(Boolean value)
		{
			return value.toString();
		}
	}

	private static final class BigIntegerParser implements StringParser<BigInteger>
	{
		private static final StringParser<BigInteger> INSTANCE = new BigIntegerParser();

		@Override
		public String descriptionOfValidValues()
		{
			return "Any integer";
		}

		@Override
		public BigInteger parse(final String value) throws ArgumentException
		{
			try
			{
				return new BigInteger(value);
			}
			catch(NumberFormatException cause)
			{
				InvalidArgument e = forInvalidValue(value, " is not a valid big-integer");
				e.initCause(cause);
				throw e;
			}
		}

		@Override
		public BigInteger defaultValue()
		{
			return ZERO;
		}
	}

	private static final class DoubleParser implements StringParser<Double>
	{
		private static final DoubleParser INSTANCE = new DoubleParser();

		@Override
		public Double parse(final String value) throws ArgumentException
		{
			try
			{
				return Double.valueOf(value);
			}
			catch(NumberFormatException cause)
			{
				InvalidArgument e = forInvalidValue(value, " is not a valid double (64-bit IEEE 754 floating point)");
				e.initCause(cause);
				throw e;
			}
		}

		@Override
		public String descriptionOfValidValues()
		{
			return -Double.MAX_VALUE + " to " + Double.MAX_VALUE;
		}

		@Override
		public Double defaultValue()
		{
			return 0.0;
		}
	}

	private static final class FloatParser implements StringParser<Float>
	{
		private static final FloatParser INSTANCE = new FloatParser();

		@Override
		public Float parse(final String value) throws ArgumentException
		{
			try
			{
				return Float.valueOf(value);
			}
			catch(NumberFormatException cause)
			{
				InvalidArgument e = forInvalidValue(value, " is not a valid float (32-bit IEEE 754 floating point)");
				e.initCause(cause);
				throw e;
			}
		}

		@Override
		public String descriptionOfValidValues()
		{
			return -Float.MAX_VALUE + " to " + Float.MAX_VALUE;
		}

		@Override
		public Float defaultValue()
		{
			return 0f;
		}
	}

	private static final class IntegerParser extends RadixiableParser<Integer>
	{
		private static final StringParser<Integer> DECIMAL_INSTANCE = new IntegerParser(Radix.DECIMAL);

		IntegerParser(final Radix radix)
		{
			super(radix);
		}

		@Override
		public Integer minValue()
		{
			return Integer.MIN_VALUE;
		}

		@Override
		public Integer maxValue()
		{
			return Integer.MAX_VALUE;
		}

		@Override
		public Integer defaultValue()
		{
			return 0;
		}

		@Override
		protected Integer cast(Long value)
		{
			return value.intValue();
		}

		@Override
		protected Long cast(Integer value)
		{
			return value.longValue();
		}

		@Override
		protected int bitSize()
		{
			return Integer.SIZE;
		}
	}

	private static final class ByteParser extends RadixiableParser<Byte>
	{
		ByteParser(final Radix radix)
		{
			super(radix);
		}

		@Override
		public Byte minValue()
		{
			return Byte.MIN_VALUE;
		}

		@Override
		public Byte maxValue()
		{
			return Byte.MAX_VALUE;
		}

		@Override
		public Byte defaultValue()
		{
			return 0;
		}

		@Override
		protected Byte cast(Long value)
		{
			return value.byteValue();
		}

		@Override
		protected Long cast(Byte value)
		{
			return value.longValue();
		}

		@Override
		protected int bitSize()
		{
			return Byte.SIZE;
		}
	}

	private static final class ShortParser extends RadixiableParser<Short>
	{
		ShortParser(final Radix radix)
		{
			super(radix);
		}

		@Override
		public Short minValue()
		{
			return Short.MIN_VALUE;
		}

		@Override
		public Short maxValue()
		{
			return Short.MAX_VALUE;
		}

		@Override
		public Short defaultValue()
		{
			return 0;
		}

		@Override
		protected Short cast(Long value)
		{
			return value.shortValue();
		}

		@Override
		protected Long cast(Short value)
		{
			return value.longValue();
		}

		@Override
		protected int bitSize()
		{
			return Short.SIZE;
		}
	}

	private static final class LongParser extends RadixiableParser<Long>
	{
		LongParser(final Radix radix)
		{
			super(radix);
		}

		@Override
		public Long minValue()
		{
			return Long.MIN_VALUE;
		}

		@Override
		public Long maxValue()
		{
			return Long.MAX_VALUE;
		}

		@Override
		public Long defaultValue()
		{
			return 0L;
		}

		@Override
		protected Long cast(Long value)
		{
			return value;
		}

		@Override
		protected int bitSize()
		{
			return Long.SIZE;
		}
	}

	/**
	 * <pre>
	 * Makes it possible to convert two (or more) {@link String}s into a single <code>T</code> value.
	 * For a simpler one use {@link StringParser}.
	 * 
	 * Pass the constructed {@link InternalStringParser} to {@link ArgumentBuilder#ArgumentBuilder(InternalStringParser)}
	 * to start building an {@link Argument} instance using it.
	 * 
	 * @param <T> the type this parser parses strings into
	 * </pre>
	 */
	@Immutable
	abstract static class InternalStringParser<T>
	{
		/**
		 * @param currentArgument an iterator where {@link Iterator#next()} points to the parameter
		 *            for a named argument, for an indexed argument it points to the single unnamed
		 *            argument
		 * @param previousOccurance the previously parsed value for this
		 *            argument if it appears several times, otherwise null
		 * @param argumentDefinition the definition that owns this converter,
		 *            can be used for providing good exception messages.
		 * @return the parsed value
		 * @throws ArgumentException if an error occurred while parsing the value
		 */
		abstract T parse(@Nonnull final ListIterator<String> currentArgument, @Nullable final T previousOccurance,
				@Nonnull final Argument<?> argumentDefinition) throws ArgumentException;

		/**
		 * Describes the values this parser accepts
		 * 
		 * @return a description string to show in usage texts
		 */
		@Nonnull
		public abstract String descriptionOfValidValues();

		/**
		 * If you can provide a sample value do so, it will look much better
		 * in the usage texts, if not return null
		 * 
		 * @return
		 */
		@Nullable
		public abstract T defaultValue();

		/**
		 * @return <code>value</code> described, or null if no description is
		 *         needed
		 */
		@Nullable
		abstract String describeValue(@Nullable T value);
	}

	abstract static class OneParameterParser<T> extends InternalStringParser<T> implements StringParser<T>
	{
		@Override
		T parse(final ListIterator<String> currentArgument, final T oldValue, final Argument<?> argumentDefinition) throws ArgumentException
		{
			if(!currentArgument.hasNext())
				// TODO: assert that error text looks good and is helpful
				throw forErrorCode(MISSING_PARAMETER);
			return parse(currentArgument.next());
		}

		@Override
		String describeValue(T value)
		{
			return String.valueOf(value);
		}
	}

	static final class StringSplitterParser<T> extends InternalStringParser<List<T>>
	{
		@Nonnull private final StringSplitter splitter;
		@Nonnull private final InternalStringParser<T> parser;

		StringSplitterParser(@Nonnull final StringSplitter splitter, @Nonnull final InternalStringParser<T> parser)
		{
			this.splitter = splitter;
			this.parser = parser;
		}

		@Override
		public String descriptionOfValidValues()
		{
			return parser.descriptionOfValidValues() + ", separated by " + splitter.description();
		}

		@Override
		List<T> parse(final ListIterator<String> currentArgument, final List<T> oldValue, final Argument<?> argumentDefinition)
				throws ArgumentException
		{
			if(!currentArgument.hasNext())
				throw forErrorCode(MISSING_PARAMETER);

			String values = currentArgument.next();

			Iterable<String> inputs = splitter.split(values);
			List<T> result = new ArrayList<T>();

			for(String value : inputs)
			{
				T parsedValue = parser.parse(Arrays.asList(value).listIterator(), null, argumentDefinition);
				result.add(parsedValue);
			}
			return result;
		}

		@Override
		public List<T> defaultValue()
		{
			return emptyList();
		}

		@Override
		String describeValue(List<T> value)
		{
			return ListUtil.describeList(value);
		}
	}

	static final class ListParser<T> extends InternalStringParser<List<T>>
	{
		private final InternalStringParser<T> parser;
		private final int argumentsToConsume;

		static final int CONSUME_ALL = -1;

		ListParser(final InternalStringParser<T> parser, final int argumentsToConsume)
		{
			this.parser = parser;
			this.argumentsToConsume = argumentsToConsume;
		}

		@Override
		List<T> parse(final ListIterator<String> currentArgument, final List<T> list, final Argument<?> argumentDefinition) throws ArgumentException
		{
			// TODO: fetch the actual value from currentArgument instead of 10
			int expectedSize = argumentsToConsume == CONSUME_ALL ? 10 : argumentsToConsume;
			List<T> parsedArguments = new ArrayList<T>(expectedSize);
			if(argumentsToConsume == CONSUME_ALL)
			{
				while(currentArgument.hasNext())
				{
					parsedArguments.add(parseValue(currentArgument, argumentDefinition));
				}
			}
			else
			{
				for(int i = 0; i < argumentsToConsume; i++)
				{
					parsedArguments.add(parseValue(currentArgument, argumentDefinition));
				}
			}
			return parsedArguments;
		}

		private T parseValue(final ListIterator<String> currentArgument, final Argument<?> argumentDefinition) throws ArgumentException
		{
			return parser.parse(currentArgument, null, argumentDefinition);
		}

		@Override
		public String descriptionOfValidValues()
		{
			// TODO: print meta descriptions
			return argumentsToConsume + " of " + parser.descriptionOfValidValues();
		}

		@Override
		public List<T> defaultValue()
		{
			return emptyList();
		}

		@Override
		String describeValue(List<T> value)
		{
			return ListUtil.describeList(value);
		}
	}

	/**
	 * Produced by {@link Argument#repeated()} and used by {@link CommandLineParser#parse(String)}
	 * 
	 * @param <T> type of the repeated values (such as {@link Integer} for {@link IntegerParser}
	 */
	static final class RepeatedArgumentParser<T> extends InternalStringParser<List<T>>
	{
		@Nonnull private final InternalStringParser<T> parser;

		RepeatedArgumentParser(@Nonnull final InternalStringParser<T> parser)
		{
			this.parser = parser;
		}

		@Override
		List<T> parse(final ListIterator<String> currentArgument, List<T> previouslyCreatedList, final Argument<?> argumentDefinition)
				throws ArgumentException
		{
			T parsedValue = parser.parse(currentArgument, null, argumentDefinition);

			List<T> listToStoreRepeatedValuesIn = previouslyCreatedList;
			if(listToStoreRepeatedValuesIn == null)
			{
				listToStoreRepeatedValuesIn = new ArrayList<T>();
			}

			listToStoreRepeatedValuesIn.add(parsedValue);
			return listToStoreRepeatedValuesIn;
		}

		@Override
		public String descriptionOfValidValues()
		{
			return parser.descriptionOfValidValues();
		}

		@Override
		public List<T> defaultValue()
		{
			return emptyList();
		}

		@Override
		String describeValue(List<T> value)
		{
			return ListUtil.describeList(value);
		}
	}

	static final class KeyValueParser<K extends Comparable<K>, V> extends InternalStringParser<Map<K, V>>
	{
		static final String DEFAULT_SEPARATOR = "=";

		@Nonnull private final InternalStringParser<V> valueParser;
		@Nonnull private final StringParser<K> keyParser;

		KeyValueParser(@Nonnull final InternalStringParser<V> valueParser, @Nonnull final StringParser<K> keyParser)
		{
			this.valueParser = valueParser;
			this.keyParser = keyParser;
		}

		@Override
		Map<K, V> parse(final ListIterator<String> currentArgument, Map<K, V> previousMap, final Argument<?> argumentDefinition)
				throws ArgumentException
		{
			Map<K, V> map = previousMap;
			if(map == null)
			{
				// TODO: should this create a LinkedHashMap instead?
				map = Maps.newHashMap();
			}

			String keyValue = currentArgument.next();
			String separator = argumentDefinition.separator();

			List<String> propertyIdentifiers = argumentDefinition.names();
			String argument = keyValue;
			if(argumentDefinition.isIgnoringCase())
			{
				propertyIdentifiers = toLowerCase(propertyIdentifiers);
				argument = argument.toLowerCase(Locale.getDefault());
			}

			for(String propertyIdentifier : propertyIdentifiers)
			{
				if(argument.startsWith(propertyIdentifier))
				{
					// Fetch key and value from "-Dkey=value"
					int keyStartIndex = propertyIdentifier.length();
					int keyEndIndex = keyValue.indexOf(separator, keyStartIndex);
					if(keyEndIndex == -1)
						throw forInvalidValue(keyValue, " Missing assignment operator(" + separator + ")");

					String key = keyValue.substring(keyStartIndex, keyEndIndex);
					K parsedKey = keyParser.parse(key);

					// Remove "-Dkey=" from "-Dkey=value"
					String value = keyValue.substring(keyEndIndex + 1);
					// Hide what we just did to the parser that handles the "value"
					currentArgument.set(value);
					currentArgument.previous();

					V oldValue = map.get(key);
					if(oldValue != null && !argumentDefinition.isAllowedToRepeat())
						throw forUnhandledRepeatedArgument(propertyIdentifier + key + " was found as a key several times in the input.");

					V parsedValue = valueParser.parse(currentArgument, oldValue, argumentDefinition);
					map.put(parsedKey, parsedValue);
					break;
				}
			}
			return map;
		}

		@Override
		public String descriptionOfValidValues()
		{
			return "key=value where key is " + keyParser.descriptionOfValidValues() + " and value is " + valueParser.descriptionOfValidValues();
		}

		@Override
		public Map<K, V> defaultValue()
		{
			return emptyMap();
		}

		@Override
		String describeValue(final Map<K, V> values)
		{
			List<K> keys = new ArrayList<K>(values.keySet());
			Collections.sort(keys);

			final Iterator<K> keyIterator = keys.iterator();

			return Joiner.on(", ").join(new UnmodifiableIterator<String>(){
				@Override
				public boolean hasNext()
				{
					return keyIterator.hasNext();
				}

				@Override
				public String next()
				{
					K key = keyIterator.next();
					return key + " -> " + values.get(key);
				}
			});
		}
	}

	@VisibleForTesting
	abstract static class RadixiableParser<T extends Number & Comparable<T>> extends OneParameterParser<T>
	{
		private final Radix radix;

		RadixiableParser(final Radix radix)
		{
			this.radix = radix;
		}

		abstract T minValue();

		abstract T maxValue();

		abstract T cast(Long value);

		abstract Long cast(T value);

		/**
		 * @return Number of bits needed to represent <code>T</code>
		 */
		abstract int bitSize();

		@Override
		public T parse(final String value) throws ArgumentException
		{
			try
			{
				Long result = null;
				if(radix.isUnsignedOutput())
				{
					result = UnsignedLongs.parseUnsignedLong(value, radix.radix());
					result = makeSigned(result);
				}
				else
				{
					result = Long.parseLong(value, radix.radix());
				}

				if(result.compareTo(minValue().longValue()) < 0 || result.compareTo(maxValue().longValue()) > 0)
					throw forInvalidValue(value, " is not in the range " + descriptionOfValidValues());

				return cast(result);
			}
			catch(NumberFormatException nfe)
			{
				// TODO: specify which argument that failed
				InvalidArgument ex = forInvalidValue(value, " is not a valid number.");
				ex.initCause(nfe);
				throw ex;
			}
		}

		private long makeSigned(long value)
		{
			long signMask = signBitMask();
			if(isSigned(value, signMask))
			{
				long valueBits = signMask - 1;
				// Inverse all bits except the sign bit
				return signMask | (valueBits ^ ~value);
			}
			return value;
		}

		/**
		 * @return one bit where the sign for the type <code>T</code> is
		 */
		private long signBitMask()
		{
			return (long) 1 << (bitSize() - 1);
		}

		private boolean isSigned(long value, long signMask)
		{
			return (value & signMask) > 0;
		}

		@Override
		public String descriptionOfValidValues()
		{
			String maxValue;
			String minValue;
			if(radix.isUnsignedOutput())
			{
				// TODO: what if minValue() is something else than
				// Byte.MIN_VALUE
				// etc?
				maxValue = describeValue(cast(-1L));
				minValue = describeValue(cast(0L));
			}
			else
			{
				maxValue = describeValue(maxValue());
				minValue = describeValue(minValue());
			}
			minValue = Strings.padStart(minValue, maxValue.length(), '0');

			return minValue + " to " + maxValue + " (" + radix.description() + ")";
		}

		@Override
		public String describeValue(T value)
		{
			// TODO: use strategy pattern instead
			switch(radix)
			{
				case BINARY:
					return toBinaryString(value);
				default:

					// TODO: Move this check into subclass or something
					if(value.getClass() == Long.class)
					{
						String result = null;
						if(radix.isUnsignedOutput())
						{
							result = UnsignedLongs.toString(cast(value), radix.radix()).toUpperCase(Locale.getDefault());
						}
						else
						{
							result = Long.toString(cast(value), radix.radix());
						}
						return result;
					}
					return String.format("%" + radix.formattingIdentifier(), value);
			}
		}

		private String toBinaryString(T tValue)
		{
			long value = cast(tValue);

			final int size = bitSize();
			char[] binaryString = new char[size];
			for(int bitPosition = 0; bitPosition < size; bitPosition++)
			{
				boolean bitIsSet = (value & (1L << bitPosition)) != 0L;
				int index = size - 1 - bitPosition;
				binaryString[index] = bitIsSet ? '1' : '0';
			}
			return new String(binaryString);
		}
	}

	/**
	 * Contains constants for the most common radix values used in Computer Science.
	 * Used with {@link StringParsers#integerParser(Radix)} for example.
	 */
	public enum Radix
	{
		/**
		 * Parses ones & zeros into a {@link Number}, treats values as unsigned when
		 * parsing/printing them. Radix: 2
		 */
		BINARY(2, "", UnsignedOutput.YES),
		/**
		 * Parses octals into {@link Number}s, treats the octals as unsigned when parsing/printing
		 * them. Radix: 8
		 */
		OCTAL(8, "o", UnsignedOutput.YES),
		/**
		 * The default radix, 10. Handles signed values as well.
		 * Just like {@link Long#parseLong(String)} does.
		 */
		DECIMAL(10, "d", UnsignedOutput.NO),
		/**
		 * Parses hex numbers, such as 0xFF into {@link Number}s, treats the hex numbers as unsigned
		 * when parsing/printing them. Radix: 16
		 */
		HEX(16, "X", UnsignedOutput.YES);

		private int radix;
		private String formattingIdentifier;

		private UnsignedOutput unsignedOutput;

		Radix(int radix, String formattingIdentifier, UnsignedOutput unsignedOutput)
		{
			this.radix = radix;
			this.formattingIdentifier = formattingIdentifier;
			this.unsignedOutput = unsignedOutput;
		}

		int radix()
		{
			return radix;
		}

		String formattingIdentifier()
		{
			return formattingIdentifier;
		}

		String description()
		{
			return toString().toLowerCase(Locale.getDefault());
		}

		boolean isUnsignedOutput()
		{
			return unsignedOutput == UnsignedOutput.YES;
		}

		/**
		 * Tells whether or not this radix'es output is affected by
		 * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4215269">
		 * Bug ID: 4215269 Some Integer.toHexString(int) results cannot be
		 * decoded back to an int
		 * </a>
		 */
		@VisibleForTesting
		enum UnsignedOutput
		{
			YES,
			NO;
		}
	}
}
