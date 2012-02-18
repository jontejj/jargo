package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentHandler;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;

public class IntegerArithmeticArgument implements ArgumentHandler<Integer>
{
	private char operation = '+';

	public IntegerArithmeticArgument operation(final char operation)
	{
		this.operation = operation;
		return this;
	}

	public Integer parse(final ListIterator<String> currentArgument, final Argument<?> argumentDefinition) throws ArgumentException
	{
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
}
