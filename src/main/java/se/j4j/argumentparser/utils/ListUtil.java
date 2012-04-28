package se.j4j.argumentparser.utils;

import java.util.List;
import java.util.ListIterator;

public final class ListUtil
{
	private ListUtil()
	{
	}

	public static <E> ListIterator<E> copy(final ListIterator<E> original)
	{
		List<E> copy = com.google.common.collect.Lists.newArrayList();
		while(original.hasNext())
		{
			copy.add(original.next());
		}
		return copy.listIterator();
	}

	public static String describeList(List<?> list)
	{
		if(list.isEmpty())
			return "Empty list";
		return list.toString();
	}
}
