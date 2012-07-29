package se.j4j.argumentparser.security;

import java.io.FilePermission;
import java.net.NetPermission;
import java.security.Permission;
import java.util.PropertyPermission;
import java.util.Set;

import se.j4j.argumentparser.concurrency.ConcurrencyTest;

import com.google.common.collect.Sets;

/**
 * Tests that argument parser works with an extremely restrictive {@link SecurityManager} installed.
 */
public class SecurityManagerDependencyTest
{
	public static void main(String[] args)
	{
		// TODO: introduce this as a security test
		System.setSecurityManager(new SecurityManager(){
			@Override
			public void checkPermission(Permission perm)
			{
				if(perm instanceof FilePermission)
				{
					// To load the java class
					if(perm.getActions().equals("read"))
						return;
				}
				else if(perm instanceof NetPermission)
				{
					// To load the java class
					if(perm.getName().equals("specifyStreamHandler"))
						return;
				}
				else if(perm instanceof RuntimePermission)
				{
					// To shutdown the executor
					if(perm.getName().equals("modifyThread"))
						return;
				}
				else if(perm instanceof PropertyPermission)
				{
					if(READABLE_PROPERTIES.contains(perm.getName()) && perm.getActions().equals("read"))
						return;
				}
				throw new SecurityException("Permission: " + perm + " not granted");
			}
		});
		ConcurrencyTest test = new ConcurrencyTest();
		test.test();
	}

	static final Set<String> READABLE_PROPERTIES = Sets.newHashSet(	"user.timezone", "user.country", "java.home",
																	"org.joda.time.DateTimeZone.Provider", "org.joda.time.DateTimeZone.NameProvider",
																	"sun.timezone.ids.oldmapping", "os.name");
}
