package se.j4j.argumentparser;

import static com.google.common.base.Strings.repeat;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.math.BigInteger.ZERO;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static se.j4j.argumentparser.ArgumentExceptions.asUnchecked;
import static se.j4j.argumentparser.ArgumentExceptions.forMissingNthParameter;
import static se.j4j.argumentparser.ArgumentExceptions.forMissingParameter;
import static se.j4j.argumentparser.ArgumentExceptions.withMessage;
import static se.j4j.argumentparser.ArgumentExceptions.wrapException;
import static se.j4j.strings.Descriptions.format;
import static se.j4j.strings.StringsUtil.NEWLINE;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.ArgumentExceptions.MissingParameterException;
import se.j4j.argumentparser.CommandLineParser.ArgumentIterator;
import se.j4j.argumentparser.internal.Texts.UserErrors;
import se.j4j.numbers.NumberType;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * <pre>
 * Gives you static access to implementations of the {@link StringParser} interface.
 * All methods return {@link Immutable} parsers.
 * Most methods here even return the same instance for every call.
 * If you want to customize one of these parsers you can use a {@link ForwardingStringParser}.
 * </pre>
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
		return StringStringParser.STRING;
	}

	/**
	 * @return a parser that makes the strings it parses into lower case using the default locale.
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<String> lowerCaseParser()
	{
		return StringStringParser.LOWER_CASE;
	}

	@VisibleForTesting
	enum StringStringParser implements StringParser<String>
	{
		/**
		 * Simple returns the strings it's given to parse
		 */
		STRING
		{
			@Override
			public String parse(String value) throws ArgumentException
			{
				return value;
			}
		},
		/**
		 * Makes the strings it parses into lower case with the {@link Locale#getDefault()} locale.
		 * TODO: make the Locale settable
		 */
		LOWER_CASE
		{
			@Override
			public String parse(final String value)
			{
				return value.toLowerCase(Locale.getDefault());
			}
		};

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
			return "<string>";
		}
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
			return "<boolean>";
		}
	}

	/**
	 * Returns parser that creates {@link File}s using {@link File#File(String)} for input strings.<br>
	 * {@link StringParser#defaultValue()} returns a {@link File} representing the current working
	 * directory.
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<File> fileParser()
	{
		return FileParser.INSTANCE;
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
			return new File(".");
		}

		@Override
		public String metaDescription()
		{
			return "<path>";
		}
	}

	/**
	 * @return a parser that returns the first character in a {@link String} as a {@link Character}
	 *         and that throws {@link ArgumentException} for any given {@link String} with more than
	 *         one {@link Character}
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Character> charParser()
	{
		return CharParser.INSTANCE;
	}

	private static final class CharParser implements StringParser<Character>
	{
		private static final CharParser INSTANCE = new CharParser();

		@Override
		public Character parse(final String value) throws ArgumentException
		{
			if(value.length() != 1)
				throw withMessage(format(UserErrors.INVALID_CHAR, value));
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
			return "<character>";
		}
	}

	/**
	 * <pre>
	 * Returns a {@link StringParser} that uses {@link Enum#valueOf(Class, String)} to
	 * {@link StringParser#parse(String)} input strings.
	 * 
	 * <b>Case sensitivity note:</b> First a direct match with the input value in upper case is
	 * made, if that fails a direct match without converting the case is made,
	 * if that also fails an {@link ArgumentException} is thrown. This order of execution is
	 * based on the fact that users typically don't upper case their input while
	 * java naming guidelines recommends upper case for enum constants.
	 * TODO: insert link to java naming guidelines
	 * </pre>
	 * 
	 * @param enumToHandle the {@link Class} literal for the {@link Enum} to parse strings into
	 * @return a {@link StringParser} that parse strings into enum values of the type {@code T}
	 */
	@CheckReturnValue
	@Nonnull
	public static <T extends Enum<T>> StringParser<T> enumParser(final Class<T> enumToHandle)
	{
		return new EnumParser<T>(enumToHandle);
	}

	private static final class EnumParser<E extends Enum<E>> implements StringParser<E>
	{
		private final Class<E> enumType;

		private EnumParser(final Class<E> enumToHandle)
		{
			enumType = enumToHandle;
		}

		@Override
		public E parse(final String value) throws ArgumentException
		{
			try
			{
				return Enum.valueOf(enumType, value.toUpperCase(Locale.ENGLISH));
			}
			catch(IllegalArgumentException noEnumFoundWithUpperCase)
			{
				try
				{
					// Try an exact match just in case an enum value doesn't follow java naming
					// guidelines
					return Enum.valueOf(enumType, value);
				}
				catch(IllegalArgumentException noEnumFoundWithExactMatch)
				{
					throw withMessage(format(UserErrors.INVALID_ENUM_VALUE, value, new Object(){
						@Override
						public String toString()
						{
							// Lazily call this as it's going over all enum values and converting
							// them
							// to strings
							return descriptionOfValidValues();
						}
					})).andCause(noEnumFoundWithExactMatch);
				}
			}
		}

		@Override
		public String descriptionOfValidValues()
		{
			E[] enumValues = enumType.getEnumConstants();
			StringBuilder values = new StringBuilder(enumValues.length * AVERAGE_ENUM_NAME_LENGTH);
			values.append('[');
			Joiner.on(" | ").appendTo(values, enumValues);
			values.append(']');
			return values.toString();
		}

		private static final int AVERAGE_ENUM_NAME_LENGTH = 10;

		@Override
		public E defaultValue()
		{
			return null;
		}

		@Override
		public String metaDescription()
		{
			return "<" + enumType.getSimpleName() + ">";
		}
	}

	/**
	 * Parses {@link String}s into {@link Integer}s.
	 * Automatically tries to identify the radix using leading 0x for hex numbers,
	 * a leading zero (0) for octal numbers and defaults back to decimal if no such identifier is
	 * given.
	 * 
	 * @see Integer#decode(String)
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Integer> integerParser()
	{
		return NumberParser.INTEGER;
	}

	/**
	 * Parses {@link String}s into {@link Byte}s.
	 * Automatically tries to identify the radix using leading 0x for hex numbers,
	 * a leading zero (0) for octal numbers and defaults back to decimal if no such identifier is
	 * given.
	 * 
	 * @see Byte#decode(String)
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Byte> byteParser()
	{
		return NumberParser.BYTE;
	}

	/**
	 * Parses {@link String}s into {@link Short}s.
	 * Automatically tries to identify the radix using leading 0x for hex numbers,
	 * a leading zero (0) for octal numbers and defaults back to decimal if no such identifier is
	 * given.
	 * 
	 * @see Short#decode(String)
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Short> shortParser()
	{
		return NumberParser.SHORT;
	}

	/**
	 * Parses {@link String}s into {@link Long}s.
	 * Automatically tries to identify the radix using leading 0x for hex numbers,
	 * a leading zero (0) for octal numbers and defaults back to decimal if no such identifier is
	 * given.
	 * 
	 * @see Long#decode(String)
	 */
	@CheckReturnValue
	@Nonnull
	public static StringParser<Long> longParser()
	{
		return NumberParser.LONG;
	}

	private static final class NumberParser<T extends Number> implements StringParser<T>
	{
		private static final StringParser<Byte> BYTE = new NumberParser<Byte>(NumberType.BYTE);
		private static final StringParser<Short> SHORT = new NumberParser<Short>(NumberType.SHORT);
		private static final StringParser<Integer> INTEGER = new NumberParser<Integer>(NumberType.INTEGER);
		private static final StringParser<Long> LONG = new NumberParser<Long>(NumberType.LONG);

		private final NumberType<T> type;

		private NumberParser(NumberType<T> type)
		{
			this.type = type;
		}

		@Override
		public T parse(String argument) throws ArgumentException
		{
			try
			{
				return type.decode(argument);
			}
			catch(IllegalArgumentException invalidNumber)
			{
				throw wrapException(invalidNumber);
			}
		}

		@Override
		public String descriptionOfValidValues()
		{
			return type.minValue() + " to " + type.maxValue();
		}

		@Override
		public T defaultValue()
		{
			return type.defaultValue();
		}

		@Override
		public String metaDescription()
		{
			return '<' + type.name() + '>';
		}

		@Override
		public String toString()
		{
			return descriptionOfValidValues();
		}
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
			catch(NumberFormatException nfe)
			{
				throw withMessage(format(UserErrors.INVALID_DOUBLE, value)).andCause(nfe);
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
			return "<double>";
		}
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
			catch(NumberFormatException nfe)
			{
				throw withMessage(format(UserErrors.INVALID_FLOAT, value)).andCause(nfe);
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
			return "<float>";
		}
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
			catch(NumberFormatException nfe)
			{
				throw withMessage(format(UserErrors.INVALID_BIG_INTEGER, value)).andCause(nfe);
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
			return "<big-integer>";
		}
	}

	/**
	 * <pre>
	 * Makes it possible to use the given {@code parser} as a Guava {@link Function}.
	 * For example:
	 * </pre>
	 * 
	 * <pre class="prettyprint">
	 * <code class="language-java">
	 * List&lt;Integer&gt; result =  Lists.transform(asList("1", "3", "2"), asFunction(integerParser()));
	 * </code>
	 * </pre>
	 * 
	 * <b>Note:</b>This method may be removed in the future if Guava is removed as a dependency.
	 * 
	 * @param parser the parser to expose as a {@link Function}
	 * @return {@code parser} exposed in a {@link Function} that throws
	 *         {@link IllegalArgumentException} when given faulty values.
	 */
	@Nonnull
	@CheckReturnValue
	@Beta
	public static <T> Function<String, T> asFunction(final StringParser<T> parser)
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
					throw asUnchecked(e);
				}
			}
		};
	}

	/**
	 * <pre>
	 * Makes it possible to convert several (or zero) {@link String}s into a single {@code T} value.
	 * For a simpler one use {@link StringParser}.
	 * 
	 * {@link ArgumentSettings} is passed to the functions that produces text for the usage,
	 * it can't be a member of this class because one parser can be referenced
	 * from multiple different {@link ArgumentSettings argumentSettings} so this is extrinsic state.
	 * 
	 * @param <T> the type this parser parses strings into
	 * </pre>
	 */
	// @Immutable
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
		abstract T parse(final ArgumentIterator arguments, @Nullable final T previousOccurance, final ArgumentSettings argumentSettings)
				throws ArgumentException;

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
		 * otherwise return {@code null}
		 */
		@Nullable
		abstract T defaultValue();

		/**
		 * @param argumentSettings can, for instance, be used to print
		 *            {@link ArgumentSettings#separator()}s
		 * @return {@code value} described, or null if no description is
		 *         needed
		 */
		@Nullable
		String describeValue(@Nullable T value, ArgumentSettings argumentSettings)
		{
			return String.valueOf(value);
		}

		@Nonnull
		abstract String metaDescription(ArgumentSettings argumentSettings);

		String metaDescriptionInLeftColumn(ArgumentSettings argumentSettings)
		{
			return metaDescription(argumentSettings);
		}

		String metaDescriptionInRightColumn(ArgumentSettings argumentSettings)
		{
			return metaDescription(argumentSettings);
		}
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
	 * Inherits from InternalStringParser because implementing StringParser
	 * would make it require a parameter which an Option doesn't.
	 */
	static final class OptionParser extends InternalStringParser<Boolean>
	{
		private static final OptionParser DEFAULT_FALSE = new OptionParser(false);
		private static final OptionParser DEFAULT_TRUE = new OptionParser(true);

		/**
		 * Specifies a {@link ArgumentSettings#parameterArity()} of zero
		 */
		static final int NO_ARGUMENTS = 0;

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
			// Options don't have parameters so there's no parameter to explain
			return "";
		}
	}

	/**
	 * Base class for {@link StringParser}s that uses a sub parser to parse element values and puts
	 * them into a {@link List}
	 */
	private abstract static class ListParser<T> extends InternalStringParser<List<T>>
	{
		private final InternalStringParser<T> parser;

		private ListParser(final InternalStringParser<T> parser)
		{
			this.parser = parser;
		}

		protected final InternalStringParser<T> elementParser()
		{
			return parser;
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
			return value.isEmpty() ? "Empty list" : value.toString();
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
				try
				{
					T parsedValue = elementParser().parse(arguments, null, argumentSettings);
					parsedArguments.add(parsedValue);
				}
				catch(MissingParameterException exception)
				{
					// Wrap exception to more clearly specify which parameter that is missing
					throw forMissingNthParameter(exception.parameterDescription(), i).andCause(exception);
				}
			}
			return parsedArguments;
		}

		@Override
		public List<T> defaultValue()
		{
			T defaultValue = elementParser().defaultValue();
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
			String metaDescriptionForValue = metaDescription(argumentSettings);
			return metaDescriptionForValue + repeat(" " + metaDescriptionForValue, arity - 1);
		}
	}

	/**
	 * Implements {@link ArgumentBuilder#variableArity()}
	 */
	static final class VariableArityParser<T> extends ListParser<T>
	{
		/**
		 * A marker for a variable amount of parameters
		 */
		static final int VARIABLE_ARITY = -1;

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
				T parsedValue = elementParser().parse(arguments, null, argumentSettings);
				parsedArguments.add(parsedValue);
			}
			return parsedArguments;
		}

		@Override
		String metaDescriptionInLeftColumn(ArgumentSettings argumentSettings)
		{
			String metaDescriptionForValue = metaDescription(argumentSettings);
			return metaDescriptionForValue + " ...";
		}
	}

	/**
	 * Implements {@link ArgumentBuilder#splitWith(String)}.
	 * 
	 * @param <T> the type that's separated by the {@code valueSeparator}
	 */
	static final class StringSplitterParser<T> extends ListParser<T>
	{
		@Nonnull private final String valueSeparator;
		@Nonnull private final Splitter splitter;

		StringSplitterParser(final String valueSeparator, final InternalStringParser<T> parser)
		{
			super(parser);
			this.valueSeparator = valueSeparator;
			this.splitter = Splitter.on(valueSeparator).trimResults();
		}

		@Override
		List<T> parse(final ArgumentIterator arguments, final List<T> oldValue, final ArgumentSettings argumentSettings) throws ArgumentException
		{
			if(!arguments.hasNext())
				throw forMissingParameter(argumentSettings);

			String values = arguments.next();
			List<T> result = new ArrayList<T>();

			for(String value : splitter.split(values))
			{
				ArgumentIterator argument = ArgumentIterator.forSingleArgument(value);
				T parsedValue = elementParser().parse(argument, null, argumentSettings);
				result.add(parsedValue);
			}
			return result;
		}

		@Override
		String metaDescriptionInLeftColumn(ArgumentSettings argumentSettings)
		{
			String metaDescriptionForValue = metaDescription(argumentSettings);
			return metaDescriptionForValue + valueSeparator + metaDescriptionForValue + valueSeparator + "...";
		}
	}

	/**
	 * Implements {@link ArgumentBuilder#repeated()}.
	 * 
	 * @param <T> type of the repeated values (such as {@link Integer} for {@link IntegerParser}
	 */
	static final class RepeatedArgumentParser<T> extends ListParser<T>
	{
		RepeatedArgumentParser(final InternalStringParser<T> parser)
		{
			super(parser);
		}

		@Override
		List<T> parse(final ArgumentIterator arguments, List<T> previouslyCreatedList, final ArgumentSettings argumentSettings)
				throws ArgumentException
		{
			T parsedValue = elementParser().parse(arguments, null, argumentSettings);

			List<T> listToStoreRepeatedValuesIn = previouslyCreatedList;
			if(listToStoreRepeatedValuesIn == null)
			{
				listToStoreRepeatedValuesIn = Lists.newArrayList();
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
	static final class KeyValueParser<K, V> extends InternalStringParser<Map<K, V>>
	{
		@Nonnull private final InternalStringParser<V> valueParser;
		@Nonnull private final StringParser<K> keyParser;
		@Nonnull private final Predicate<V> valueLimiter;

		KeyValueParser(StringParser<K> keyParser, InternalStringParser<V> valueParser, Predicate<V> valueLimiter)
		{
			this.valueParser = valueParser;
			this.keyParser = keyParser;
			this.valueLimiter = valueLimiter;
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
			String key = getKey(keyValue, argumentSettings);
			K parsedKey = keyParser.parse(key);
			V oldValue = map.get(parsedKey);

			// TODO: what if null is an actual value then non-allowed repetitions won't be
			// detected
			if(oldValue != null && !argumentSettings.isAllowedToRepeat())
				throw withMessage(format(UserErrors.UNALLOWED_REPETITION_OF_KEY, argumentSettings, key));

			// Hide what we just did to the parser that handles the "value"
			arguments.setNextArgumentTo(getValue(key, keyValue, argumentSettings));
			V parsedValue = valueParser.parse(arguments, oldValue, argumentSettings);
			try
			{
				if(!valueLimiter.apply(parsedValue))
					throw withMessage(format(UserErrors.UNALLOWED_VALUE, parsedValue, valueLimiter));
			}
			catch(IllegalArgumentException e)
			{
				throw wrapException(e).originatedFrom(argumentSettings);
			}

			map.put(parsedKey, parsedValue);
			return map;
		}

		/**
		 * Fetch "key" from "key=value"
		 */
		private String getKey(String keyValue, ArgumentSettings argumentSettings) throws ArgumentException
		{
			String separator = argumentSettings.separator();
			int keyEndIndex = keyValue.indexOf(separator);
			if(keyEndIndex == -1)
				throw withMessage(format(UserErrors.MISSING_KEY_VALUE_SEPARATOR, argumentSettings, keyValue, separator));

			return keyValue.substring(0, keyEndIndex);
		}

		/**
		 * Fetch "value" from "key=value"
		 */
		private String getValue(String key, String keyValue, ArgumentSettings argumentSettings)
		{
			return keyValue.substring(key.length() + argumentSettings.separator().length());
		}

		@Override
		public String descriptionOfValidValues(ArgumentSettings argumentSettings)
		{
			String keyMeta = '"' + keyParser.metaDescription() + '"';
			String valueMeta = '"' + valueParser.metaDescription(argumentSettings) + '"';
			String keyDescription = keyParser.descriptionOfValidValues();
			String valueDescription;
			if(valueLimiter != Predicates.alwaysTrue())
			{
				valueDescription = valueLimiter.toString();
			}
			else
			{
				valueDescription = valueParser.descriptionOfValidValues(argumentSettings);
			}

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
				return "Empty map";

			return Joiner.on(NEWLINE).withKeyValueSeparator(argumentSettings.separator()).join(values);
		}

		@Override
		String metaDescription(ArgumentSettings argumentSettings)
		{
			String keyMeta = keyParser.metaDescription();
			String separator = argumentSettings.separator();
			String valueMeta = valueParser.metaDescription(argumentSettings);
			return keyMeta + separator + valueMeta;
		}
	}

	static final class StringParserBridge<T> extends InternalStringParser<T> implements StringParser<T>
	{
		private final StringParser<T> stringParser;

		/**
		 * A bridge between the {@link StringParser} & {@link InternalStringParser} interfaces.
		 * 
		 * @param parserToBridge the {@link StringParser} to expose as a
		 *            {@link InternalStringParser}
		 * @param <T> the type the bridged {@link StringParser} handles
		 */
		StringParserBridge(StringParser<T> parserToBridge)
		{
			stringParser = parserToBridge;
		}

		@Override
		T parse(ArgumentIterator arguments, T previousOccurance, ArgumentSettings argumentSettings) throws ArgumentException
		{
			if(!arguments.hasNext())
				throw forMissingParameter(argumentSettings);
			return parse(arguments.next());
		}

		@Override
		String descriptionOfValidValues(ArgumentSettings argumentSettings)
		{
			return descriptionOfValidValues();
		}

		@Override
		String metaDescription(ArgumentSettings argumentSettings)
		{
			return metaDescription();
		}

		@Override
		public T parse(String argument) throws ArgumentException
		{
			return stringParser.parse(argument);
		}

		@Override
		public String descriptionOfValidValues()
		{
			return stringParser.descriptionOfValidValues();
		}

		@Override
		public String metaDescription()
		{
			return stringParser.metaDescription();
		}

		@Override
		public T defaultValue()
		{
			return stringParser.defaultValue();
		}

		@Override
		public String toString()
		{
			return descriptionOfValidValues();
		}
	}
}
