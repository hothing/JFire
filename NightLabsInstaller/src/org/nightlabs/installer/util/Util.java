package org.nightlabs.installer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nightlabs.installer.InstallationManager;
import org.nightlabs.installer.base.Configurable;
import org.nightlabs.installer.base.InstallationEntity;
import org.nightlabs.installer.base.UI;

/**
 * @version $Revision: 1338 $ - $Date: 2008-07-10 22:36:50 +0200 (Do, 10 Jul 2008) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class Util
{
	public static UI getStandardUI(Class<?> clazz, InstallationEntity installationEntity)
	{
		if (clazz == null)
			throw new IllegalArgumentException("clazz must not be null!");

		if (clazz.getPackage() == null)
			throw new IllegalStateException("clazz.getPackage() returned null! clazz: " + clazz);

		String packageName = clazz.getPackage().getName();
		// try sub-package:
		String subPackageName = "ui."+InstallationManager.getInstallationManager().getUiType().toString();
		String className = clazz.getSimpleName() + "UI";
		String fullClassName = packageName + "." + subPackageName + "." + className;
		try {
			Class<?> uiClass = Class.forName(fullClassName);
			UI ui = (UI) uiClass.getConstructor(new Class[0]).newInstance(new Object[0]);
			ui.setInstallationEntity(installationEntity);
			return ui;
		} catch (Throwable e) {
			Class<?> superclass = clazz.getSuperclass();
			if(superclass == null || superclass == Object.class)
				return null;
			return getStandardUI(superclass, installationEntity);
		}
	}
	
	public static Configurable getConfigurable(Properties config, String keyPrefix) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException
	{
		return getConfigurable(config, keyPrefix, null);
	}

	public static Configurable getConfigurable(Properties config, String keyPrefix, Class<? extends Configurable> defaultEntity) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException
	{
		Properties installerConfig = Util.getProperties(config, keyPrefix);
		String className = installerConfig.getProperty("class");
		Class<?> clazz;
		if(className != null)
			clazz = Class.forName(className);
		else
			clazz = defaultEntity;
		Configurable entity = null;
		if(clazz != null) {
			entity = (Configurable)clazz
					.getConstructor(new Class[0])
					.newInstance(new Object[0]);
			entity.setConfig(installerConfig);
		}
		return entity;
	}
	
	public static Collection<Matcher> getPropertyKeyMatches(Properties properties, Pattern pattern)
	{
		Collection<Matcher> matches = new ArrayList<Matcher>();
		for (Iterator<Object> iter = properties.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			Matcher m = pattern.matcher(key);
			if(m.matches())
				matches.add(m);
		}
		return matches;
	}
	
	public static Properties getProperties(Properties properties, String keyPrefix)
	{
		Properties newProperties = new Properties();
		Collection<Matcher> matches = getPropertyKeyMatches(properties, Pattern.compile("^"+Pattern.quote(keyPrefix)+"(.*)$"));
		for (Matcher m : matches)
			newProperties.put(m.group(1), properties.get(m.group(0)));
		return newProperties;
	}
	
	/**
	 * Return a newly created {@link Properties} instance. All values of the given
	 * <code>values</code> argument will be properties in the newly created
	 * object. The default values of the given <code>values</code> argument will
	 * be discarded. The <code>defaults</code> argument will directly be used as
	 * default argument in the Properties constructor of the newly created
	 * properties.
	 * <p>
	 * All in all, the resulting {@link Properties} object will be a copy of the
	 * given <code>values</code> argument with the default values replaced by
	 * the <code>defaults</code> argument.
	 * 
	 * @param values
	 *          The property list containing the values for the newly created
	 *          property list.
	 * @param defaults
	 *          The property list containing the default values for the newly
	 *          created property list.
	 * @return A new property list containing values and defaults.
	 */
	public static Properties createProperties(Properties values, Properties defaults)
	{
		Properties p = new Properties(defaults); // the underlying implementation handles null defaults
		if(values != null) {
			Enumeration<Object> keys = values.keys();
			while(keys.hasMoreElements()) {
				String key = (String)keys.nextElement();
				p.setProperty(key, values.getProperty(key));
			}
		}
		return p;
	}
	
	/**
	 * Transfer all available data from an {@link InputStream} to an {@link OutputStream}.
	 * <p>
	 * This is a convenience method for <code>transferStreamData(in, out, 0, -1)</code>
	 * @param in The stream to read from
	 * @param out The stream to write to
	 * @return The number of bytes transferred
	 * @throws IOException In case of an error
	 */
	public static long transferStreamData(java.io.InputStream in, java.io.OutputStream out)
	throws java.io.IOException
	{
		return transferStreamData(in, out, 0, -1);
	}

	/**
	 * Transfer data between streams
	 * @param in The input stream
	 * @param out The output stream
	 * @param inputOffset How many bytes to skip before transferring
	 * @param inputLen How many bytes to transfer. -1 = all
	 * @return The number of bytes transferred
	 * @throws IOException if an error occurs.
	 */
	public static long transferStreamData(java.io.InputStream in, java.io.OutputStream out, long inputOffset, long inputLen)
	throws java.io.IOException
	{
		int bytesRead;
		int transferred = 0;
		byte[] buf = new byte[4096];

		//skip offset
		if(inputOffset > 0)
			if(in.skip(inputOffset) != inputOffset)
				throw new IOException("Input skip failed (offset "+inputOffset+")");

		while (true) {
			if(inputLen >= 0)
				bytesRead = in.read(buf, 0, (int)Math.min(buf.length, inputLen-transferred));
			else
				bytesRead = in.read(buf);

			if (bytesRead <= 0)
				break;

			out.write(buf, 0, bytesRead);

			transferred += bytesRead;

			if(inputLen >= 0 && transferred >= inputLen)
				break;
		}
		out.flush();
		return transferred;
	}

	/**
	 * Copy a file.
	 * @param sourceFile The source file to copy
	 * @param destinationFile To which file to copy the source
	 * @throws IOException in case of an error
	 */
	public static void copyFile(File sourceFile, File destinationFile)
	throws IOException
	{
		FileInputStream source = null;
		FileOutputStream destination = null;

		try {
			// First make sure the specified source file
			// exists, is a file, and is readable.
			if (!sourceFile.exists() || !sourceFile.isFile())
				throw new IOException("FileCopy: no such source file: "+sourceFile.getCanonicalPath());
			if (!sourceFile.canRead())
			 throw new IOException("FileCopy: source file is unreadable: "+sourceFile.getCanonicalPath());

			// If the destination exists, make sure it is a writeable file.	If the destination doesn't
			// exist, make sure the directory exists and is writeable.
			if (destinationFile.exists()) {
				if (destinationFile.isFile()) {
					if (!destinationFile.canWrite())
						throw new IOException("FileCopy: destination file is unwriteable: " + destinationFile.getCanonicalPath());
				} else
					throw new IOException("FileCopy: destination is not a file: " +	destinationFile.getCanonicalPath());
			} else {
				File parentdir = destinationFile.getParentFile();
				if (parentdir == null || !parentdir.exists())
					throw new IOException("FileCopy: destination directory doesn't exist: " +
									destinationFile.getCanonicalPath());
				 if (!parentdir.canWrite())
					 throw new IOException("FileCopy: destination directory is unwriteable: " +
									destinationFile.getCanonicalPath());
			}
			// If we've gotten this far, then everything is okay; we can
			// copy the file.
			source = new FileInputStream(sourceFile);
			destination = new FileOutputStream(destinationFile);
			transferStreamData(source, destination);
			// No matter what happens, always close any streams we've opened.
		} finally {
			if (source != null)
				try { source.close(); } catch (IOException e) { ; }
			if (destination != null)
				try { destination.close(); } catch (IOException e) { ; }
		}
	}

	/**
	 * Copy a directory recursively.
	 * @param sourceDirectory The source directory
	 * @param destinationDirectory The destination directory
	 * @throws IOException in case of an error
	 */
	public static void copyDirectory(File sourceDirectory, File destinationDirectory) throws IOException
	{
		if(!sourceDirectory.exists() || !sourceDirectory.isDirectory())
			throw new IOException("No such source directory: "+sourceDirectory.getAbsolutePath());
		if(destinationDirectory.exists()) {
			if(!destinationDirectory.isDirectory())
				throw new IOException("Destination exists but is not a directory: "+sourceDirectory.getAbsolutePath());
		} else
			destinationDirectory.mkdirs();
		
		File[] files = sourceDirectory.listFiles();
		for (File file : files) {
			File destinationFile = new File(destinationDirectory, file.getName());
			if(file.isDirectory())
				copyDirectory(file, destinationFile);
			else
				copyFile(file, destinationFile);
		}
	}
	

	/**
	 * Copy a resource loaded by the class loader of a given class to a file.
	 * <p>
	 * This is a convenience method for <code>copyResource(sourceResClass, sourceResName, new File(destinationFilename))</code>.
	 * @param sourceResClass The class whose class loader to use. If the class 
	 * 		was loaded using the bootstrap class loaderClassloader.getSystemResourceAsStream
	 * 		will be used. See {@link Class#getResourceAsStream(String)} for details.
	 * @param sourceResName The name of the resource
	 * @param destinationFilename Where to copy the contents of the resource
	 * @throws IOException in case of an error
	 */
	public static void copyResource(Class<?> sourceResClass, String sourceResName, String destinationFilename) 
	throws IOException
	{
		copyResource(sourceResClass, sourceResName, new File(destinationFilename));
	}

	/**
	 * Copy a resource loaded by the class loader of a given class to a file.
	 * @param sourceResClass The class whose class loader to use. If the class 
	 * 		was loaded using the bootstrap class loaderClassloader.getSystemResourceAsStream
	 * 		will be used. See {@link Class#getResourceAsStream(String)} for details.
	 * @param sourceResName The name of the resource
	 * @param destinationFile Where to copy the contents of the resource
	 * @throws IOException in case of an error
	 */
	public static void copyResource(Class<?> sourceResClass, String sourceResName, File destinationFile) 
	throws IOException
	{
		InputStream source = null;
		FileOutputStream destination = null;
		try{
			source = sourceResClass.getResourceAsStream(sourceResName);
			if (source == null)
				throw new FileNotFoundException("Class " + sourceResClass.getName() + " could not find resource " + sourceResName);

			if (destinationFile.exists()) {
				if (destinationFile.isFile()) {
//					DataInputStream in = new DataInputStream(System.in);
					if (!destinationFile.canWrite())
						throw new IOException("FileCopy: destination file is unwriteable: " + destinationFile.getCanonicalPath());
				} else
					throw new IOException("FileCopy: destination is not a file: " +	destinationFile.getCanonicalPath());
			} else {
				File parentdir = destinationFile.getParentFile();
				if (parentdir == null || !parentdir.exists())
					throw new IOException("FileCopy: destination directory doesn't exist: " + destinationFile.getCanonicalPath());
				if (!parentdir.canWrite())
					throw new IOException("FileCopy: destination directory is unwriteable: " + destinationFile.getCanonicalPath());
			} // if (destination_file.exists())
			destination = new FileOutputStream(destinationFile);
			transferStreamData(source,destination);
		} finally {
			if (source != null)
				try { source.close(); } catch (IOException e) { ; }

			if (destination != null)
				try { destination.close(); } catch (IOException e) { ; }
		}
	}
	

	/**
	 * This method deletes the given directory recursively. If the given parameter
	 * specifies a file and no directory, it will be deleted anyway. If one or more
	 * files or subdirectories cannot be deleted, the method still continues and tries
	 * to delete as many files/subdirectories as possible.
	 *
	 * @param dir The directory or file to delete
	 * @return <code>true</code> if the file or directory does not exist anymore. 
	 * 		This means it either was not existing already before or it has been 
	 * 		successfully deleted. <code>false</code> if the directory could not be 
	 * 		deleted.
	 */
	public static boolean deleteDirectoryRecursively(File dir)
	{
		if (!dir.exists())
			return true;
		if (dir.isDirectory()) {
			File[] content = dir.listFiles();
			for (int i = 0; i < content.length; ++i) {
				File f = content[i];
				if (f.isDirectory())
					deleteDirectoryRecursively(f);
				else
					try {
						f.delete();
					} catch(SecurityException e) {
						// ignore according to docs.
					}
			}
		}
		try {
			return dir.delete();
		} catch(SecurityException e) {
			return false;
		}
	}


	/**
	 * 1 GB in bytes.
	 * This holds the result of the calculation 1 * 1024 * 1024 * 1024
	 */
	public static final long GIGABYTE = 1 * 1024 * 1024 * 1024;
	
	/**
	 * Read a text file and return the contents as string.
	 * @param f The file to read, maximum size 1 GB
	 * @param encoding The file encoding, e.g. "UTF-8"
	 * @throws FileNotFoundException if the file was not found
	 * @throws IOException in case of an io error
	 * @throws UnsupportedEncodingException If the named encoding is not supported
	 * @return The contents of the text file
	 */
	public static String readTextFile(File f, String encoding)
	throws FileNotFoundException, IOException, UnsupportedEncodingException
	{
		if (f.length() > GIGABYTE)
			throw new IllegalArgumentException("File exceeds " + GIGABYTE + " bytes: " + f.getAbsolutePath());
	
		StringBuffer sb = new StringBuffer();
		FileInputStream fin = new FileInputStream(f);
		try {
			InputStreamReader reader = new InputStreamReader(fin, encoding);
			try {
				char[] cbuf = new char[1024];
				int bytesRead;
				while (true) {
					bytesRead = reader.read(cbuf);
					if (bytesRead <= 0)
						break;
					else
						sb.append(cbuf, 0, bytesRead);
				}
			} finally {
				reader.close();
			}
		} finally {
			fin.close();
		}
		return sb.toString();
	}

	/**
	 * Write text to a file.
	 * @param file The file to write the text to
	 * @param text The text to write
	 * @param encoding The caracter set to use as file encoding (e.g. "UTF-8")
	 * @throws IOException in case of an io error
	 * @throws FileNotFoundException if the file exists but is a directory
	 *                   rather than a regular file, does not exist but cannot
	 *                   be created, or cannot be opened for any other reason
	 * @throws UnsupportedEncodingException If the named encoding is not supported
	 */
	public static void writeTextFile(File file, String text, String encoding)
	throws IOException, FileNotFoundException, UnsupportedEncodingException
	{
		FileOutputStream out = null;
		OutputStreamWriter w = null;
		try {
			out = new FileOutputStream(file);
			w = new OutputStreamWriter(out, encoding);
			w.write(text);
		} finally {
			if (w != null) w.close();
			if (out != null) out.close();
		}
	}
	
	/**
	 * This method encodes a byte array into a human readable hex string. For each byte,
	 * two hex digits are produced. They are concatted without any separators.
	 * <p>
	 * This is a convenience method for <code>encodeHexStr(buf, 0, buf.length)</code>
	 *
	 * @param buf The byte array to translate into human readable text.
	 * @return a human readable string like "fa3d70" for a byte array with 3 bytes and these values.
	 * @see #encodeHexStr(byte[], int, int)
	 * @see #decodeHexStr(String)
	 */
	public static String encodeHexStr(byte[] buf)
	{
		return encodeHexStr(buf, 0, buf.length);
	}

	/**
	 * Encode a byte array into a human readable hex string. For each byte,
	 * two hex digits are produced. They are concatted without any separators.
	 *
	 * @param buf The byte array to translate into human readable text.
	 * @param pos The start position (0-based).
	 * @param len The number of bytes that shall be processed beginning at the position specified by <code>pos</code>.
	 * @return a human readable string like "fa3d70" for a byte array with 3 bytes and these values.
	 * @see #encodeHexStr(byte[])
	 * @see #decodeHexStr(String)
	 */
	public static String encodeHexStr(byte[] buf, int pos, int len)
	{
		 StringBuffer hex = new StringBuffer();
		 while (len-- > 0) {
				byte ch = buf[pos++];
				int d = (ch >> 4) & 0xf;
				hex.append((char)(d >= 10 ? 'a' - 10 + d : '0' + d));
				d = ch & 0xf;
				hex.append((char)(d >= 10 ? 'a' - 10 + d : '0' + d));
		 }
		 return hex.toString();
	}
}
