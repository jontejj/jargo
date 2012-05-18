package se.j4j.argumentparser.providers;

public final class SlowProvider extends ChangingProvider
{
	@Override
	public Integer provideValue()
	{
		try
		{
			Thread.sleep(100);
		}
		catch(InterruptedException e)
		{
			Thread.interrupted();
			throw new IllegalStateException("Was interrupted while sleeping.", e);
		}
		return super.provideValue();
	}
}
