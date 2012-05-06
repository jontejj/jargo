package se.j4j.argumentparser;

import javax.annotation.Nonnull;

import com.google.common.base.Splitter;

public final class StringSplitters
{
	private StringSplitters()
	{
	}

	/**
	 * Splits input by a comma and trims away whitespace
	 */
	public static StringSplitter comma()
	{
		return Comma.INSTANCE;
	}

	private static class Comma implements StringSplitter
	{
		private static final StringSplitter INSTANCE = new Comma();

		private static final Splitter COMMA = Splitter.on(',').trimResults();

		@Override
		@Nonnull
		public Iterable<String> split(final CharSequence input)
		{
			return COMMA.split(input);
		}

		@Override
		public String description()
		{
			return "a comma";
		}
	}
}
