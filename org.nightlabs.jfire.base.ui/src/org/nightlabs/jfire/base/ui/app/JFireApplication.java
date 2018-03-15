/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
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

package org.nightlabs.jfire.base.ui.app;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.app.AbstractApplication;
import org.nightlabs.base.ui.app.AbstractWorkbenchAdvisor;
import org.nightlabs.base.ui.language.LanguageManager;
import org.nightlabs.j2ee.InitialContextProvider;
import org.nightlabs.jfire.base.j2ee.RemoteResourceFilterRegistry;
import org.nightlabs.jfire.base.login.JFireSecurityConfiguration;
import org.nightlabs.jfire.base.ui.login.JFireLoginHandler;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.login.LoginAbortedException;
import org.nightlabs.util.IOUtil;

/**
 * JFireApplication is the main executed class {@see JFireApplication#run(Object)}.
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class JFireApplication
extends AbstractApplication
{
	public static final String PLUGIN_ID = "org.nightlabs.jfire.base.ui"; //$NON-NLS-1$

	/**
	 * The logger instance used in this class.
	 */
	private static final Logger logger = Logger.getLogger(JFireApplication.class);

	private static List<JFireApplicationListener> applicationListener = new LinkedList<JFireApplicationListener>();

	public static void addApplicationListener(JFireApplicationListener listener) {
		applicationListener.add(listener);
	}

	public static void removeApplicationListener(JFireApplicationListener listener) {
		applicationListener.remove(listener);
	}

	public static final int APPLICATION_EVENTTYPE_STARTED = 1;

	void notifyApplicationListeners(int applicationEventType) {
		for (Iterator<?> iter = applicationListener.iterator(); iter.hasNext();) {
			JFireApplicationListener listener = (JFireApplicationListener) iter.next();
			switch (applicationEventType) {
				case APPLICATION_EVENTTYPE_STARTED:
					listener.applicationStarted();
					break;
			}
		}
	}

	public String initApplicationName() {
		return "jfire"; //$NON-NLS-1$
	}

	@Override
	protected void preCreateWorkbench()
	{
		try {
			// initialise truststore
			initSSLTruststore();

			initLogin();

			LanguageManager.sharedInstance().setLanguage();
		} catch(Exception e) {
			logger.error("preCreateWorkbench: " + e.getClass() + ": " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
			throw new RuntimeException(e);
		}
	}

	/**
	 * If no truststore is specified set the java system wide truststore to the default:
	 * config_dir/jfire-server.truststore and if no truststore is found at that location, the default
	 * truststore from this plugin/src/jfire-server.truststore is copied to the expected location.
	 *
	 * @throws IOException if the truststore couldn't be copied.
	 */
	protected void initSSLTruststore() throws IOException
	{
		if (System.getProperty("javax.net.ssl.trustStore") != null) //$NON-NLS-1$
			return;

		File truststoreFile = new File(getConfigDir(), "jfire-server.truststore").getAbsoluteFile(); //$NON-NLS-1$
		if (!truststoreFile.exists())
		{
			IOUtil.copyResource(JFireApplication.class, "/jfire-server.truststore", truststoreFile); //$NON-NLS-1$
		}

		System.setProperty("javax.net.ssl.trustStore", truststoreFile.getPath()); //$NON-NLS-1$
		System.setProperty("javax.net.ssl.trustStorePassword", "nightlabs"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public AbstractWorkbenchAdvisor initWorkbenchAdvisor(Display display) {
		return new JFireWorkbenchAdvisor();
	}

	protected void initLogin() throws LoginException, LoginAbortedException
	{
		// create log directory if not existent
		AbstractApplication.getLogDir();
		try {
			InitialContextProvider icp = new InitialContextProvider() {
				public InitialContext getInitialContext()
				throws LoginException, NamingException
				{
					return new InitialContext(getInitialContextProperties());
				}
				public Hashtable getInitialContextProperties()
				throws LoginException
				{
					return Login.sharedInstance().getInitialContextProperties();
				}
			};


//			File classLoaderCacheDir = new File(AbstractApplication.getRootDir(), "classloader.cache"); //$NON-NLS-1$

			// https://www.jfire.org/modules/bugs/view.php?id=479
			// When using Windows "domains", the client computers' home directories are mirrored to the server. Unfortunately, though,
			// this mechanism suffers a maximum path length which our class-loader's cache directory often exceeds. Since we 1st don't need
			// the classes to be mirrored to the Windows Domain Controller and 2nd they cause trouble, we simply put this directory into the
			// temp directory instead (which is not mirrored to the server).
			//
			// In GNU/Linux, there is exactly one temp-directory for all users; hence we need to put the current OS user's name into the path.
			// This is done by IOUtil.getUserTempDir().

			File classLoaderCacheDir = new File(IOUtil.createUserTempDir("jfire.", null), "classloader.cache"); //$NON-NLS-1$ //$NON-NLS-2$
			org.nightlabs.jfire.classloader.remote.JFireRCDLDelegate.createSharedInstance(
					icp,
					classLoaderCacheDir).setFilter(RemoteResourceFilterRegistry.sharedInstance());
//		} catch (LoginException e) {
//			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			LoginException x = new LoginException(e.getMessage());
			x.initCause(e);
			throw x;
		}
		initializeLoginModule();
	}

	protected void initializeLoginModule()
	{
		JFireSecurityConfiguration.declareConfiguration();
		try {
			Login.getLogin(false).setLoginHandler(new JFireLoginHandler());
		} catch (LoginException e) {
			throw new RuntimeException("How the hell could this happen?!", e); //$NON-NLS-1$
		}
	}


}
