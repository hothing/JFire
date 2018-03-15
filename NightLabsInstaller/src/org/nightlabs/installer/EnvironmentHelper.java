package org.nightlabs.installer;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class EnvironmentHelper
{
	private static final String JAR_PREFIX = "jar:"; //$NON-NLS-1$

	public static boolean isWindows()
	{
		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		return osName != null && osName.startsWith("Windows"); //$NON-NLS-1$
	}

	/**
	 * @param classInTopLevel This is a class in the top-level of the jar (i.e. not in a nested jar).
	 */
	public static File getEnclosingJar(Class<?> classInTopLevel)
	{
		URL classURL = classInTopLevel.getResource(classInTopLevel.getSimpleName()+".class"); //$NON-NLS-1$
		//Logger.out.println("class url: "+classURL);
		String fileInJar = classURL.toString();

		if(fileInJar.startsWith(JAR_PREFIX)) {
			int end = fileInJar.indexOf('!', JAR_PREFIX.length());
			if(end != -1) {
				String jarURL = fileInJar.substring(JAR_PREFIX.length(), end);
				//Logger.out.println("class url: "+jarURL);
				File f;
				try {
					URI uri = new URI(jarURL);
					//Logger.out.println("class uri: "+uri.toString());
					try {
						f = new File(uri);
					} catch(IllegalArgumentException e) {
						// this is a windows share file path if: 
						// authority is not null 
						// and we are under windows 
						// and the url starts with "file://" instead of "file:/"
						if(uri.getAuthority() != null && isWindows() && jarURL.startsWith("file://")) { //$NON-NLS-1$
							f = new File("\\\\"+jarURL.substring("file://".length()).replace("/", "\\")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						} else {
							throw e;
						}
					}
					//Logger.out.println("class file: "+f.getAbsolutePath());
				} catch (URISyntaxException e) {
					return null;
				}
				if(f.exists())
					return f;
			}
		}
		return null;
	}


//	/**
//	 * This method reads the jar within we're running from the system properties, key "java.class.path".
//	 */ 
//	public static File getEnclosingJar()
//	{
//		for (Map.Entry<Object, Object> me : System.getProperties().entrySet()) {
//			Logger.out.println(String.valueOf(me.getKey()) + " => " + me.getValue());
//			Logger.out.println("");
//		}
//		return null;
//	}
}
