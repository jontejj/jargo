package se.j4j.argumentparser.validators;

import java.util.List;

import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.interfaces.ValueValidator;

public class ListValidator<T> implements ValueValidator<List<T>>
{
	private final ValueValidator<T> elementValidator;

	private ListValidator(ValueValidator<T> elementValidator)
	{
		this.elementValidator = elementValidator;
	}

	public static <T> ValueValidator<List<T>> create(ValueValidator<T> elementValidator)
	{
		if(elementValidator == NullValidator.instance())
			return NullValidator.instance();
		return new ListValidator<T>(elementValidator);

	}

	@Override
	public void validate(List<T> values) throws InvalidArgument
	{
		for(T value : values)
		{
			elementValidator.validate(value);
		}
	}
}
