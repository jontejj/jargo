package se.j4j.argumentparser;

import static com.google.common.base.Strings.repeat;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.math.BigInteger.ZERO;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static se.j4j.argumentparser.ArgumentExceptions.forInvalidValue;
import static se.j4j.argumentparser.ArgumentExceptions.forMissingParameter;
import static se.j4j.argumentparser.ArgumentExceptions.forUnhandledRepeatedArgument;
import static se.j4j.argumentparser.ArgumentExceptions.withDescription;
import static se.j4j.argumentparser.StringParsers.RadixiableParser.radixiableParser;
import static se.j4j.argumentparser.internal.Platform.NEWLINE;
import static se.j4j.argumentparser.internal.StringsUtil.surroundWithMarkers;
import static se.j4j.argumentparser.internal.StringsUtil.toLowerCase;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.ArgumentExceptions.InvalidArgument;
import se.j4j.argumentparser.CommandLineParser.ArgumentIterator;
import se.j4j.argumentparser.ForwardingStringParser.SimpleForwardingStringParser;
import se.j4j.argumentparser.internal.NumberType;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.primitives.UnsignedLongs;

/**
 * Gives you static access to implementations of the {@link StringParser} interface.
 * All methods return {@link Immutable} parsers.
 * Most methods here returns the same instance for every call.
 * If you want to customize one of these parsers you can use a {@link ForwardingStringParser}
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
		return StringStringParser.INSTANCE;
	}

	/**
	 * @return a parser that makes the strings it parses into lower case using the default locale.
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<String> lowerCaseParser()
	{
		return LowerCaseStringParser.INSTANCE;
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
		return radixiableParser(radix, NumberType.INTEGER);
	}

	/**
	 * @return a parser that uses {@link Radix#DECIMAL} to parse {@link String}s into
	 *         {@link Integer}s
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Integer> integerParser()
	{
		return radixiableParser(Radix.DECIMAL, NumberType.INTEGER);
	}

	/**
	 * @return a parser that uses <code>radix</code> to parse {@link String}s into {@link Byte}s
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Byte> byteParser(Radix radix)
	{
		return radixiableParser(radix, NumberType.BYTE);
	}

	/**
	 * @return a parser that uses <code>radix</code> to parse {@link String}s into {@link Short}s
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Short> shortParser(Radix radix)
	{
		return radixiableParser(radix, NumberType.SHORT);
	}

	/**
	 * @return a parser that uses <code>radix</code> to parse {@link String}s into {@link Long}s
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Long> longParser(Radix radix)
	{
		return radixiableParser(radix, NumberType.LONG);
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
	 * Makes it possible to use the given <code>parser</code> as a Guava {@link Function}.
	 * For example:<code>
	 * List&lt;Integer&gt; result =  Lists.transform(Arrays.asList("1", "3", "2"), StringParsers.asFunction(StringParsers.integerParser()));
	 * </code>
	 * 
	 * <b>Note:</b>This method may be removed in the future if Guava is removed as a dependency.
	 * 
	 * @param parser the parser to expose as a {@link Function}
	 * @return <code>parser</code> exposed in a {@link Function} that
	 * throws {@link IllegalArgumentException} when given faulty values.
	 * </pre>
	 */
	@Nonnull
	@CheckReturnValue
	@Beta
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
	static StringParser<Boolean> optionParser(final boolean defaultValue)
	{
		if(defaultValue)
			return OptionParser.DEFAULT_TRUE;
		return OptionParser.DEFAULT_FALSE;
	}

	/**
	 * Simple returns the strings it's given to parse
	 */
	private static final class StringStringParser implements StringParser<String>
	{
		private static final StringParser<String> INSTANCE = new StringStringParser();

		@Override
		public String parse(final String value)
		{
			return value;
		}

		@Override
		public String descriptionOfValidValues()
		{
			return "any string";
		}

		@Override
		public String defaultValue()
		{
			return "";
		}

		@Override
		public String metaDescription()
		{
			return "string";
		}
	}

	/**
	 * Returns a Boolean with a value represented by the next string in the arguments.
	 * The Boolean returned represents a true value if the string argument is
	 * equal, ignoring case, to the string "true".
	 */
	private static final class BooleanParser implements StringParser<Boolean>
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

		@Override
		public String metaDescription()
		{
			return "boolean";
		}
	}

	private static final class FileParser implements StringParser<File>
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
		public String metaDescription()
		{
			return "path";
		}
	}

	private static final class CharParser implements StringParser<Character>
	{
		private static final CharParser INSTANCE = new CharParser();

		@Override
		public Character parse(final String value) throws ArgumentException
		{
			if(value.length() != 1)
				throw forInvalidValue(value, "is not a valid character");

			return value.charAt(0);
		}

		@Override
		public String descriptionOfValidValues()
		{
			return "any unicode character";
		}

		@Override
		public Character defaultValue()
		{
			return 0;
		}

		@Override
		public String metaDescription()
		{
			return "character";
		}
	}

	private static final class EnumParser<E extends Enum<E>> implements StringParser<E>
	{
		private final Class<E> enumType;

		private EnumParser(final Class<E> enumToHandle)
		{
			enumType = enumToHandle;
			// TODO: As this check causes the enum to load, consider making it possible to opt out
			// of it
			if(enumType.getEnumConstants().length == 0)
				throw new IllegalArgumentException(enumType.getSimpleName() + " has no possible values defined");
		}

		@Override
		public E parse(final String value) throws ArgumentException
		{
			try
			{
				// TODO: add possibility to convert strings to
				// upper case before finding the enum value
				return Enum.valueOf(enumType, value);
			}
			catch(IllegalArgumentException noEnumFound)
			{
				throw forInvalidValue(value, "is not a valid Option, Expecting one of " + descriptionOfValidValues());
			}
		}

		@Override
		public String descriptionOfValidValues()
		{
			E[] enumValues = enumType.getEnumConstants();
			StringBuilder values = new StringBuilder(enumValues.length * 10);
			values.append('[');
			Joiner.on(" | ").appendTo(values, enumValues);
			values.append(']');
			return values.toString();
		}

		@Override
		public E defaultValue()
		{
			return null;
		}

		@Override
		public String metaDescription()
		{
			return enumType.getSimpleName();
		}
	}

	/**
	 * Inherits from InternalStringParser because implementing StringParser
	 * would make it require a parameter which an Option doesn't.
	 */
	private static final class OptionParser extends SimpleForwardingStringParser<Boolean>
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
		Boolean parse(ArgumentIterator arguments, Boolean previousOccurance, ArgumentSettings argumentSettings) throws ArgumentException
		{
			return enabledValue;
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
		public String descriptionOfValidValues(ArgumentSettings argumentSettings)
		{
			return "";
		}

		@Override
		String metaDescription(ArgumentSettings argumentSettings)
		{
			return "";
		}
	}

	private static final class BigIntegerParser implements StringParser<BigInteger>
	{
		private static final StringParser<BigInteger> INSTANCE = new BigIntegerParser();

		@Override
		public String descriptionOfValidValues()
		{
			return "any integer";
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
				InvalidArgument e = forInvalidValue(value, "is not a valid big-integer");
				e.initCause(cause);
				throw e;
			}
		}

		@Override
		public BigInteger defaultValue()
		{
			return ZERO;
		}

		@Override
		public String metaDescription()
		{
			return "big-integer";
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
				InvalidArgument e = forInvalidValue(value, "is not a valid double (64-bit IEEE 754 floating point)");
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

		@Override
		public String metaDescription()
		{
			return "double";
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
				InvalidArgument e = forInvalidValue(value, "is not a valid float (32-bit IEEE 754 floating point)");
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

		@Override
		public String metaDescription()
		{
			return "float";
		}
	}

	/**
	 * <pre>
	 * Makes it possible to convert several (or zero) {@link String}s into a single <code>T</code> value.
	 * For a simpler one use {@link StringParser}.
	 * 
	 * {@link ArgumentSettings} is passed to the functions that produces text for the usage,
	 * it can't be a member of this class because one parser can be referenced
	 * from multiple different {@link ArgumentSettings argumentSettings} so this is extrinsic state.
	 * 
	 * @param <T> the type this parser parses strings into
	 * </pre>
	 */
	@Immutable
	abstract static class InternalStringParser<T>
	{
		/**
		 * @param arguments the arguments given from the command line where
		 *            {@link ArgumentIterator#next()} points to the parameter
		 *            for a named argument, for an indexed argument it points to the single unnamed
		 *            argument
		 * @param previousOccurance the previously parsed value for this
		 *            argument if it appears several times, otherwise null
		 * @param argumentSettings argument settings for this parser,
		 *            can be used for providing good exception messages.
		 * @return the parsed value
		 * @throws ArgumentException if an error occurred while parsing the value
		 */
		abstract T parse(@Nonnull final ArgumentIterator arguments, @Nullable final T previousOccurance,
				@Nonnull final ArgumentSettings argumentSettings) throws ArgumentException;

		/**
		 * Describes the values this parser accepts
		 * 
		 * @return a description string to show in usage texts
		 */
		@Nonnull
		abstract String descriptionOfValidValues(ArgumentSettings argumentSettings);

		/**
		 * If you can provide a suitable value do so, it will look much better
		 * in the usage texts and providing sane defaults makes your program/code easier to use,
		 * otherwise return <code>null</code>
		 */
		@Nullable
		public abstract T defaultValue();

		/**
		 * @return <code>value</code> described, or null if no description is
		 *         needed
		 */
		@Nullable
		abstract String describeValue(@Nullable T value, ArgumentSettings argumentSettings);

		@Nonnull
		abstract String metaDescription(ArgumentSettings argumentSettings);

		String metaDescriptionInLeftColumn(ArgumentSettings argumentSettings)
		{
			return surroundWithMarkers(metaDescription(argumentSettings));
		}

		String metaDescriptionInRightColumn(ArgumentSettings argumentSettings)
		{
			return surroundWithMarkers(metaDescription(argumentSettings));
		}
	}

	/**
	 * Implements {@link ArgumentBuilder#splitWith(String)}.
	 * 
	 * @param <T> the type that's separated by the <code>valueSeparator</code>
	 */
	static final class StringSplitterParser<T> extends ListParser<T>
	{
		@Nonnull private final String valueSeparator;
		@Nonnull private final Splitter splitter;

		StringSplitterParser(@Nonnull final String valueSeparator, @Nonnull final InternalStringParser<T> parser)
		{
			super(parser);
			this.valueSeparator = valueSeparator;
			this.splitter = Splitter.on(valueSeparator).trimResults();
		}

		@Override
		List<T> parse(final ArgumentIterator arguments, final List<T> oldValue, final ArgumentSettings argumentSettings) throws ArgumentException
		{
			if(!arguments.hasNext())
				throw forMissingParameter(argumentSettings, arguments.getCurrentArgumentName());

			String values = arguments.next();
			List<T> result = new ArrayList<T>();

			for(String value : splitter.split(values))
			{
				ArgumentIterator argument = ArgumentIterator.forSingleArgument(value);
				T parsedValue = parser.parse(argument, null, argumentSettings);
				result.add(parsedValue);
			}
			return result;
		}

		@Override
		String metaDescriptionInLeftColumn(ArgumentSettings argumentSettings)
		{
			String metaDescriptionForValue = surroundWithMarkers(metaDescription(argumentSettings));
			return metaDescriptionForValue + valueSeparator + metaDescriptionForValue + valueSeparator + "...";
		}
	}

	/**
	 * Base class for {@link StringParser}s that uses a sub parser to parse element values
	 */
	private static abstract class ListParser<T> extends InternalStringParser<List<T>>
	{
		protected final InternalStringParser<T> parser;

		private ListParser(final InternalStringParser<T> parser)
		{
			this.parser = parser;
		}

		@Override
		public String descriptionOfValidValues(ArgumentSettings argumentSettings)
		{
			return parser.descriptionOfValidValues(argumentSettings);
		}

		@Override
		String metaDescription(ArgumentSettings argumentSettings)
		{
			return parser.metaDescription(argumentSettings);
		}

		@Override
		public List<T> defaultValue()
		{
			return emptyList();
		}

		@Override
		String describeValue(List<T> value, ArgumentSettings argumentSettings)
		{
			if(value.isEmpty())
				return "Empty list";

			StringBuilder sb = new StringBuilder(value.size() * 10);
			Iterator<T> values = value.iterator();
			sb.append('[').append(parser.describeValue(values.next(), argumentSettings));
			while(values.hasNext())
			{
				sb.append(", ").append(parser.describeValue(values.next(), argumentSettings));
			}
			sb.append(']');
			return sb.toString();
		}
	}

	/**
	 * Implements {@link ArgumentBuilder#arity(int)}
	 */
	static final class FixedArityParser<T> extends ListParser<T>
	{
		private final int arity;

		FixedArityParser(final InternalStringParser<T> parser, final int arity)
		{
			super(parser);
			this.arity = arity;
		}

		@Override
		List<T> parse(final ArgumentIterator arguments, final List<T> list, final ArgumentSettings argumentSettings) throws ArgumentException
		{
			List<T> parsedArguments = newArrayListWithCapacity(arity);
			for(int i = 0; i < arity; i++)
			{
				// TODO: should this wrap the exception when there's missing arguments?
				T parsedValue = parser.parse(arguments, null, argumentSettings);
				parsedArguments.add(parsedValue);
			}
			return parsedArguments;
		}

		@Override
		public List<T> defaultValue()
		{
			T defaultValue = parser.defaultValue();
			List<T> listFilledWithDefaultValues = newArrayListWithCapacity(arity);
			for(int i = 0; i < arity; i++)
			{
				listFilledWithDefaultValues.add(defaultValue);
			}
			return listFilledWithDefaultValues;
		}

		@Override
		String metaDescriptionInLeftColumn(ArgumentSettings argumentSettings)
		{
			String metaDescriptionForValue = surroundWithMarkers(metaDescription(argumentSettings));
			return metaDescriptionForValue + repeat(" " + metaDescriptionForValue, arity - 1);
		}
	}

	/**
	 * Implements {@link ArgumentBuilder#variableArity()}
	 */
	static final class VariableArityParser<T> extends ListParser<T>
	{
		VariableArityParser(final InternalStringParser<T> parser)
		{
			super(parser);
		}

		@Override
		List<T> parse(final ArgumentIterator arguments, final List<T> list, final ArgumentSettings argumentSettings) throws ArgumentException
		{
			List<T> parsedArguments = newArrayListWithCapacity(arguments.nrOfRemainingArguments());
			while(arguments.hasNext())
			{
				T parsedValue = parser.parse(arguments, null, argumentSettings);
				parsedArguments.add(parsedValue);
			}
			return parsedArguments;
		}

		@Override
		String metaDescriptionInLeftColumn(ArgumentSettings argumentSettings)
		{
			String metaDescriptionForValue = surroundWithMarkers(metaDescription(argumentSettings));
			return metaDescriptionForValue + " ...";
		}
	}

	/**
	 * Implements {@link ArgumentBuilder#repeated()}.
	 * 
	 * @param <T> type of the repeated values (such as {@link Integer} for {@link IntegerParser}
	 */
	static final class RepeatedArgumentParser<T> extends ListParser<T>
	{
		RepeatedArgumentParser(@Nonnull final InternalStringParser<T> parser)
		{
			super(parser);
		}

		@Override
		List<T> parse(final ArgumentIterator arguments, List<T> previouslyCreatedList, final ArgumentSettings argumentSettings)
				throws ArgumentException
		{
			T parsedValue = parser.parse(arguments, null, argumentSettings);

			List<T> listToStoreRepeatedValuesIn = previouslyCreatedList;
			if(listToStoreRepeatedValuesIn == null)
			{
				listToStoreRepeatedValuesIn = new ArrayList<T>();
			}

			listToStoreRepeatedValuesIn.add(parsedValue);
			return listToStoreRepeatedValuesIn;
		}
	}

	/**
	 * Implements {@link ArgumentBuilder#asPropertyMap()} &
	 * {@link ArgumentBuilder#asKeyValuesWithKeyParser(StringParser)}.
	 * 
	 * @param <K> the type of key in the resulting map
	 * @param <V> the type of values in the resulting map
	 */
	@VisibleForTesting
	static final class KeyValueParser<K extends Comparable<K>, V> extends InternalStringParser<Map<K, V>>
	{
		@Nonnull private final InternalStringParser<V> valueParser;
		@Nonnull private final InternalStringParser<K> keyParser;

		KeyValueParser(@Nonnull final InternalStringParser<V> valueParser, @Nonnull final InternalStringParser<K> keyParser)
		{
			this.valueParser = valueParser;
			this.keyParser = keyParser;
		}

		@Override
		Map<K, V> parse(final ArgumentIterator arguments, Map<K, V> previousMap, final ArgumentSettings argumentSettings) throws ArgumentException
		{
			Map<K, V> map = previousMap;
			if(map == null)
			{
				map = Maps.newLinkedHashMap();
			}

			String keyValue = arguments.next();
			String separator = argumentSettings.separator();

			List<String> propertyIdentifiers = argumentSettings.names();
			String argument = keyValue;
			if(argumentSettings.isIgnoringCase())
			{
				propertyIdentifiers = toLowerCase(propertyIdentifiers);
				// TODO: set Locale.ENGLISH on lowerCaseParser() as well
				// TODO: document the usage of Locale.ENGLISH
				argument = argument.toLowerCase(Locale.ENGLISH);
			}

			for(String propertyIdentifier : propertyIdentifiers)
			{
				if(argument.startsWith(propertyIdentifier))
				{
					// Fetch key and value from "-Dkey=value"
					int keyStartIndex = propertyIdentifier.length();
					int keyEndIndex = keyValue.indexOf(separator, keyStartIndex);
					if(keyEndIndex == -1)
						throw forInvalidValue(keyValue, "Missing assignment operator(" + separator + ")");

					String key = keyValue.substring(keyStartIndex, keyEndIndex);
					ArgumentIterator keyArgument = ArgumentIterator.forSingleArgument(key);
					K parsedKey = keyParser.parse(keyArgument, null, argumentSettings);

					// Remove "-Dkey=" from "-Dkey=value"
					String value = keyValue.substring(keyEndIndex + separator.length());
					// Hide what we just did to the parser that handles the "value"
					arguments.setNextArgumentTo(value);

					V oldValue = map.get(key);
					// TODO: what if null is an actual value then unallowed repitions won't be
					// detected
					if(oldValue != null && !argumentSettings.isAllowedToRepeat())
						// TODO: the last occurrence wasn't necessarily propertyIdentifier it could
						// be any propertyIdentifiers
						throw forUnhandledRepeatedArgument(propertyIdentifier + key + " was found as a key several times in the input.");

					V parsedValue = valueParser.parse(arguments, oldValue, argumentSettings);
					map.put(parsedKey, parsedValue);
					break;
				}
			}
			return map;
		}

		@Override
		public String descriptionOfValidValues(ArgumentSettings argumentSettings)
		{
			String keyMeta = '"' + keyParser.metaDescription(argumentSettings) + '"';
			String valueMeta = '"' + valueParser.metaDescription(argumentSettings) + '"';
			String keyDescription = keyParser.descriptionOfValidValues(argumentSettings);
			String valueDescription = valueParser.descriptionOfValidValues(argumentSettings);

			return "where " + keyMeta + " is " + keyDescription + " and " + valueMeta + " is " + valueDescription;
		}

		@Override
		public Map<K, V> defaultValue()
		{
			return emptyMap();
		}

		@Override
		String describeValue(final Map<K, V> values, final ArgumentSettings argumentSettings)
		{
			if(values.isEmpty())
				return "empty map";

			List<K> keys = new ArrayList<K>(values.keySet());
			Collections.sort(keys);

			final Iterator<K> keyIterator = keys.iterator();

			return Joiner.on(NEWLINE).join(new UnmodifiableIterator<String>(){
				@Override
				public boolean hasNext()
				{
					return keyIterator.hasNext();
				}

				@Override
				public String next()
				{
					K key = keyIterator.next();
					String describedKey = keyParser.describeValue(key, argumentSettings);
					String separator = argumentSettings.separator();
					String describedValue = valueParser.describeValue(values.get(key), argumentSettings);
					return describedKey + separator + describedValue;
				}
			});
		}

		@Override
		String metaDescription(ArgumentSettings argumentSettings)
		{
			String keyMeta = keyParser.metaDescription(argumentSettings);
			String separator = argumentSettings.separator();
			String valueMeta = valueParser.metaDescription(argumentSettings);
			return keyMeta + separator + valueMeta;
		}
	}

	/**
	 * Inherits from ForwardingStringParser because it needs to print default values
	 * ({@link RadixiableParser#describeValue(Number, ArgumentSettings)}) in the
	 * configured radix.
	 * 
	 * @param <T> the type of {@link Number} to parse
	 */
	@VisibleForTesting
	static final class RadixiableParser<T extends Number & Comparable<T>> extends SimpleForwardingStringParser<T>
	{
		private final Radix radix;
		private final NumberType<T> type;
		private final Range<T> validRange;

		private RadixiableParser(final Radix radix, final NumberType<T> type, Range<T> validRange)
		{
			this.radix = radix;
			this.type = type;
			this.validRange = validRange;
		}

		static <T extends Number & Comparable<T>> RadixiableParser<T> radixiableParser(final Radix radix, final NumberType<T> type)
		{
			return new RadixiableParser<T>(radix, type, type.asRange());
		}

		static <T extends Number & Comparable<T>> RadixiableParser<T> radixiableParser(final Radix radix, final NumberType<T> type,
				Range<T> validRange)
		{
			return new RadixiableParser<T>(radix, type, validRange);
		}

		@Override
		public T parse(final String value) throws ArgumentException
		{
			try
			{
				Long result = null;
				if(radix.isUnsignedOutput())
				{
					// TODO: make Radix.HEX handle leading 0x
					result = UnsignedLongs.parseUnsignedLong(value, radix.radix());
					result = makeSigned(result);
				}
				else
				{
					result = Long.parseLong(value, radix.radix());
				}

				if(notInRange(result))
					throw forInvalidValue(value, "is not in the range " + descriptionOfValidValues());

				return type.cast(result);
			}
			catch(NumberFormatException nfe)
			{
				// TODO: specify which argument that failed
				InvalidArgument ex = forInvalidValue(value, "is not a valid number.");
				ex.initCause(nfe);
				throw ex;
			}
		}

		private boolean notInRange(Long result)
		{
			long minValue = validRange.lowerEndpoint().longValue();
			long maxValue = validRange.upperEndpoint().longValue();

			return result.compareTo(minValue) < 0 || result.compareTo(maxValue) > 0;
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
			return (long) 1 << (type.bitSize() - 1);
		}

		private boolean isSigned(long value, long signMask)
		{
			return (value & signMask) > 0;
		}

		@Override
		public String descriptionOfValidValues()
		{
			T maxValue = validRange.upperEndpoint();
			T minValue = validRange.lowerEndpoint();
			String maximumValue;
			String minimumValue;
			if(radix.isUnsignedOutput())
			{
				T maxValueToDisplay = (maxValue.equals(type.maxValue())) ? type.cast(-1L) : maxValue;
				T minValueToDisplay = (minValue.equals(type.minValue())) ? type.cast(0L) : minValue;

				maximumValue = describeValue(maxValueToDisplay);
				minimumValue = describeValue(minValueToDisplay);

				minimumValue = Strings.padStart(minimumValue, maximumValue.length(), '0');
			}
			else
			{
				maximumValue = describeValue(maxValue);
				minimumValue = describeValue(minValue);
			}
			return minimumValue + " to " + maximumValue + " (" + radix.description() + ")";
		}

		String describeValue(T value)
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
							result = UnsignedLongs.toString(type.cast(value), radix.radix()).toUpperCase(Locale.ENGLISH);
						}
						else
						{
							// TODO: is it better to use java.text.NumberFormat?
							result = Long.toString(type.cast(value), radix.radix());
						}
						return result;
					}
					// TODO: Make Locale.ENGLISH settable
					return String.format(Locale.ENGLISH, "%" + radix.formattingIdentifier(), value);
			}
		}

		@Override
		String describeValue(T value, ArgumentSettings argumentSettings)
		{
			return describeValue(value);
		}

		private String toBinaryString(T tValue)
		{
			long value = type.cast(tValue);

			final int size = type.bitSize();
			char[] binaryString = new char[size];
			for(int bitPosition = 0; bitPosition < size; bitPosition++)
			{
				boolean bitIsSet = (value & (1L << bitPosition)) != 0L;
				int index = size - 1 - bitPosition;
				binaryString[index] = bitIsSet ? '1' : '0';
			}
			return new String(binaryString);
		}

		@Override
		public String metaDescription()
		{
			return type.name();
		}

		@Override
		public T defaultValue()
		{
			return type.defaultValue();
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
			return toString().toLowerCase(Locale.ENGLISH);
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

	/**
	 * Makes the strings it parses into lower case with the {@link Locale#getDefault()} locale.
	 * TODO: make the Locale settable
	 */
	private static final class LowerCaseStringParser implements StringParser<String>
	{
		private static final StringParser<String> INSTANCE = new LowerCaseStringParser();

		@Override
		public String parse(final String value)
		{
			return value.toLowerCase(Locale.getDefault());
		}

		@Override
		public String descriptionOfValidValues()
		{
			return "any string";
		}

		@Override
		public String defaultValue()
		{
			return "";
		}

		@Override
		public String metaDescription()
		{
			return StringStringParser.INSTANCE.metaDescription();
		}
	}

	static final class StringParserBridge<T> extends SimpleForwardingStringParser<T>
	{
		/**
		 * A bridge between the {@link StringParser} & {@link InternalStringParser} interfaces.
		 * 
		 * @param <T> the type the bridged {@link StringParser} handles
		 */
		protected StringParserBridge(StringParser<T> parserToBridge)
		{
			super(parserToBridge);
		}
	}
}