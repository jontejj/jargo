package se.j4j.argumentparser.internal;

import java.io.File;

import se.j4j.argumentparser.interfaces.DefaultValueProvider;

/**
 * @Formatter:off
 * @author Jonatan Jönsson <jontejj@gmail.com>
 *
 */
public final class DefaultProviders
{
	private DefaultProviders(){}

	public static final DefaultValueProvider<File> FILE = new DefaultValueProvider<File>()
			{ @Override public File defaultValue(){ return new File(""); };};
}
