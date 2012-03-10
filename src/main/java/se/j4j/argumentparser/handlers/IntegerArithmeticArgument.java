package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import javax.annotation.Nonnull;

import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.interfaces.ArgumentHandler;

public class IntegerArithmeticArgument implements ArgumentHandler<Integer>
{
	//TODO: implement calculator support for MyProgram 2+(5*4)
	private final char operation;

	public IntegerArithmeticArgument(final char operation)
	{
		this.operation = operation;
	}

	public final Integer parse(final @Nonnull ListIterator<String> currentArgument, final Integer oldValue, final @Nonnull Argument<?> argumentDefinition) throws ArgumentException
	{
		//TODO: how to validate
		Integer result = Integer.valueOf(currentArgument.next());

		while(currentArgument.hasNext())
		{
			switch(operation)
			{
				case '+':
					result += Integer.valueOf(currentArgument.next());
					break;
				case '-':
					result -= Integer.valueOf(currentArgument.next());
					break;
				case '*':
					result *= Integer.valueOf(currentArgument.next());
					break;
				case '/':
					result /= Integer.valueOf(currentArgument.next());
					break;
			}
		}

		return result;
	}

	public String descriptionOfValidValues()
	{
		return "A series of integers to use " + operation + " on.";
	}
}
