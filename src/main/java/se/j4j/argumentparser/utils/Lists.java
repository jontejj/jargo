package se.j4j.argumentparser.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public final class Lists
{
	private Lists(){}

	public static <E> List<E> of(final E oneElement)
	{
		List<E> list = new ArrayList<E>();
		list.add(oneElement);
		return list;
	}

	public static <E> ListIterator<E> copy(final ListIterator<E> original)
	{
		List<E> copy = new ArrayList<E>();
		while(original.hasNext())
		{
			copy.add(original.next());
		}
		return copy.listIterator();
	}
}
