package se.j4j.argumentparser.parsedvaluecallbacks;

import java.util.List;

import se.j4j.argumentparser.interfaces.ParsedValueCallback;

import com.google.common.collect.Lists;

public class PrintStringsWhenParsed implements ParsedValueCallback<String>
{
	List<String> printQueue = Lists.newArrayList();

	@Override
	public void parsedValue(String parsedValue)
	{
		printQueue.add(parsedValue);
	}

}
