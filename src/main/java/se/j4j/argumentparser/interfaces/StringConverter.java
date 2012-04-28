package se.j4j.argumentparser.interfaces;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import se.j4j.argumentparser.exceptions.ArgumentException;

public interface StringConverter<T>
{
	T convert(String argument) throws ArgumentException;

	@Nonnull
	public String descriptionOfValidValues();

	/**
	 * If you can provide a sample value do so, it will look much better
	 * in the usage texts, if not return null
	 * 
	 * @return
	 */
	@Nullable
	public T defaultValue();
}
