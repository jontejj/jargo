package se.j4j.classes;

public class NoMainAvailable
{
	public static void main(String[] args)
	{
		final Thread mainThread = Thread.currentThread();
		Thread thread = new Thread(){
			@Override
			public void run()
			{
				while(mainThread.isAlive())
				{
					;
				}
				try
				{
					Classes.mainClassName();
					System.err.print("Requesting name of mainClass after main thread has died should trigger an IllegalStateException");
				}
				catch(IllegalStateException expected)
				{
					System.err.print(expected.getMessage());
				}
			}
		};
		thread.start();
	}
}
