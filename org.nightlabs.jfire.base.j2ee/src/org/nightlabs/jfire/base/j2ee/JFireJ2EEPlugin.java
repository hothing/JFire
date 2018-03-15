/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2006 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/
package org.nightlabs.jfire.base.j2ee;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.update.configurator.ConfiguratorUtils;
import org.nightlabs.jfire.classloader.remote.JFireRCDLDelegate;
import org.nightlabs.util.IOUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * @author Alexander Bieber
 * @author Marco Schulze
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JFireJ2EEPlugin
implements BundleActivator
{
	// The plug-in ID
	public static final String PLUGIN_ID = "org.nightlabs.jfire.base.j2ee";
	private static final Logger logger = Logger.getLogger(JFireJ2EEPlugin.class);

	// The shared instance
	private static JFireJ2EEPlugin plugin;

	/**
	 * The constructor
	 */
	public JFireJ2EEPlugin()
	{
		plugin = this;
	}

	private Bundle bundle;

	public Bundle getBundle()
	{
		return bundle;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception
	{
		this.bundle = context.getBundle();
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception
	{
		plugin = null;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////
//BEGIN: Must be exactly the same as in org.nightlabs.jfire.base.j2ee.osgi.RemoteClassLoadingHook
	private String hash(String s)
	{
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256"); // SHA-256 causes a 64 char hash string (SHA-512 would be 128 chars and maybe too long for windows - can't test)
		} catch (NoSuchAlgorithmException x) {
			throw new RuntimeException(x);
		}
		try {
			md.update(s.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		byte[] buf = md.digest();

		int len = buf.length;
		int pos = 0;
		StringBuilder hex = new StringBuilder();
		while (len-- > 0) {
				byte ch = buf[pos++];
				int d = (ch >> 4) & 0xf;
				hex.append((char)(d >= 10 ? 'a' - 10 + d : '0' + d));
				d = ch & 0xf;
				hex.append((char)(d >= 10 ? 'a' - 10 + d : '0' + d));
		 }
		 return hex.toString();
	}

	private File getJ2eePluginRuntimeDir()
	{
		//		for (Map.Entry<Object, Object> me : System.getProperties().entrySet())
		//			System.out.println(me.getKey() + " = " + me.getValue());
		//
		// relevant properties:
		//		osgi.instance.area = file:/home/marco/workspaces/runtime-org.nightlabs.jfire.base.ui.product/
		//		osgi.syspath = /home/marco/workspaces/jfire.branch-1.0/target-rcp-jfire-max-linux-gtk-x86_64/target/plugins
		//		osgi.instance.area.default = file:/home/marco/.jfire/workspace/
		//		osgi.install.area = file:/home/marco/workspaces/jfire.branch-1.0/target-rcp-jfire-max-linux-gtk-x86_64/target/
		String workspace = System.getProperty("osgi.instance.area");
		if (workspace == null || workspace.isEmpty())
			workspace = System.getProperty("osgi.instance.area.default");

		if (workspace == null || workspace.isEmpty())
			throw new IllegalStateException("There is neither the system property \"osgi.instance.area\" nor \"osgi.instance.area.default\" set! Are we running in OSGI?");

		String osgiInstallArea = System.getProperty("osgi.install.area");

		if (workspace.startsWith("file:")) {
			workspace = workspace.substring("file:".length());
			File workspaceDir = new File(workspace);

			File j2eePluginRuntimeDir = new File(new File(workspaceDir, "data"), "rcl");
			j2eePluginRuntimeDir = new File(j2eePluginRuntimeDir, hash(osgiInstallArea));
			j2eePluginRuntimeDir = new File(j2eePluginRuntimeDir, "org.nightlabs.jfire.base.j2ee");
			j2eePluginRuntimeDir = j2eePluginRuntimeDir.getAbsoluteFile();
			return j2eePluginRuntimeDir;
		}
		throw new UnsupportedOperationException("The system property \"osgi.instance.area\" or \"osgi.instance.area.default\" is not supported! Must start with \"file:\", but does not: " + workspace);
	}
//END: Must be exactly the same as in org.nightlabs.jfire.base.j2ee.osgi.RemoteClassLoadingHook
///////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static JFireJ2EEPlugin getDefault() {
		return plugin;
	}

	protected Set<String> readPublishedRemotePackages(File f)
	throws IOException
	{
		HashSet<String> res = new HashSet<String>();
		Reader r = new BufferedReader(new FileReader(f));
		try {
			StreamTokenizer st = new StreamTokenizer(r);
			st.resetSyntax();
			st.wordChars(0, '\n' - 1); st.wordChars('\n' + 1, Integer.MAX_VALUE);
			while (st.ttype != StreamTokenizer.TT_EOF) {
				if (st.ttype == StreamTokenizer.TT_WORD) {
					res.add(st.sval);
				}
				st.nextToken();
			}
		} finally {
			r.close();
		}
		return res;
	}

	protected void writePublishedRemotePackages(Set<String> packages, File f)
	throws IOException
	{
		Writer w = new BufferedWriter(new FileWriter(f));
		try {
			for (String pkg : packages) {
				w.write(pkg);
				w.write('\n');
			}
		} finally {
			w.close();
		}
	}

	/**
	 * Returns a file for the contents of the specified bundle.  Depending
	 * on how the bundle is installed the returned file may be a directory or a jar file
	 * containing the bundle content.
	 *
	 * @param bundle the bundle
	 * @return a file with the contents of the bundle
	 * @throws IOException if an error occurs during the resolution
	 *
	 * @since org.eclipse.equinox.common 3.4
	 *
	 * XXX: taken from Equinox 3.4 class FileLocator without any changes (Marc)
	 */
	private static File getBundleFile(Bundle bundle) throws IOException {
		URL rootEntry = bundle.getEntry("/"); //$NON-NLS-1$
		rootEntry = FileLocator.resolve(rootEntry);
		if ("file".equals(rootEntry.getProtocol())) //$NON-NLS-1$
			return new File(rootEntry.getPath());
		if ("jar".equals(rootEntry.getProtocol())) { //$NON-NLS-1$
			String path = rootEntry.getPath();
			if (path.startsWith("file:")) {
				// strip off the file: and the !/
				path = path.substring(5, path.length() - 2);
				return new File(path);
			}
		}
		throw new IOException("Unknown protocol"); //$NON-NLS-1$
	}

	private static boolean sourceFileOrDirModified(Properties properties, File source, Set<String> processedSourcePaths)
	{
		String sourcePath = source.getPath();

		if (!processedSourcePaths.add(sourcePath))
			return false;

		if (!source.exists())
			return true;

		boolean result = false;

		long timestamp = Long.MIN_VALUE;
		long fileSize = Long.MIN_VALUE;

		if (source.isDirectory()) {
			for (File f : source.listFiles()) {
				if (sourceFileOrDirModified(properties, f, processedSourcePaths))
					result = true;
			}
		}
		else {
			String timestampS = properties.getProperty(COPY_PROPERTIES_KEY_PREFIX_FILE + sourcePath + COPY_PROPERTIES_KEY_SUFFIX_TIMESTAMP);
			if (timestampS != null) {
				try {
					timestamp = Long.parseLong(timestampS, 36);
				} catch (NumberFormatException x) {
					// ignore
				}
			}

			String fileSizeS = properties.getProperty(COPY_PROPERTIES_KEY_PREFIX_FILE + sourcePath + COPY_PROPERTIES_KEY_SUFFIX_FILE_SIZE);
			if (fileSizeS != null) {
				try {
					fileSize = Long.parseLong(fileSizeS, 36);
				} catch (NumberFormatException x) {
					// ignore
				}
			}

			if (source.lastModified() != timestamp || source.length() != fileSize) {
				result = true;
				timestamp = source.lastModified();
				fileSize = source.length();
				timestampS = Long.toString(timestamp, 36);
				fileSizeS = Long.toString(fileSize, 36);
				properties.setProperty(COPY_PROPERTIES_KEY_PREFIX_FILE + sourcePath + COPY_PROPERTIES_KEY_SUFFIX_TIMESTAMP, timestampS);
				properties.setProperty(COPY_PROPERTIES_KEY_PREFIX_FILE + sourcePath + COPY_PROPERTIES_KEY_SUFFIX_FILE_SIZE, fileSizeS);
			}
		}

		return result;
	}

	private static final String COPY_PROPERTIES_KEY_PREFIX_FILE = "file:";
	private static final String COPY_PROPERTIES_KEY_SUFFIX_TIMESTAMP = "//timestamp";
	private static final String COPY_PROPERTIES_KEY_SUFFIX_FILE_SIZE = "//size";

	private static File getSourceDirMetaFile(File destDir)
	{
		return new File(destDir, ".source-dir.properties");
	}

	private static Properties readSourceDirProperties(File destDir)
	throws IOException
	{
		File metaFile = getSourceDirMetaFile(destDir);
		if (!metaFile.exists())
			return null;

		Properties properties = new Properties();
		InputStream in = new FileInputStream(metaFile);
		try {
			properties.load(in);
		} finally {
			in.close();
		}
		return properties;
	}

	private static boolean sourceFileOrDirModified(Properties properties, Collection<File> sourceDirs)
	{
		boolean result = false;

		Set<String> processedSourcePaths = new HashSet<String>();

		// Check, if any of the existing source files/directories is new or has been modified.
		for (File sourceDir : sourceDirs) {
			if (sourceFileOrDirModified(properties, sourceDir, processedSourcePaths))
				result = true;
		}

		// Check, if any of the previously existing source files/directories does not exist anymore.
		Set<String> removedSourceFilePaths = new HashSet<String>();
		for (Object _propKey : properties.keySet()) {
			String propKey = _propKey.toString();
			if (!propKey.endsWith(COPY_PROPERTIES_KEY_SUFFIX_TIMESTAMP))
				continue;

			if (!propKey.startsWith(COPY_PROPERTIES_KEY_PREFIX_FILE))
				continue;

			String filePath = propKey.substring(
					COPY_PROPERTIES_KEY_PREFIX_FILE.length(),
					propKey.length() - COPY_PROPERTIES_KEY_SUFFIX_TIMESTAMP.length()
			);
			if (!processedSourcePaths.contains(filePath))
				removedSourceFilePaths.add(filePath);
		}

		if (!removedSourceFilePaths.isEmpty()) {
			for (String sourcePath : removedSourceFilePaths) {
				properties.remove(COPY_PROPERTIES_KEY_PREFIX_FILE + sourcePath + COPY_PROPERTIES_KEY_SUFFIX_TIMESTAMP);
				properties.remove(COPY_PROPERTIES_KEY_PREFIX_FILE + sourcePath + COPY_PROPERTIES_KEY_SUFFIX_FILE_SIZE);
			}

			result = true;
		}

		return result;
	}

	private static void writeSourceDirProperties(Properties properties, File destDir)
	throws IOException
	{
		File metaFile = getSourceDirMetaFile(destDir);
		OutputStream out = new FileOutputStream(metaFile);
		try {
			properties.store(out, null);
		} finally {
			out.close();
		}
	}

	/**
	 * Copy the bundle if it is deployed as directory or extract it if it is deployed as a jar.
	 * @param bundle The bundle
	 * @param j2eePluginRuntimeDir The target directory
	 * @throws IOException In case of an error
	 */
	private void copyJ2eePluginToRuntimeDir(Bundle bundle, File j2eePluginRuntimeDir) throws IOException
	{
		File bundleFile = getBundleFile(bundle);
		if(bundleFile.isDirectory()) {
			// packaged as directory
			IOUtil.copyDirectory(bundleFile, j2eePluginRuntimeDir);
		} else if(bundleFile.isFile()) {
			// packaged as jar file
			IOUtil.unzipArchive(bundleFile, j2eePluginRuntimeDir);
		} else {
			throw new IOException("Invalid file type: "+bundleFile.getAbsolutePath());
		}
	}

	private boolean isJ2eePlugin(URL pluginLocation)
	{
		// TODO read the MANIFEST.MF instead of just using the file name / directory name.
		// Note, that if we find too many plugins here, it doesn't harm. All plugins matching
		// this method are surveilled for changes. Thus, if we find too many, the system
		// will react on changes of plugins which don't matter - that's no essential error,
		// but just a minor in-efficiency. Marco.
		return pluginLocation.toExternalForm().contains("org.nightlabs.jfire.base.j2ee");
	}

	private Collection<File> getJ2eePluginLocations()
	{
		Set<File> result = new HashSet<File>();
		URL[] pluginPath = ConfiguratorUtils.getCurrentPlatformConfiguration().getPluginPath();
		for (URL pluginURL : pluginPath) {
			if (!isJ2eePlugin(pluginURL))
				continue;

			if (!"file".equals(pluginURL.getProtocol())) {
				logger.warn("getJ2eePluginLocations: pluginURL is not a file!!! " + pluginURL);
				continue;
			}

			File pluginFile = new File(pluginURL.getPath());
			result.add(pluginFile);
		}
		return result;
	}

	private void writeDeleteMarkerAndDelete(File dir)
	throws IOException
	{
		if (!dir.exists()) // nothing to do since it doesn't exist
			return;

		if (!dir.isDirectory())
			throw new IllegalArgumentException("The path does not point to a directory: " + dir.getAbsolutePath());

		// First create the deletion marker, thus ensuring that a partially deleted directory
		// will be removed completely at the next start.
		File deletionMarker = new File(dir.getParentFile(), dir.getName() + ".delete.me");
		IOUtil.writeTextFile(deletionMarker, "The directory \"" + dir.getAbsolutePath() + "\" should be deleted. Just in case, deleting it fails, we'll delete it later (thus first placing this marker file).");

		// Then delete. If this fails/completes only partially, the delete-marker created above
		// should be taken into account by RemoteClassLoadingHook.installJ2eePlugin(...).
		IOUtil.deleteDirectoryRecursively(dir);
	}

	/**
	 * This method rewrites (if necessary) the MANIFEST.MF of this plugin (i.e. <code>org.nightlabs.jfire.base.j2ee</code>).
	 * <p>
	 * Since 2009-05-27, this method does not touch the original MANIFEST.MF anymore, but instead creates a runtime-version
	 * of the <code>org.nightlabs.jfire.base.j2ee</code> plug-in. If this runtime-version exists, it is installed immediately
	 * on OSGI-start by the class <code>org.nightlabs.jfire.base.j2ee.osgi.RemoteClassLoadingHook</code>.
	 * </p>
	 * <p>
	 * This new mechanism employing a runtime-copy of the j2ee plugin makes it possible to have a system-wide read-only
	 * JFire-installation.
	 * </p>
	 *
	 * @return <code>true</code>, if the file had to be modified (and thus a reboot of the RCP is necessary).
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public boolean updateManifest()
	throws IOException, URISyntaxException
	{
		System.out.println("****************************************************************");
		boolean changed = false;

		// find the MANIFEST.MF
		File manifestFile;
		{
			// Bundle bundle = Platform.getBundle(PLUGIN_ID);
			Bundle bundle = getBundle();
//			Path path = new Path("META-INF/MANIFEST.MF");
//			URL fileURL = FileLocator.find(bundle, path, null);
//			URL realURL = FileLocator.resolve(fileURL);
//
//			if (!realURL.getProtocol().equalsIgnoreCase("file"))
//				throw new IllegalStateException("The plugin org.nightlabs.jfire.j2ee is not deployed as directory-plugin. Its URL protocol is "+realURL.getProtocol());
//
//			manifestFile = new File(realURL.getPath());

			Properties sourceDirProps = null;
			File j2eePluginRuntimeDir = getJ2eePluginRuntimeDir();
			if (j2eePluginRuntimeDir.exists()) {
				// Is it outdated? If yes, we have to delete it and return true without copying
				// (because we don't know what to copy [there might be multiple versions installed
				// in multiple locations and we don't know the search-strategy of the eclipse-plugin-finder]).

				sourceDirProps = readSourceDirProperties(j2eePluginRuntimeDir);
				if (sourceFileOrDirModified(sourceDirProps, getJ2eePluginLocations())) {
					writeDeleteMarkerAndDelete(j2eePluginRuntimeDir);
					return true;
				}
			}

			if (!j2eePluginRuntimeDir.exists()) {
				boolean successful = false;
				try {
					j2eePluginRuntimeDir.mkdirs();
					if (!j2eePluginRuntimeDir.isDirectory())
						throw new IllegalStateException("Creation of directory failed: " + j2eePluginRuntimeDir);

					changed = true;
					copyJ2eePluginToRuntimeDir(bundle, j2eePluginRuntimeDir);

					successful = true;
				} finally {
					if (!successful) { // clean up in case it was only partially created.
						changed = true; // make sure we definitely restart - no matter what
						writeDeleteMarkerAndDelete(j2eePluginRuntimeDir);
					}
				}

				if (sourceDirProps == null) {
					sourceDirProps = new Properties();
					Collection<File> pluginLocations = getJ2eePluginLocations();
					File bundleFile = getBundleFile(bundle);
					if (!pluginLocations.contains(bundleFile)) {
						logger.error("The current bundle is not contained in the plugin-locations!");
						logger.error("  * current-bundle: " + bundleFile.getAbsolutePath());
						for (File pluginLocation : pluginLocations) {
							logger.error("  * plugin-location: " + pluginLocation.getAbsolutePath());
						}

						throw new IllegalStateException("The current bundle is not contained in the plugin-locations! " + bundleFile.getAbsolutePath());
					}

					sourceFileOrDirModified(sourceDirProps, pluginLocations); // just to index the files
				}

				writeSourceDirProperties(sourceDirProps, j2eePluginRuntimeDir);
			}

			manifestFile = new File(j2eePluginRuntimeDir, "META-INF/MANIFEST.MF");
//			if (!j2eePluginRuntimeDirAlreadyExisted) {
//				manifestFile = new File(j2eePluginRuntimeDir, "META-INF/MANIFEST.MF");
//			}
//			else {
//				String bundleLocation = bundle.getLocation();
//				if (!bundleLocation.startsWith("file:"))
//					throw new IllegalStateException("The plugin org.nightlabs.jfire.j2ee is not loaded from its runtime-location, even though that location exists! Its loaded location is " + bundleLocation);
//
//				URL realURL = new URL(bundleLocation);
//				manifestFile = new File(realURL.getPath(), "META-INF/MANIFEST.MF");
//			}

			if (!manifestFile.exists())
				throw new IllegalStateException("The plugin's MANIFEST.MF does not exist: " + manifestFile.getAbsolutePath());
		}

		File metaInfDir = manifestFile.getParentFile();
		File origManifestFile = new File(metaInfDir, "MANIFEST.MF.orig");
		if (!origManifestFile.exists()) {
			// it seems, this is the first start - so create a backup of the original MANIFEST.MF

			InputStream in = new FileInputStream(manifestFile);
			try {
				OutputStream out = new FileOutputStream(origManifestFile);
				try {
					IOUtil.transferStreamData(in, out);
				} finally {
					out.close();
				}
			} finally {
				in.close();
			}
			origManifestFile.setLastModified(manifestFile.lastModified());
		}

		// read the last server-package-list
		File publishedRemotePackagesFile = new File(metaInfDir, "publishedRemotePackages.csv");
		Set<String> lastPublishedRemotePackages;
		if (publishedRemotePackagesFile.exists())
			lastPublishedRemotePackages = readPublishedRemotePackages(publishedRemotePackagesFile);
		else
			lastPublishedRemotePackages = new HashSet<String>();

		// obtain the current published remote packages
		Set<String> currentPublishedRemotePackages = JFireRCDLDelegate.sharedInstance().getPublishedRemotePackages();

		// diff the last and the new ones
		if (!changed) {
			for (String currPkg : currentPublishedRemotePackages) {
				if (!lastPublishedRemotePackages.contains(currPkg)) {
					changed = true; // there is a new one on the server which we don't have yet locally
					break;
				}
			}
		}

		if (!changed) {
			for (String lastPkg : lastPublishedRemotePackages) {
				if (!currentPublishedRemotePackages.contains(lastPkg)) {
					changed = true; // one of the packages that we still have locally doesn't exist anymore on the server
					break;
				}
			}
		}

		if (!changed)
			return false;

		// We need to read the MANIFEST.MF.orig, append all the current published packages and write it as MANIFEST.MF.

		// read the MANIFEST.MF.orig
		InputStream in = new FileInputStream(origManifestFile);
		Manifest manifest;
		try {
			manifest = new Manifest(in);
		} finally {
			in.close();
		}

		// get the Export-Package entry
		StringBuilder exportPackage = new StringBuilder(
				manifest.getMainAttributes().getValue("Export-Package"));

		// append all the packages from the server
		for (String pkg : currentPublishedRemotePackages) {
			exportPackage.append(',');
			exportPackage.append(pkg);
		}

		// write the MANIFEST.MF
		manifest.getMainAttributes().put(new Attributes.Name("Export-Package"), exportPackage.toString());
		OutputStream out = new FileOutputStream(manifestFile);
		try {
			manifest.write(out);
		} finally {
			out.close();
		}

		// write the server's packages
		writePublishedRemotePackages(currentPublishedRemotePackages, publishedRemotePackagesFile);

		return true;
	}
}
