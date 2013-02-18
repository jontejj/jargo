package se.j4j.classes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Utilities for working with {@link Class} instances
 */
public final class Classes
{
	private Classes()
	{
	}

	/**
	 * Tries to find the class of the main method by looking at {@link Thread#getAllStackTraces()}.
	 * In simpler terms, a best effort attempt at figuring out the name of the invoked program.
	 * 
	 * @return the simple name of the class containing the "main" method
	 * @throws IllegalStateException when no main method could be found
	 */
	@CheckReturnValue
	@Nonnull
	public static String mainClassName()
	{
		return MAIN_CLASS_NAME.get();
	}

	private static final Supplier<String> MAIN_CLASS_NAME = Suppliers.memoize(new Supplier<String>(){
		@Override
		public String get()
		{
			Iterable<StackTraceElement[]> stacks = Thread.getAllStackTraces().values();
			for(StackTraceElement[] currentStack : stacks)
			{
				if(currentStack.length == 0)
				{
					continue;
				}
				StackTraceElement startMethod = currentStack[currentStack.length - 1];
				if(startMethod.getMethodName().equals("main"))
					return classNameFor(startMethod);
			}
			throw new IllegalStateException("No main method found in the stack traces, could it be that the main thread has been terminated?");
		}
	});

	private static String classNameFor(StackTraceElement element)
	{
		String fullyQualifiedClassName = element.getClassName();
		int classNameStart = fullyQualifiedClassName.lastIndexOf('.') + 1;
		String simpleClassName = fullyQualifiedClassName.substring(classNameStart);
		return simpleClassName;
	}
}
