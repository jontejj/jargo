package se.j4j.argumentparser;

import javax.annotation.Nonnull;

/**
 * Factory for creating/using {@link Description} instances.
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
	 * Returns an empty string as a description.
	 */
	public static final Description EMPTY_STRING = new Description(){
		@Override
		public String description()
		{
			return "";
		}
	};

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
}
