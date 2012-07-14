package se.j4j.argumentparser.internal;

import java.util.List;

public final class ListUtil
{
	private ListUtil()
	{
	}

	public static String describeList(List<?> list)
	{
		if(list.isEmpty())
			return "Empty list";
		return list.toString();
	}
}
