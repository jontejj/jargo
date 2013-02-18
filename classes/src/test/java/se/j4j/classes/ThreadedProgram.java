package se.j4j.classes;

public class ThreadedProgram
{
	public static void main(String[] args) throws InterruptedException
	{
		Thread thread = new Thread(){
			@Override
			public void run()
			{
				System.out.print(Classes.mainClassName());
			}
		};
		thread.start();
		thread.join();

	}
}
