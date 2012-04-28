package se.j4j.argumentparser.basichandlers;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.byteArgument;
import static se.j4j.argumentparser.ArgumentParser.forArguments;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.handlers.ByteArgument;
import se.j4j.argumentparser.handlers.RadixiableArgument.Radix;
import se.j4j.argumentparser.internal.Usage;

public class TestByteArguments
{

	@Test
	public void testValidByteArguments() throws ArgumentException
	{
		List<Byte> properInput = Arrays.asList(Byte.MIN_VALUE, (byte) 0, (byte) 50, Byte.MAX_VALUE);

		Argument<Byte> byteArgument = byteArgument("-b").build();
		ArgumentParser parser = forArguments(byteArgument);
		for(Byte input : properInput)
		{
			ParsedArguments parsed = parser.parse("-b", input.toString());
			assertThat(parsed.get(byteArgument)).isEqualTo(input);
		}
	}

	@Test
	public void testBinaryByteString() throws ArgumentException
	{
		@SuppressWarnings("serial")
		// Only used as test assertion
		Map<Radix, String> validInputs = new HashMap<Radix, String>(){
			{
				put(Radix.BINARY, "00000000 to 11111111");
				put(Radix.OCTAL, "000 to 377");
				put(Radix.DECIMAL, "-128 to 127");
				put(Radix.HEX, "00 to FF");
			}
		};

		@SuppressWarnings("serial")
		// Only used as test assertion
		Map<Radix, String> defaultValues = new HashMap<Radix, String>(){
			{
				put(Radix.BINARY, "00100000");
				put(Radix.OCTAL, "40");
				put(Radix.DECIMAL, "32");
				put(Radix.HEX, "20");
			}
		};

		for(Radix radix : Radix.values())
		{
			ByteArgument handler = new ByteArgument(radix);
			for(byte value = Byte.MIN_VALUE + 1;; value++)
			{
				String describedValue = handler.describeValue(value);
				Byte parsedValue = handler.parse(describedValue);
				assertThat(parsedValue).isEqualTo(value);

				if(value == Byte.MAX_VALUE - 1) // Prevent wrap around
				{
					break;
				}
			}
			Argument<Byte> byteArgument = byteArgument("-b").defaultValue((byte) 32).radix(radix).build();
			String usage = Usage.forSingleArgument(byteArgument);
			assertThat(usage).contains(validInputs.get(radix));
			assertThat(usage).contains(defaultValues.get(radix));
		}
	}

	@Test
	public void testInvalidByteArguments() throws ArgumentException
	{
		List<Integer> invalidInput = Arrays.asList(-129, 128);

		Argument<Byte> byteArgument = byteArgument("-b").build();
		ArgumentParser parser = forArguments(byteArgument);
		for(Integer input : invalidInput)
		{
			try
			{
				parser.parse("-b", input.toString());
				fail("Invalid byte input not detected: " + input);
			}
			catch(InvalidArgument e)
			{

			}
		}
	}
}
