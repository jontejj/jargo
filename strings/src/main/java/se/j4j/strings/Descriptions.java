package se.j4j.strings;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import javax.annotation.CheckReturnValue;
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
	 * Returns an empty string as a description.
	 */
	@Nonnull public static final Description EMPTY_STRING = withString("");

	/**
	 * Supplies an already created {@link String} as a {@link Description}.
	 * Also useful for caching {@link Description}s that won't change.
	 */
	@Nonnull
	@CheckReturnValue
	public static Description withString(String description)
	{
		return new NonLazyDescription(description);
	}

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

	/**
	 * Lazily calls {@link String#format(String, Object...)}
	 */
	@Nonnull
	@CheckReturnValue
	public static Description format(String formatTemplate, Object ... args)
	{
		return new FormatDescription(formatTemplate, args);
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

	/**
	 * Lazily calls the {@link #toString()} of {@code value} as a description
	 * 
	 * @param value the object to call {@link #toString()} on
	 */
	@Nonnull
	@CheckReturnValue
	public static Description toString(Object value)
	{
		return new ToStringDescription(value);
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

	/**
	 * Creates an {@link IllegalArgumentException} where the {@link Description#description()} of
	 * {@code message} is used as the detail message.
	 */
	@Nonnull
	@CheckReturnValue
	public static IllegalArgumentException illegalArgument(Description message)
	{
		return new DescriptionException(message);
	}

	/**
	 * Creates an {@link IllegalArgumentException} where the {@link Description#description()} of
	 * {@code message} is used as the detail message. {@code cause} is set as the cause.
	 */
	@Nonnull
	@CheckReturnValue
	public static IllegalArgumentException illegalArgument(Description message, Throwable cause)
	{
		return new DescriptionException(message, cause);
	}

	private static final class DescriptionException extends IllegalArgumentException
	{
		private final SerializableDescription message;

		private DescriptionException(final Description message)
		{
			this.message = Descriptions.asSerializable(message);
		}

		private DescriptionException(final Description message, Throwable cause)
		{
			this.message = Descriptions.asSerializable(message);
			initCause(cause);
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

	/**
	 * Returns a version of {@code description} that is serializable
	 */
	@Nonnull
	@CheckReturnValue
	public static SerializableDescription asSerializable(Description description)
	{
		return new SerializableDescription(description);
	}

	/**
	 * A {@link Serializable} wrapper for {@link Description}s
	 */
	public static final class SerializableDescription implements Serializable, Description
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
			// TODO: read Effective Java, Second Ed., Item 78. and test this
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
}
