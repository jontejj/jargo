package se.j4j.argumentparser.handlers;

public class ByteArgument extends RadixiableArgument<Byte>
{
	public ByteArgument(final Radix radix)
	{
		super(radix);
	}

	@Override
	public Byte minValue()
	{
		return Byte.MIN_VALUE;
	}

	@Override
	public Byte maxValue()
	{
		return Byte.MAX_VALUE;
	}

	@Override
	protected String toBinaryString(Byte byteValue)
	{
		final int size = Byte.SIZE;
		char[] binaryString = new char[size];

		for(int bitPosition = 0; bitPosition < size; bitPosition++)
		{
			int oneBitMask = 1 << bitPosition;
			int bit = byteValue & oneBitMask;
			boolean bitIsSet = bit == oneBitMask;
			int index = size - 1 - bitPosition;
			binaryString[index] = bitIsSet ? '1' : '0';
		}
		return new String(binaryString);
	}

	@Override
	public Byte defaultValue()
	{
		return 0;
	}

	@Override
	protected long signBitMask()
	{
		return 1 << (Byte.SIZE - 1);
	}

	@Override
	protected Byte cast(Long value)
	{
		return value.byteValue();
	}
}
