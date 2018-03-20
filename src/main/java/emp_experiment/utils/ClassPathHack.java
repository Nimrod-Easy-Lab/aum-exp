package emp_experiment.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassPathHack {

	private static final Class[] parameters = new Class[] { URL.class };

	// public static void addFile(String s) throws IOException {
	// File f = new File(s);
	// addFile(f);
	// }//end method

	public static void addFile(File f) throws IOException {
		
		ClassLoader prevCl = Thread.currentThread().getContextClassLoader();

		// Create class loader using given codebase
		// Use prevCl as parent to maintain current visibility
		ClassLoader urlCl = URLClassLoader.newInstance(new URL[] { f.toURL() }, prevCl);

		try {
			// Save class loader so that we can restore later
			Thread.currentThread().setContextClassLoader(urlCl);
		} finally {
			// Restore
//			Thread.currentThread().setContextClassLoader(prevCl);
		}
	}

	public static void addURL(URL u) throws IOException {

		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class sysclass = URLClassLoader.class;

		try {
			Method method = sysclass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { u });
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IOException("Error, could not add URL to system classloader");
		} // end try catch

	}// end method

}// end class
