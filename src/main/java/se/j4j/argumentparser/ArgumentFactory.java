package se.j4j.argumentparser;

import static java.math.BigInteger.ZERO;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

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
import javax.annotation.concurrent.NotThreadSafe;

import se.j4j.argumentparser.ArgumentBuilder.OptionArgumentBuilder;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.ArgumentExceptionCodes;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.exceptions.UnhandledRepeatedArgument;
import se.j4j.argumentparser.internal.ListUtil;
import se.j4j.argumentparser.internal.StringsUtil;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.primitives.UnsignedLongs;

/**
 * <pre>
 * Used as a starting point to create {@link Argument} instances through
 * different {@link ArgumentBuilder}s.
 * 
 * The produced arguments can either be passed to
 * {@link ArgumentParser#forArguments(Argument...)} to group several
 * {@link Argument}s together (the common use case) or if 
 * only one argument should be parsed {@link ArgumentBuilder#parse(String...)} can be called instead.
 * </pre>
 */
public final class ArgumentFactory
{
	private ArgumentFactory()
	{
	}

	@NotThreadSafe
	public static class DefaultArgumentBuilder<T> extends ArgumentBuilder<DefaultArgumentBuilder<T>, T>
	{
		public DefaultArgumentBuilder(@Nonnull final ArgumentHandler<T> handler)
		{
			super(handler);
		}
	}

	// TODO: verify parsing of invalid values for each argument type (and their
	// string output when given such values)
	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Boolean> booleanArgument(@Nonnull final String ... names)
	{
		return new DefaultArgumentBuilder<Boolean>(new BooleanArgument()).metaDescription("boolean").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Integer> integerArgument(@Nonnull final String ... names)
	{
		return new RadixiableArgumentBuilder<Integer>(){
			@Override
			protected IntegerArgument handler()
			{
				return new IntegerArgument(radix());
			}
		}.metaDescription("integer").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Short> shortArgument(@Nonnull final String ... names)
	{
		return new RadixiableArgumentBuilder<Short>(){
			@Override
			protected ShortArgument handler()
			{
				return new ShortArgument(radix());
			}
		}.metaDescription("short").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Byte> byteArgument(@Nonnull final String ... names)
	{
		return new RadixiableArgumentBuilder<Byte>(){
			@Override
			protected ByteArgument handler()
			{
				return new ByteArgument(radix());
			}
		}.metaDescription("byte").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static RadixiableArgumentBuilder<Long> longArgument(@Nonnull final String ... names)
	{
		return new RadixiableArgumentBuilder<Long>(){
			@Override
			protected LongArgument handler()
			{
				return new LongArgument(radix());
			}
		}.metaDescription("long").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<BigInteger> bigIntegerArgument(@Nonnull final String ... names)
	{
		return new DefaultArgumentBuilder<BigInteger>(new BigIntegerArgument()).names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Character> charArgument(@Nonnull final String ... names)
	{
		return new DefaultArgumentBuilder<Character>(new CharArgument()).metaDescription("character").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Double> doubleArgument(@Nonnull final String ... names)
	{
		return new DefaultArgumentBuilder<Double>(new DoubleArgument()).metaDescription("double").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<Float> floatArgument(@Nonnull final String ... names)
	{
		return new DefaultArgumentBuilder<Float>(new FloatArgument()).metaDescription("float").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<String> stringArgument(@Nonnull final String ... names)
	{
		return new DefaultArgumentBuilder<String>(new StringArgument()).metaDescription("string").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<File> fileArgument(@Nonnull final String ... names)
	{
		return new DefaultArgumentBuilder<File>(new FileArgument()).metaDescription("path").names(names);
	}

	@CheckReturnValue
	@Nonnull
	public static OptionArgumentBuilder optionArgument(@Nonnull final String ... names)
	{
		// TODO: check names length, either enforce it by having the first
		// argument a String and not part of the varargs or
		// checkArgument(names.length > 0)
		return new OptionArgumentBuilder().names(names).defaultValue(false);
	}

	@CheckReturnValue
	@Nonnull
	public static <T extends Enum<T>> DefaultArgumentBuilder<T> enumArgument(@Nonnull final Class<T> enumToHandle)
	{
		return new DefaultArgumentBuilder<T>(new EnumArgument<T>(enumToHandle)).metaDescription("value");
	}

	@CheckReturnValue
	@Nonnull
	public static DefaultArgumentBuilder<String> commandArgument(@Nonnull final CommandArgument commandParser)
	{
		return new DefaultArgumentBuilder<String>(commandParser).names(commandParser.commandName());
	}

	@CheckReturnValue
	@Nonnull
	public static <T> DefaultArgumentBuilder<T> customArgument(@Nonnull final StringConverter<T> converter)
	{
		return new DefaultArgumentBuilder<T>(new StringConverterWrapper<T>(converter));
	}

	static class ArgumentSplitter<T> implements ArgumentHandler<List<T>>
	{
		@Nonnull private final StringSplitter splitter;
		@Nonnull private final ArgumentHandler<T> handler;

		ArgumentSplitter(@Nonnull final StringSplitter splitter, @Nonnull final ArgumentHandler<T> handler)
		{
			this.splitter = splitter;
			this.handler = handler;
		}

		@Override
		public String descriptionOfValidValues()
		{
			return handler.descriptionOfValidValues() + ", separated by " + splitter.description();
		}

		@Override
		public List<T> parse(final ListIterator<String> currentArgument, final List<T> oldValue, final Argument<?> argumentDefinition)
				throws ArgumentException
		{
			if(!currentArgument.hasNext())
				throw ArgumentException.create(ArgumentExceptionCodes.MISSING_PARAMETER);

			String values = currentArgument.next();

			Iterable<String> inputs = splitter.split(values);
			List<T> result = new ArrayList<T>();

			for(String value : inputs)
			{
				T parsedValue = handler.parse(Arrays.asList(value).listIterator(), null, argumentDefinition);
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
		public String describeValue(List<T> value)
		{
			return ListUtil.describeList(value);
		}
	}

	static class ListArgument<T> implements ArgumentHandler<List<T>>
	{
		private final ArgumentHandler<T> argumentHandler;
		private final int argumentsToConsume;

		static final int CONSUME_ALL = -1;

		public ListArgument(final ArgumentHandler<T> argumentHandler, final int argumentsToConsume)
		{
			this.argumentHandler = argumentHandler;
			this.argumentsToConsume = argumentsToConsume;
		}

		@Override
		public List<T> parse(final ListIterator<String> currentArgument, final List<T> list, final Argument<?> argumentDefinition)
				throws ArgumentException
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
			return argumentHandler.parse(currentArgument, null, argumentDefinition);
		}

		@Override
		public String descriptionOfValidValues()
		{
			// TODO: print meta descriptions
			return argumentsToConsume + " of " + argumentHandler.descriptionOfValidValues();
		}

		@Override
		public List<T> defaultValue()
		{
			return emptyList();
		}

		@Override
		public String describeValue(List<T> value)
		{
			return ListUtil.describeList(value);
		}
	}

	/**
	 * Produced by {@link Argument#repeated()} and used by
	 * {@link ArgumentParser#parse(String)}
	 * 
	 * @param <T> type of the repeated values (such as {@link Integer} for
	 *            {@link IntegerArgument}
	 */
	static class RepeatedArgument<T> implements ArgumentHandler<List<T>>
	{
		@Nonnull private final ArgumentHandler<T> argumentHandler;

		RepeatedArgument(@Nonnull final ArgumentHandler<T> argumentHandler)
		{
			this.argumentHandler = argumentHandler;
		}

		@Override
		public List<T> parse(final ListIterator<String> currentArgument, List<T> previouslyCreatedList, final Argument<?> argumentDefinition)
				throws ArgumentException
		{
			T parsedValue = argumentHandler.parse(currentArgument, null, argumentDefinition);

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
			return argumentHandler.descriptionOfValidValues();
		}

		@Override
		public List<T> defaultValue()
		{
			return emptyList();
		}

		@Override
		public String describeValue(List<T> value)
		{
			return ListUtil.describeList(value);
		}
	}

	static class MapArgument<T> implements ArgumentHandler<Map<String, T>>
	{
		static final String DEFAULT_SEPARATOR = "=";

		@Nonnull private final ArgumentHandler<T> handler;

		MapArgument(@Nonnull final ArgumentHandler<T> handler)
		{
			this.handler = handler;
		}

		@Override
		public Map<String, T> parse(final ListIterator<String> currentArgument, Map<String, T> previousMap, final Argument<?> argumentDefinition)
				throws ArgumentException
		{
			Map<String, T> map = previousMap;
			if(map == null)
			{
				// TODO: should this create a LinkedHashMap instead?
				map = Maps.newHashMap();
			}

			String keyValue = currentArgument.next();
			String separator = argumentDefinition.separator();

			List<String> namesToMatch = argumentDefinition.names();
			String argument = keyValue;
			if(argumentDefinition.isIgnoringCase())
			{
				namesToMatch = StringsUtil.toLowerCase(namesToMatch);
				argument = argument.toLowerCase(Locale.getDefault());
			}

			for(String name : namesToMatch)
			{
				if(argument.startsWith(name))
				{
					// Fetch key and value from "-Dkey=value"
					int keyStartIndex = name.length();
					int keyEndIndex = keyValue.indexOf(separator, keyStartIndex);
					if(keyEndIndex == -1)
						throw InvalidArgument.create(keyValue, " Missing assignment operator(" + separator + ")");

					String key = keyValue.substring(keyStartIndex, keyEndIndex);
					// Remove "-Dkey=" from "-Dkey=value"
					String value = keyValue.substring(keyEndIndex + 1);
					// Hide what we just did to the handler that handles the
					// "value"
					currentArgument.set(value);
					currentArgument.previous();

					T oldValue = map.get(key);
					if(oldValue != null && !argumentDefinition.isAllowedToRepeat())
						throw UnhandledRepeatedArgument.create(name + key + " was found as a key several times in the input.");

					T parsedValue = handler.parse(currentArgument, oldValue, argumentDefinition);
					map.put(key, parsedValue);
					break;
				}
			}
			return map;
		}

		@Override
		public String descriptionOfValidValues()
		{
			return "key=value where key is an identifier and value is " + handler.descriptionOfValidValues();
		}

		@Override
		public Map<String, T> defaultValue()
		{
			return emptyMap();
		}

		@Override
		public String describeValue(final Map<String, T> values)
		{
			List<String> keys = new ArrayList<String>(values.keySet());
			Collections.sort(keys);

			final Iterator<String> keyIterator = keys.iterator();

			return Joiner.on(", ").join(new UnmodifiableIterator<String>(){
				@Override
				public boolean hasNext()
				{
					return keyIterator.hasNext();
				}

				@Override
				public String next()
				{
					String key = keyIterator.next();
					return key + " -> " + values.get(key);
				}
			});
		}
	}

	/**
	 * A wrapper for {@link StringConverter}s. I.e it works like a bridge
	 * between
	 * the {@link StringConverter} and {@link ArgumentHandler} interfaces.
	 * 
	 * @param <T>
	 */
	private static class StringConverterWrapper<T> extends OneParameterArgument<T>
	{
		@Nonnull private final StringConverter<T> converter;

		private StringConverterWrapper(@Nonnull final StringConverter<T> converter)
		{
			this.converter = converter;
		}

		@Override
		public String descriptionOfValidValues()
		{
			return converter.descriptionOfValidValues();
		}

		@Override
		public T parse(final String value) throws ArgumentException
		{
			return converter.convert(value);
		}

		@Override
		public T defaultValue()
		{
			return converter.defaultValue();
		}
	}

	/**
	 * Returns a Boolean with a value represented by the next string in the
	 * arguments.
	 * The Boolean returned represents a true value if the string argument is
	 * equal,
	 * ignoring case, to the string "true".
	 */
	private static class BooleanArgument extends OneParameterArgument<Boolean>
	{
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

	private static class CharArgument extends OneParameterArgument<Character>
	{
		@Override
		public Character parse(final String value) throws ArgumentException
		{
			if(value.length() != 1)
				throw InvalidArgument.create(value, " is not a valid character");

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

	private static class EnumArgument<T extends Enum<T>> extends OneParameterArgument<T>
	{
		private final Class<T> enumType;

		public EnumArgument(final Class<T> enumToHandle)
		{
			enumType = enumToHandle;
		}

		@Override
		public T parse(final String value) throws ArgumentException
		{
			try
			{
				return Enum.valueOf(enumType, value);
			}
			catch(IllegalArgumentException noEnumFound)
			{
				List<T> validValues = Arrays.asList(enumType.getEnumConstants());
				throw InvalidArgument.create(value, " is not a valid Option, Expecting one of " + validValues);
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

	private static class FileArgument extends OneParameterArgument<File>
	{
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

	static class OptionArgument extends NoParameterArgument<Boolean>
	{
		private final Boolean defaultValue;

		OptionArgument(final boolean defaultValue)
		{
			this.defaultValue = defaultValue;
		}

		@Override
		public Boolean defaultValue()
		{
			return defaultValue;
		}

		@Override
		public String describeValue(Boolean value)
		{
			return null;
		}

		@Override
		public Boolean get() throws ArgumentException
		{
			return !defaultValue;
		}
	}

	private static class StringArgument extends OneParameterArgument<String>
	{
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

	private static class BigIntegerArgument extends OneParameterArgument<BigInteger>
	{
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
			catch(NumberFormatException e)
			{
				throw InvalidArgument.create(value, " is not a valid BigInteger. " + e.getLocalizedMessage());
			}
		}

		@Override
		public BigInteger defaultValue()
		{
			return ZERO;
		}
	}

	private static class DoubleArgument extends OneParameterArgument<Double>
	{
		@Override
		public Double parse(final String value) throws ArgumentException
		{
			try
			{
				return Double.valueOf(value);
			}
			catch(NumberFormatException ex)
			{
				throw InvalidArgument.create(value, " is not a valid double");
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

	private static class FloatArgument extends OneParameterArgument<Float>
	{
		@Override
		public Float parse(final String value) throws ArgumentException
		{
			try
			{
				return Float.valueOf(value);
			}
			catch(NumberFormatException ex)
			{
				throw InvalidArgument.create(value, " is not a valid float value");
			}
		}

		@Override
		public String descriptionOfValidValues()
		{
			return -Float.MAX_VALUE + " - " + Float.MAX_VALUE;
		}

		@Override
		public Float defaultValue()
		{
			return 0f;
		}
	}

	public static abstract class RadixiableArgumentBuilder<T extends Number> extends ArgumentBuilder<RadixiableArgumentBuilder<T>, T>
	{
		private Radix radix = Radix.DECIMAL;

		RadixiableArgumentBuilder()
		{
			super(null);
		}

		/**
		 * <b>Note:</b> {@link Radix#BINARY}, {@link Radix#OCTAL} &
		 * {@link Radix#HEX} is parsed as unsigned values as
		 * the sign doesn't really make sense when such values are used.
		 * 
		 * @param aRadix the radix to parse/print values with
		 * @return this builder
		 */
		public RadixiableArgumentBuilder<T> radix(final Radix aRadix)
		{
			radix = aRadix;
			return this;
		}

		Radix radix()
		{
			return radix;
		}
	}

	public enum Radix
	{
		BINARY(2, "", UnsignedOutput.YES),
		OCTAL(8, "o", UnsignedOutput.YES),
		DECIMAL(10, "d", UnsignedOutput.NO),
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

		public int radix()
		{
			return radix;
		}

		public String formattingIdentifier()
		{
			return formattingIdentifier;
		}

		public String description()
		{
			return toString().toLowerCase(Locale.getDefault());
		}

		public boolean isUnsignedOutput()
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

	static abstract class RadixiableArgument<T extends Number & Comparable<T>> extends OneParameterArgument<T>
	{
		private final Radix radix;

		RadixiableArgument(final Radix radix)
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
					throw InvalidArgument.create(value, " is not in the range " + descriptionOfValidValues());

				return cast(result);
			}
			catch(NumberFormatException nfe)
			{
				// TODO: specify which argument that failed
				InvalidArgument ex = InvalidArgument.create(value, " is not a valid number.");
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
							result = UnsignedLongs.toString((Long) value, radix.radix()).toUpperCase(Locale.getDefault());
						}
						else
						{
							result = Long.toString((Long) value, radix.radix());
						}
						return result;
					}
					return String.format("%" + radix.formattingIdentifier(), value);
			}
		}

		@Override
		public String toString()
		{
			return "Radix: " + radix + ", Default value:" + defaultValue();
		}

		private String toBinaryString(T tValue)
		{
			long value = cast(tValue);

			final int size = bitSize();
			char[] binaryString = new char[size];

			for(int bitPosition = 0; bitPosition < size; bitPosition++)
			{
				long oneBitMask = 1L << bitPosition;
				long bit = value & oneBitMask;
				boolean bitIsSet = bit == oneBitMask;
				int index = size - 1 - bitPosition;
				binaryString[index] = bitIsSet ? '1' : '0';
			}
			return new String(binaryString);
		}
	}

	@VisibleForTesting
	static class ByteArgument extends RadixiableArgument<Byte>
	{
		ByteArgument(final Radix radix)
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

	@VisibleForTesting
	static class ShortArgument extends RadixiableArgument<Short>
	{
		ShortArgument(final Radix radix)
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

	@VisibleForTesting
	static class IntegerArgument extends RadixiableArgument<Integer>
	{
		IntegerArgument(final Radix radix)
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

	@VisibleForTesting
	static class LongArgument extends RadixiableArgument<Long>
	{
		LongArgument(final Radix radix)
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

	private abstract static class OneParameterArgument<T> implements ArgumentHandler<T>
	{
		public abstract T parse(@Nonnull final String value) throws ArgumentException;

		@Override
		public final T parse(final ListIterator<String> currentArgument, final T oldValue, final Argument<?> argumentDefinition)
				throws ArgumentException
		{
			if(!currentArgument.hasNext())
				throw ArgumentException.create(ArgumentExceptionCodes.MISSING_PARAMETER);
			return parse(currentArgument.next());
		}

		@Override
		public String describeValue(T value)
		{
			return String.valueOf(defaultValue());
		}
	}

	private static abstract class NoParameterArgument<T> implements ArgumentHandler<T>
	{
		public abstract T get() throws ArgumentException;

		@Override
		public final T parse(final ListIterator<String> currentArgument, final T oldValue, final Argument<?> argumentDefinition)
				throws ArgumentException
		{
			return get();
		}

		/**
		 * Only the existence of the flag matters, no specific value.
		 * Use {@link ArgumentBuilder#description} to describe this argument.
		 */
		@Override
		public String descriptionOfValidValues()
		{
			return "";
		}
	}
}
