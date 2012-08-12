package se.j4j.argumentparser;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;

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
	public static <T> Describer<T> withConstantString(final String constant)
	{
		return new ConstantStringDescriber<T>(constant);
	}

	/**
	 * Describes {@link Character}s by printing explanations for unprintable characters.
	 */
	public static Describer<Character> characterDescriber()
	{
		return CharDescriber.INSTANCE;
	}

	/**
	 * Describes {@link File}s with {@link File#getAbsolutePath()} instead of {@link File#getPath()}
	 * as {@link File#toString()} does.
	 */
	public static Describer<File> fileDescriber()
	{
		return FileDescriber.INSTANCE;
	}

	/**
	 * Describes a boolean as enabled when true and disabled when false
	 */
	public static Describer<Boolean> booleanAsEnabledDisabled()
	{
		return EnabledDescriber.INSTANCE;
	}

	/**
	 * Describes a boolean as on when true and off when false
	 */
	public static Describer<Boolean> booleanAsOnOff()
	{
		return OnOffDescriber.INSTANCE;
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
	public static <T> Function<T, String> asFunction(final Describer<T> describer)
	{
		return new Function<T, String>(){
			@Override
			public String apply(T input)
			{
				return describer.describe(input);
			}
		};
	}

	static <T> Describer<List<T>> forListValues(Describer<T> valueDescriber)
	{
		return new ListDescriber<T>(valueDescriber);
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

	private static final class FileDescriber implements Describer<File>
	{
		private static final Describer<File> INSTANCE = new FileDescriber();

		@Override
		public String describe(File file)
		{
			return file.getAbsolutePath();
		}
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

	private static final class EnabledDescriber implements Describer<Boolean>
	{
		public static final Describer<Boolean> INSTANCE = new EnabledDescriber();

		@Override
		public String describe(Boolean value)
		{
			return value ? "enabled" : "disabled";
		}
	}

	private static final class OnOffDescriber implements Describer<Boolean>
	{
		public static final Describer<Boolean> INSTANCE = new OnOffDescriber();

		@Override
		public String describe(Boolean value)
		{
			return value ? "on" : "off";
		}
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

			StringBuilder sb = new StringBuilder(value.size() * 10);
			Iterator<T> values = value.iterator();
			sb.append('[').append(valueDescriber.describe(values.next()));
			while(values.hasNext())
			{
				sb.append(", ").append(valueDescriber.describe(values.next()));
			}
			sb.append(']');
			return sb.toString();
		}
	}
}
