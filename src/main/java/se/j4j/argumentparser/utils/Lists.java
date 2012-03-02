package se.j4j.argumentparser.utils;

import java.util.ArrayList;
import java.util.List;

public final class Lists
{
	private Lists(){}

	public static <E> List<E> of(final E oneElement)
	{
		List<E> list = new ArrayList<E>();
		list.add(oneElement);
		return list;
	}
}
