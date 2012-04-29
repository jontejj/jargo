package se.j4j.argumentparser.security;

import java.security.Permission;

import se.j4j.argumentparser.concurrency.TestArgumentParserConcurrency;

/**
 * Tests that argument parser works with an extremely restrictive
 * {@link SecurityManager} installed.
 */
public class SecurityManagerDependencyTest
{
	public static void main(String[] args)
	{
		// TODO: test this better
		System.setSecurityManager(new SecurityManager(){
			@Override
			public void checkPermission(Permission perm)
			{
				throw new SecurityException("No permissions given at all");
			}
		});
		TestArgumentParserConcurrency test = new TestArgumentParserConcurrency();
		test.test();
	}
}
