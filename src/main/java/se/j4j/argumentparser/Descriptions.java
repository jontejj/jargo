package se.j4j.argumentparser;

import javax.annotation.Nonnull;

/**
 * Gives you static access to implementations of the {@link Description} interface.
 */
public final class Descriptions
{
	private Descriptions()
	{
	}

	/**
	 * Supplies an already created {@link String} as a {@link Description}.
	 * Also useful for caching {@link Description}s that won't change.
	 */
	public static Description forString(@Nonnull String description)
	{
		return new NonLazyDescription(description);
	}

	/**
	 * Lazily calls {@link String#format(String, Object...)}
	 */
	public static Description format(@Nonnull String formatTemplate, Object ... args)
	{
		return new FormatDescription(formatTemplate, args);
	}

	/**
	 * Lazily calls the {@link #toString()} of {@code value} as a description
	 * 
	 * @param value the object to call {@link #toString()} on
	 */
	public static Description toString(@Nonnull Object value)
	{
		return new ToStringDescription(value);
	}

	/**
	 * Returns an empty string as a description.
	 */
	public static final Description EMPTY_STRING = forString("");

	private static final class NonLazyDescription implements Description
	{
		private final String description;

		private NonLazyDescription(String description)
		{
			this.description = description;
		}

		@Override
		public String description()
		{
			return description;
		}
	}

	private static final class ToStringDescription implements Description
	{
		private final Object value;

		private ToStringDescription(Object value)
		{
			this.value = value;
		}

		@Override
		public String description()
		{
			return value.toString();
		}
	}

	private static final class FormatDescription implements Description
	{
		private final String formattingTemplate;
		private final Object[] args;

		private FormatDescription(String formattingTemplate, Object[] args)
		{
			this.formattingTemplate = formattingTemplate;
			this.args = args;
		}

		@Override
		public String description()
		{
			return String.format(formattingTemplate, args);
		}
	}
}
