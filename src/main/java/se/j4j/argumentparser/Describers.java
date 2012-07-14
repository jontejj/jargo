package se.j4j.argumentparser;

import java.io.File;

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
	 * Always describes any value of type <code>T</code> with the given <code>description</code>
	 */
	public static <T> Describer<T> withStaticString(final String description)
	{
		return new StaticStringDescriber<T>(description);
	}

	/**
	 * Describes {@link Character}s by printing explanations for unprintable characters.
	 */
	public static Describer<Character> characterDescriber()
	{
		return CharDescriber.INSTANCE;
	}

	/**
	 * Describes {@link File}s by {@link File#getAbsolutePath()} instead of {@link File#getPath()}
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

	static Describer<Argument<?>> argumentDescriber()
	{
		return ArgumentDescriber.INSTANCE;
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

	private static final class StaticStringDescriber<T> implements Describer<T>
	{
		private final String description;

		private StaticStringDescriber(final String description)
		{
			this.description = description;
		}

		@Override
		public String describe(T value)
		{
			return description;
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

	private static final class ArgumentDescriber implements Describer<Argument<?>>
	{
		private static final Describer<Argument<?>> INSTANCE = new ArgumentDescriber();

		@Override
		public String describe(Argument<?> argument)
		{
			// TODO: handle indexed arguments that have no names
			return argument.names().get(0);
		}
	}
}
