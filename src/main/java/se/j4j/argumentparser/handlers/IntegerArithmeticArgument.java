package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentHandler;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;

public class IntegerArithmeticArgument implements ArgumentHandler<Integer>
{
	//TODO: implement calculator support for MyProgram 2+(5*4)
	private final char operation;

	public IntegerArithmeticArgument(final char operation)
	{
		this.operation = operation;
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
