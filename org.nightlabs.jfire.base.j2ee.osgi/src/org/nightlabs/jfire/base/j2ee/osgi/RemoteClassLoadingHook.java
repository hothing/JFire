/**
 *
 */
package org.nightlabs.jfire.base.j2ee.osgi;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.ArrayList;

import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.HookConfigurator;
import org.eclipse.osgi.baseadaptor.HookRegistry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook;
import org.eclipse.osgi.baseadaptor.loader.BaseClassLoader;
import org.eclipse.osgi.baseadaptor.loader.ClasspathEntry;
import org.eclipse.osgi.baseadaptor.loader.ClasspathManager;
import org.eclipse.osgi.framework.adaptor.BundleProtectionDomain;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;
import org.nightlabs.classloader.osgi.DelegatingClassLoaderOSGI;
import org.osgi.framework.BundleContext;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class RemoteClassLoadingHook implements ClassLoadingHook, HookConfigurator {

	public RemoteClassLoadingHook() {
		System.out.println("RemoteClassLoadingHook instantiated");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#addClassPathEntry(java.util.ArrayList, java.lang.String, org.eclipse.osgi.baseadaptor.loader.ClasspathManager, org.eclipse.osgi.baseadaptor.BaseData, java.security.ProtectionDomain)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean addClassPathEntry(ArrayList cpEntries, String cp, ClasspathManager hostmanager, BaseData sourcedata, ProtectionDomain sourcedomain) {
		return false;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////
//BEGIN: Must be exactly the same as in org.nightlabs.jfire.base.j2ee.JFireJ2EEPlugin
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
//END: Must be exactly the same as in org.nightlabs.jfire.base.j2ee.JFireJ2EEPlugin
///////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Deletes a directory (or a file) recursively.
	 * <p>
	 * Method copied from <code>org.nightlabs.util.IOUtil</code> since we don't have access
	 * to this bundle from here.
	 * </p>
	 */
	private static boolean deleteDirectoryRecursively(File dir)
	{
		if (!dir.exists())
			return true;

		// If we're running this on linux (that's what I just tested ;) and dir denotes a symlink,
		// we must not dive into it and delete its contents! We can instead directly delete dir.
		// There is no way in Java (except for calling system tools) to find out whether it is a symlink,
		// but we can simply delete it. If the deletion succeeds, it was a symlink, otherwise it's a real directory.
		// This way, we don't delete the contents in symlinks and thus prevent data loss!
		try {
			if (dir.delete())
				return true;
		} catch(SecurityException e) {
			// ignore according to docs.
			return false; // or should we really ignore this security exception and delete the contents?!?!?! To return false instead is definitely safer.
		}

		if (dir.isDirectory()) {
			File[] content = dir.listFiles();
			for (File f : content) {
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
	 * If the plugin org.nightlabs.jfire.base.j2ee exists in its runtime-location (i.e. temp-directory,
	 * where it can be modified), this method installs it into OSGI from there. Otherwise, it will be
	 * found by the Eclipse RCP plugin-finder and during first login, it will create itself this
	 * runtime-directory.
	 * <p>
	 * See the code in org.nightlabs.jfire.base.j2ee.JFireJ2EEPlugin#updateManifest() for further details.
	 * </p>
	 */
	private void installJ2eePlugin(BundleContext bundleContext)
	{
		System.out.println("Installing J2EE plugin...");
		try {
			File j2eePluginRuntimeDir = getJ2eePluginRuntimeDir();
			File deletionMarker = new File(j2eePluginRuntimeDir.getParentFile(), j2eePluginRuntimeDir.getName() + ".delete.me");
			if (j2eePluginRuntimeDir.exists()) {
				if (deletionMarker.exists()) {
					deleteDirectoryRecursively(j2eePluginRuntimeDir);

					if (j2eePluginRuntimeDir.exists())
						throw new IllegalStateException("Deleting this directory failed (permissions?): " + j2eePluginRuntimeDir.getAbsolutePath());
				}

				// BEGIN downward compatibility.
				File oldDeletionMarker = new File(j2eePluginRuntimeDir, "delete.me");
				if (oldDeletionMarker.exists()) {
					deleteDirectoryRecursively(j2eePluginRuntimeDir);

					if (j2eePluginRuntimeDir.exists())
						throw new IllegalStateException("Deleting this directory failed (permissions?): " + j2eePluginRuntimeDir.getAbsolutePath());
				}
				// END downward compatibility.
			}

			if (deletionMarker.exists() && !j2eePluginRuntimeDir.exists()) {
				deletionMarker.delete();
				if (deletionMarker.exists())
					throw new IllegalStateException("Deleting this file failed (permissions?): " + deletionMarker.getAbsolutePath());
			}

			if (j2eePluginRuntimeDir.exists()) {
				// commented because internal bundle start code BundeInstall.begin() fails when path contains URL encoded spaces
				// which is the case with toURI().toURL() only toURL() leaves spaces unencoded and it works
//				String j2eePluginRuntimeURL = j2eePluginRuntimeDir.toURI().toURL().toExternalForm();
				@SuppressWarnings("deprecation")
				String j2eePluginRuntimeURL = j2eePluginRuntimeDir.toURL().toExternalForm();
				bundleContext.installBundle(j2eePluginRuntimeURL);
				System.out.println("J2EE plugin in runtime directory installed: " + j2eePluginRuntimeDir);
			}
			else
				System.out.println("J2EE plugin runtime directory does not (yet) exist: " + j2eePluginRuntimeDir);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private boolean j2eePluginIsInstalled = false;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#createClassLoader(java.lang.ClassLoader, org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate, org.eclipse.osgi.framework.adaptor.BundleProtectionDomain, org.eclipse.osgi.baseadaptor.BaseData, java.lang.String[])
	 */
	@Override
	public BaseClassLoader createClassLoader(ClassLoader parent, ClassLoaderDelegate delegate, BundleProtectionDomain domain, BaseData data, String[] bundleclasspath) {
		System.out.println("RemoteClassLoadingHook called createClassLoader for "+data.getBundle().getSymbolicName());
		System.out.println(delegate.getClass().toString());

		if (!j2eePluginIsInstalled) {
			installJ2eePlugin(data.getBundle().getBundleContext());
			j2eePluginIsInstalled = true;
		}

		if (data.getBundle().getSymbolicName().equals("org.nightlabs.jfire.base.j2ee")) {
			System.out.println("RemoteClassLoadingHook called createClassLoader returning DelegatingClassLoaderOSGI");
			return DelegatingClassLoaderOSGI.createSharedInstance(parent, delegate, domain, data, bundleclasspath);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#findLibrary(org.eclipse.osgi.baseadaptor.BaseData, java.lang.String)
	 */
	@Override
	public String findLibrary(BaseData data, String libName) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#getBundleClassLoaderParent()
	 */
	@Override
	public ClassLoader getBundleClassLoaderParent() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#initializedClassLoader(org.eclipse.osgi.baseadaptor.loader.BaseClassLoader, org.eclipse.osgi.baseadaptor.BaseData)
	 */
	@Override
	public void initializedClassLoader(BaseClassLoader baseClassLoader, BaseData data) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#processClass(java.lang.String, byte[], org.eclipse.osgi.baseadaptor.loader.ClasspathEntry, org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry, org.eclipse.osgi.baseadaptor.loader.ClasspathManager)
	 */
	@Override
	public byte[] processClass(String name, byte[] classbytes, ClasspathEntry classpathEntry, BundleEntry entry, ClasspathManager manager) {
		return null;
	}

	@Override
	public void addHooks(HookRegistry hookRegistry) {
		hookRegistry.addClassLoadingHook(new RemoteClassLoadingHook());
	}

}
