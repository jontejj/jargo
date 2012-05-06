package se.j4j.argumentparser.callbacks;

import java.util.List;

import se.j4j.argumentparser.Callback;

import com.google.common.collect.Lists;

public class PrintStringsWhenParsed implements Callback<String>
{
	List<String> printQueue = Lists.newArrayList();

	@Override
	public void parsedValue(String parsedValue)
	{
		printQueue.add(parsedValue);
	}

}
