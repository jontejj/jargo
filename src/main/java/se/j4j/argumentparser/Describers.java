package se.j4j.argumentparser;

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

	static Describer<Argument<?>> argumentDescriber()
	{
		return ArgumentDescriber.INSTANCE;
	}

	/**
	 * Exposes a {@link Describer} as a {@link Function}.
	 * 
	 * @param describer the describer to convert to a {@link Function}
	 * @return a {@link Function} that applies {@link Describer#describe(Object)} to input values.
	 */
	public static <T> Function<T, String> functionFor(final Describer<T> describer)
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
