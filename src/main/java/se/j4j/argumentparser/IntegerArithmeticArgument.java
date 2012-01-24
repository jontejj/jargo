package se.j4j.argumentparser;

import java.util.ListIterator;

public class IntegerArithmeticArgument extends Argument<Integer>
{
	private char operation = '+';

	IntegerArithmeticArgument(final String ... names)
	{
		super(names);
	}

	public IntegerArithmeticArgument operation(final char operation)
	{
		this.operation = operation;
		return this;
	}

	@Override
	Integer parse(final ListIterator<String> currentArgument) throws ArgumentException
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
