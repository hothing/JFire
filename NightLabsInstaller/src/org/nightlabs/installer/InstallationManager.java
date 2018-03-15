package org.nightlabs.installer;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.nightlabs.installer.base.ErrorHandler;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.Installer;
import org.nightlabs.installer.base.Navigator;
import org.nightlabs.installer.base.defaults.DefaultErrorHandler;
import org.nightlabs.installer.base.defaults.DefaultInstaller;
import org.nightlabs.installer.base.defaults.DefaultNavigator;
import org.nightlabs.installer.util.Util;

/**
 * @version $Revision: 1567 $ - $Date: 2009-01-17 14:54:35 +0100 (Sa, 17 Jan 2009) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class InstallationManager
{
	private static final String DEFAULT_CONFIG_RESOURCE = "installation-config.properties"; //$NON-NLS-1$
	private static InstallationManager installationManager = null;
	private Properties config = null;
	private UIType uiType;
	private String masterDefaultsFilename = null;
	private Properties masterDefaults = null;
	private String configFilename = null;
	private InputStream configInputStream = null;

	private Installer installer = null;
	private Navigator navigator = null;
	private ErrorHandler errorHandler = null;
	private ResourceBundle resourceBundle = null;
	
	private InstallerCLI installerCLI;
	
	/**
	 * The execution progress listener list.
	 */
	private Set<ExecutionProgressListener> listeners = null;
	
	
	public static InstallationManager getInstallationManager()
	{
		if(installationManager == null)
			throw new IllegalStateException("Installation manager was not yet created"); //$NON-NLS-1$
		return installationManager;
	}
	
	protected InstallationManager(String[] args)
	{
		this(new InstallerCLI(args));
	}
	
	private UIType getAutoUIType()
	{
		if(GraphicsEnvironment.isHeadless())
			return UIType.console;
		else
			return UIType.swing;
//		if(EnvironmentHelper.isWindows())
//			return UIType.swing;
//		else
//			return UIType.console;
	}
	
	protected InstallationManager(InstallerCLI cli)
	{
		installationManager = this;
		this.installerCLI = cli;
		cli.handleHelp();
		this.uiType = cli.getUIType();
		if(uiType == UIType.auto)
			uiType = getAutoUIType();
		this.masterDefaultsFilename = cli.getDefaultsFilename();
		this.configFilename = cli.getConfigFilename();
		Logger.setVerbose(cli.isVerbose());
	}
	
	public Properties getConfig() throws InstallationException
	{
		if(config == null) {
			config = new Properties();
			try {
				InputStream configStream = getConfigInputStream();
				if(configStream == null)
					throw new IllegalStateException(Messages.getString("InstallationManager.configNotFoundError")); //$NON-NLS-1$
				config.load(configStream);
			} catch (IOException e) {
				throw new InstallationException(Messages.getString("InstallationManager.configLoadError"), e); //$NON-NLS-1$
			}
		}
		return config;
	}
	
	protected Installer getInstaller() throws InstallationException
	{
		if(installer == null) {
			try {
				installer = (Installer)Util.getConfigurable(getConfig(), "installer.", DefaultInstaller.class); //$NON-NLS-1$
			} catch (Throwable e) {
				throw new InstallationException(Messages.getString("InstallationManager.installerError"), e); //$NON-NLS-1$
			}
		}
		return installer;
	}

	protected Navigator getNavigator() throws InstallationException
	{
		if(navigator == null) {
			try {
				navigator = (Navigator)Util.getConfigurable(getConfig(), "navigator.", DefaultNavigator.class); //$NON-NLS-1$
			} catch (Throwable e) {
				throw new InstallationException(Messages.getString("InstallationManager.navigatorError"), e); //$NON-NLS-1$
			}
		}
		return navigator;
	}
	
	public ErrorHandler getErrorHandler()
	{
		if(errorHandler == null) {
			try {
				errorHandler = (ErrorHandler)Util.getConfigurable(getConfig(), "errorHandler.", DefaultErrorHandler.class); //$NON-NLS-1$
			} catch (Throwable e) {
				errorHandler = new DefaultErrorHandler();
				errorHandler.handle(e);
			}
			errorHandler.setVerbose(installerCLI.isVerbose());
		}
		return errorHandler;
	}
	
	protected void run()
	{
		try {
			Installer installer = installationManager.getInstaller();
			installer.run(true);
		} catch(Throwable e) {
			getErrorHandler().handle(e);
		}
	}
	
	public String getString(String key) throws InstallationException
	{
		return getString(key, "!!"+key+"!!"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String getString(String key, String defaultValue) throws InstallationException
	{
		if(resourceBundle == null) {
			String bundleName = getConfig().getProperty("installer.bundle"); //$NON-NLS-1$
			if(bundleName != null)
				resourceBundle = ResourceBundle.getBundle(bundleName);
			else
				resourceBundle = ResourceBundle.getBundle("org.nightlabs.installer.installer"); //$NON-NLS-1$
		}
		try {
			return resourceBundle.getString(key);
		} catch(MissingResourceException e) {
			return defaultValue;
		}
	}
	
	public boolean haveString(String key) throws InstallationException
	{
		return getString(key, null) != null;
	}
	
	/**
	 * Get the uiType.
	 * @return the uiType
	 */
	public UIType getUiType()
	{
		return uiType;
	}
	
	public static void main(String[] args)
	{
		InstallationManager installationManager = new InstallationManager(new InstallerCLI(args));
		installationManager.run();
	}

	/**
	 * Get the configInputStream.
	 * @return the configInputStream
	 * @throws IOException In case of an io error
	 */
	protected InputStream getConfigInputStream() throws IOException
	{
		if(configInputStream == null) {
			if(configFilename != null)
				configInputStream = new FileInputStream(configFilename);
			else
				configInputStream = InstallationManager.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_RESOURCE);
		}
		return configInputStream;
	}

	/**
	 * Set the configInputStream.
	 * @param configInputStream the configInputStream to set
	 */
	protected void setConfigInputStream(InputStream configInputStream)
	{
		this.configInputStream = configInputStream;
	}
	
	/**
	 * Add an execution progress listener.
	 * @param l The listener to add
	 */
	public void addExecutionProgressListener(ExecutionProgressListener l)
	{
		if(listeners == null)
			listeners = new HashSet<ExecutionProgressListener>(1);
		listeners.add(l);
	}

	/**
	 * Remove an execution progress listener.
	 * @param l The listener to remove
	 */
	public void removeExecutionProgressListener(ExecutionProgressListener l)
	{
		if(listeners != null) {
			listeners.remove(l);
			if(listeners.isEmpty())
				listeners = null;
		}
	}
	
	/**
	 * Fire an execution progress event to all registered listeners.
	 * @param e The event to fire
	 */
	public void fireExecutionProgress(ExecutionProgressEvent e)
	{
		if(listeners != null)
			for (ExecutionProgressListener l : listeners)
				l.executionProgress(e);
	}
	
	/**
	 * Get the defaults loaded from the file given with the "default" option.
	 * @return The default properties or <code>null</code> if this option
	 * 		is not available.
	 * @throws InstallationException In case of an error with the option value
	 * 		or the properties file.
	 */
	protected Properties getMasterDefaults() throws InstallationException
	{
		if(masterDefaults == null) {
			masterDefaults = new Properties();
			if(masterDefaultsFilename != null) {
				File f = new File(masterDefaultsFilename);
				if(!f.exists() || !f.canRead())
					throw new InstallationException("Invalid defaults file: "+masterDefaultsFilename);
				try {
					FileInputStream fileInputStream = new FileInputStream(f);
					try {
						masterDefaults.load(fileInputStream);
					} finally {
						fileInputStream.close();
					}
				} catch(IOException e) {
					throw new InstallationException("Loading defaults failed", e);
				}
			}
		}
		return masterDefaults;
	}
	
	/**
	 * Get the master default value for a given installation entity
	 * as provided by the given defaults file command line option.
	 * @param id The installation entity id (fully qualified)
	 * @return The master default value or <code>null</code> if no such 
	 * 		value exists.
	 * @throws InstallationException In case of an error getting the master
	 * 		default values
	 */
	public String getMasterDefaultValue(String id) throws InstallationException
	{
		if(id == null) {
			Logger.out.println("Warning: id = null");
			return null;
		}
		Logger.out.println("Request for master default value for id: "+id);
		Properties masterDefaults = getMasterDefaults();
		return masterDefaults == null ? null : masterDefaults.getProperty(id);
	}
}
