package se.j4j.testlib;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.concurrent.ThreadSafe;

/**
 * <pre>
 * Utilities for testing different {@link Locale}s.
 * </pre>
 */
@ThreadSafe
public final class Locales
{
	/**
	 * The {@link Locale} representing Sweden
	 */
	public static final Locale SWEDISH = new Locale("sv", "SE");

	/**
	 * See <a href="http://java.sys-con.com/node/46241">Turkish Java Needs Special Brewing</a> and
	 * <a href="http://www.codinghorror.com/blog/2008/03/whats-wrong-with-turkey.html">What's Wrong
	 * With Turkey?</a> for more details about why the Turkish {@link Locale} is suitable for
	 * testing purposes.
	 */
	public static final Locale TURKISH = new Locale("tr", "TR");

	private static final Lock defaultLocaleLock = new ReentrantLock();

	private static volatile Locale defaultLocale = Locale.getDefault();

	private static volatile Thread threadUsingIt = null;

	/**
	 * How long the previous caller said he wanted to use {@link Locale#getDefault()} for
	 */
	private static volatile long maximumUsageTime = SECONDS.toMillis(60);

	/**
	 * <pre>
	 *  Makes it possible to test invalid usages of {@link Locale#getDefault()} in a synchronized
	 *  manner making sure that test cases don't fail suddenly when they are run concurrently.
	 *  I.e a pipelined version of {@link Locale#getDefault()}.
	 * 
	 * You're allowed to use {@link Locale#getDefault()} for 60 seconds then other threads
	 * will start to receive {@link InterruptedException} because they can't get access to it.
	 * If you need more than 60 seconds use {@link #setDefault(Locale, long, TimeUnit)} instead.
	 * <b>Note:</b> Don't forget to call {@link #resetDefaultLocale()} when you're done with using
	 * the {@link Locale#getDefault()} method.
	 * 
	 * @param locale the locale {@link Locale#getDefault()} should return
	 * @throws InterruptedException if it wasn't possible to invoke
	 *             {@link Locale#setDefault(Locale)} due to another thread using the default
	 *             locale for longer than he specified (default 60 seconds)
	 * </pre>
	 */
	public static void setDefault(Locale locale) throws InterruptedException
	{
		setDefault(locale, 60, SECONDS);
	}

	/**
	 * <pre>
	 * Works like {@link #setDefault(Locale)} but it allows you to use {@link Locale#getDefault()}
	 * for the time specified by {@code time} and {@code unit}.
	 * <b>Note:</b> if the current thread already "owns" the default locale (by having called
	 * {@link #setDefault(Locale, long, TimeUnit)} before) {@code time} and {@code unit} will be
	 * ignored.
	 * 
	 * @param locale the locale {@link Locale#getDefault()} should return
	 * @param time the maximum time to use {@link Locale#getDefault()}
	 * @param unit the {@link TimeUnit} of the time argument
	 * @throws InterruptedException if it wasn't possible to invoke
	 *             {@link Locale#setDefault(Locale)} due to another thread using the default
	 *             locale for longer than he specified (default 60 seconds)
	 * </pre>
	 */
	public static void setDefault(Locale locale, long time, TimeUnit unit) throws InterruptedException
	{
		checkNotNull(locale);
		checkNotNull(unit);

		// TODO: test this

		// Allow the same thread to change the locale during his "period" without locking
		if(!Thread.currentThread().equals(threadUsingIt))
		{
			defaultLocaleLock.tryLock(maximumUsageTime, MILLISECONDS);
			threadUsingIt = Thread.currentThread();
			maximumUsageTime = unit.toMillis(time);
			defaultLocale = Locale.getDefault();
		}

		try
		{
			Locale.setDefault(locale);
		}
		catch(SecurityException e)
		{
			defaultLocaleLock.unlock(); // Let someone else try again
			throw e;
		}
	}

	/**
	 * Resets {@link Locale#getDefault()} to what it was when {@link #setDefault(Locale)} was
	 * called. Should be called after you're done with using {@link Locale#getDefault()}, typically
	 * called in a "tear down" method.
	 * 
	 * @throws IllegalStateException if this isn't the same thread that called
	 *             {@link #setDefault(Locale)}
	 */
	public static void resetDefaultLocale()
	{
		if(threadUsingIt == null) // No call to setDefault, no need to reset it
			return;
		checkState(Thread.currentThread().equals(threadUsingIt), Thread.currentThread() + " tried to unlock Locale while " + threadUsingIt
				+ " was using it");
		Locale.setDefault(defaultLocale);
		defaultLocaleLock.unlock();
	}
}
