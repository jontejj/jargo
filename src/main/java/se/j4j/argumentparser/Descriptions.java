package se.j4j.argumentparser;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

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
	public static Description withString(@Nonnull String description)
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
	 * Creates an {@link IllegalArgumentException} where the {@link Description#description()} of
	 * {@code message} is used as the detail message.
	 */
	public static IllegalArgumentException illegalArgument(Description message)
	{
		return new DescriptionException(message);
	}

	static SerializableDescription asSerializable(Description description)
	{
		return new SerializableDescription(description);
	}

	/**
	 * Returns an empty string as a description.
	 */
	public static final Description EMPTY_STRING = withString("");

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

		@Override
		public String toString()
		{
			return description();
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

		@Override
		public String toString()
		{
			return description();
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

		@Override
		public String toString()
		{
			return description();
		}
	}

	static final class SerializableDescription implements Serializable, Description
	{
		private final transient Description description;

		private SerializableDescription(Description descriptionToSerialize)
		{
			description = descriptionToSerialize;
		}

		private static final class SerializationProxy implements Serializable
		{
			/**
			 * The detail message for this description. Constructed lazily when serialized.
			 * 
			 * @serial
			 */
			private final String message;

			private static final long serialVersionUID = 1L;

			private SerializationProxy(Description descriptionToSerialize)
			{
				message = descriptionToSerialize.description();
			}

			private Object readResolve()
			{
				return new SerializableDescription(Descriptions.withString(message));
			}
		}

		Object writeReplace()
		{
			return new SerializationProxy(this);
		}

		/**
		 * @param stream a stream that (wrongly so) tries to construct a SerializableDescription
		 *            directly instead of going through the SerializationProxy
		 */
		private void readObject(ObjectInputStream stream) throws InvalidObjectException
		{
			throw new InvalidObjectException("Proxy required");
		}

		@Override
		public String description()
		{
			return description.description();
		}

		@Override
		public String toString()
		{
			return description();
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	}

	private static final class DescriptionException extends IllegalArgumentException
	{
		private final SerializableDescription message;

		private DescriptionException(final Description message)
		{
			this.message = Descriptions.asSerializable(message);
		}

		@Override
		public String getMessage()
		{
			return message.description();
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	}
}
