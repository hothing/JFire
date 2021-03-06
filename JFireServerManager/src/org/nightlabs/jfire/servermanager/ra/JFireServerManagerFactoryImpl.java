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

package org.nightlabs.jfire.servermanager.ra;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.transaction.UserTransaction;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.InvokeUtil;
import org.nightlabs.jfire.base.JFireBasePrincipal;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.base.JFireServerLocalLoginManager;
import org.nightlabs.jfire.base.PersistenceManagerProvider;
import org.nightlabs.jfire.base.SimplePrincipal;
import org.nightlabs.jfire.classloader.CLRegistrar;
import org.nightlabs.jfire.classloader.CLRegistrarFactory;
import org.nightlabs.jfire.classloader.CLRegistryCfMod;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jdo.cache.CacheCfMod;
import org.nightlabs.jfire.jdo.cache.CacheManagerFactory;
import org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationManagerFactory;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisationinit.OrganisationInitException;
import org.nightlabs.jfire.organisationinit.OrganisationInitManager;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.Role;
import org.nightlabs.jfire.security.RoleConstants;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.RoleRef;
import org.nightlabs.jfire.security.RoleSet;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.UndeployedRoleGroupAuthorityUserRecord;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.AuthorizedObjectRefID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.jfire.security.listener.SecurityChangeController;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurationException;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurator;
import org.nightlabs.jfire.serverinit.ServerInitManager;
import org.nightlabs.jfire.servermanager.DuplicateOrganisationException;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.NoServerAdminException;
import org.nightlabs.jfire.servermanager.OrganisationNotFoundException;
import org.nightlabs.jfire.servermanager.RoleImportSet;
import org.nightlabs.jfire.servermanager.config.CreateOrganisationConfigModule;
import org.nightlabs.jfire.servermanager.config.DatabaseCf;
import org.nightlabs.jfire.servermanager.config.J2eeServerTypeRegistryConfigModule;
import org.nightlabs.jfire.servermanager.config.JDOCf;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.config.OrganisationConfigModule;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.servermanager.config.J2eeServerTypeRegistryConfigModule.J2eeLocalServer;
import org.nightlabs.jfire.servermanager.createorganisation.BusyCreatingOrganisationException;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationException;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgress;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgressID;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStatus;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStep;
import org.nightlabs.jfire.servermanager.db.DatabaseAdapter;
import org.nightlabs.jfire.servermanager.deploy.DeployOverwriteBehaviour;
import org.nightlabs.jfire.servermanager.deploy.DeployedFileAlreadyExistsException;
import org.nightlabs.jfire.servermanager.deploy.DeploymentJarItem;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapterException;
import org.nightlabs.jfire.servermanager.j2ee.JMSConnectionFactoryLookup;
import org.nightlabs.jfire.servermanager.j2ee.ServerStartNotificationListener;
import org.nightlabs.jfire.servermanager.xml.AuthorityTypeDef;
import org.nightlabs.jfire.servermanager.xml.EARApplication;
import org.nightlabs.jfire.servermanager.xml.EARApplicationSet;
import org.nightlabs.jfire.servermanager.xml.EARModuleType;
import org.nightlabs.jfire.servermanager.xml.EJBJarMan;
import org.nightlabs.jfire.servermanager.xml.JFireSecurityMan;
import org.nightlabs.jfire.servermanager.xml.JarEntryHandler;
import org.nightlabs.jfire.servermanager.xml.RoleGroupDef;
import org.nightlabs.jfire.servermanager.xml.XMLReadException;
import org.nightlabs.jfire.shutdownafterstartup.ShutdownAfterStartupManager;
import org.nightlabs.jfire.shutdownafterstartup.ShutdownControlHandle;
import org.nightlabs.math.Base62Coder;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.Util;

/**
 * @author marco schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JFireServerManagerFactoryImpl
	implements
		ConnectionFactory,
		JFireServerManagerFactory,
		PersistenceManagerProvider,
		ServerStartNotificationListener
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireServerManagerFactoryImpl.class);

	private final ManagedConnectionFactoryImpl mcf;
	private final ConnectionManager cm;
	private Reference ref;

	private volatile boolean upAndRunning = false;
	private volatile boolean shuttingDown = false;

	protected J2eeServerTypeRegistryConfigModule j2eeServerTypeRegistryConfigModule;
	protected J2eeServerTypeRegistryConfigModule.J2eeLocalServer j2eeLocalServerCf;

	protected OrganisationConfigModule organisationConfigModule;
	protected CreateOrganisationConfigModule createOrganisationConfigModule;
	protected CacheCfMod cacheCfMod;

	private CLRegistrarFactory clRegistrarFactory;

	public JFireServerManagerFactoryImpl(final ManagedConnectionFactoryImpl mcf, final ConnectionManager cm)
	throws ResourceException
	{
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": CONSTRUCTOR");
		this.mcf = mcf;
		this.cm = cm;

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run()
			{
				shuttingDown = true;
			}
		});

		Config config = getConfig();
		boolean saveConfig =
				config.getConfigModule(OrganisationConfigModule.class, false) == null ||
				config.getConfigModule(CreateOrganisationConfigModule.class, false) == null ||
				config.getConfigModule(J2eeServerTypeRegistryConfigModule.class, false) == null ||
				config.getConfigModule(CacheCfMod.class, false) == null;

		try {
			organisationConfigModule = config.createConfigModule(OrganisationConfigModule.class);
		} catch (ConfigException e) {
			logger.log(Level.FATAL, "Getting/creating OrganisationConfigModule failed!", e);
			throw new ResourceException(e.getMessage());
		}
		try {
			createOrganisationConfigModule = config.createConfigModule(CreateOrganisationConfigModule.class);
		} catch (ConfigException e) {
			logger.log(Level.FATAL, "Getting/creating CreateOrganisationConfigModule failed!", e);
			throw new ResourceException(e.getMessage());
		}
		try {
			j2eeServerTypeRegistryConfigModule = config.createConfigModule(J2eeServerTypeRegistryConfigModule.class);
		} catch (ConfigException e) {
			logger.log(Level.FATAL, "Getting/creating J2eeServerTypeRegistryConfigModule failed!", e);
			throw new ResourceException(e.getMessage());
		}

		try {
			cacheCfMod = config.createConfigModule(CacheCfMod.class);
		} catch (Exception e) {
			logger.error("Creating CacheCfMod failed!", e);
			throw new ResourceException(e.getMessage());
		}

		if (saveConfig) {
			try {
				config.save(false);
				// shall we really force all modules to be written here?
				// Probably not, after last config bugs are fixed.
				// I think I fixed the bug today ;-) Changed it to false. Marco.
			} catch (ConfigException e) {
				logger.fatal("Saving configuration failed!", e);
				throw new ResourceException(e.getMessage());
			}
		}

		// TODO make the IDGenerator configurable
		System.setProperty(IDGenerator.PROPERTY_KEY_ID_GENERATOR_CLASS, "org.nightlabs.jfire.idgenerator.IDGeneratorServer");
		// org.nightlabs.jfire.id.IDGeneratorServer comes from JFireBaseBean and thus is not known in this module


		String j2eeServerType = null;
		ServerCf localServerCf = mcf.getConfigModule().getLocalServer();
		if (localServerCf != null) {
			j2eeServerType = localServerCf.getJ2eeServerType();
		}
		if (j2eeServerType == null) {
			logger.warn("No configuration existing! Assuming that this is a 'jboss40x'. If you change the server type, you must restart!");
			j2eeServerType = Server.J2EESERVERTYPE_JBOSS40X; // TODO we assume that we're running on a jboss40x, but we should somehow allow the user to change this on the fly.
//			throw new ResourceException("JFireServerConfigModule: localServer.j2eeServerType is null! Check config!");
		}

		j2eeLocalServerCf = null;
		for (Iterator<J2eeLocalServer> it = j2eeServerTypeRegistryConfigModule.getJ2eeLocalServers().iterator(); it.hasNext(); ) {
			J2eeServerTypeRegistryConfigModule.J2eeLocalServer jls = it.next();
			if (j2eeServerType.equals(jls.getJ2eeServerType())) {
				j2eeLocalServerCf = jls;
				break;
			}
		}
		if (j2eeLocalServerCf == null)
			throw new ResourceException("JFireServerConfigModule: localServer.j2eeServerType: This serverType is not registered in the J2eeServerTypeRegistryConfigModule!");

		try {
			this.clRegistrarFactory = new CLRegistrarFactory(
					this.getConfig().createConfigModule(CLRegistryCfMod.class));
//			clRegistrarFactory.scan(); // TODO this should be done lazy. Only for a test here in constructor!
		} catch (Exception e) {
			logger.error("Creating CLRegistrarFactory failed!", e);
			throw new ResourceException(e.getMessage());
		}

		InitialContext initialContext = null;
		try {
			initialContext = new InitialContext();
		} catch (Exception e) {
			logger.error("Obtaining JNDI InitialContext failed!", e);
			throw new ResourceException(e.getMessage());
		}

		try {
			try {
				initialContext.createSubcontext("java:/jfire");
			} catch (NameAlreadyBoundException e) {
				// ignore
			}

			try {
				initialContext.createSubcontext("java:/jfire/system");
			} catch (NameAlreadyBoundException e) {
				// ignore
			}

			try {
				initialContext.createSubcontext("jfire");
			} catch (NameAlreadyBoundException e) {
				// ignore
			}

			try {
				initialContext.createSubcontext("jfire/system");
			} catch (NameAlreadyBoundException e) {
				// ignore
			}

			String rootOrganisationID = getJFireServerConfigModule().getRootOrganisation().getOrganisationID();
			try
			{
				initialContext.bind(Organisation.ROOT_ORGANISATION_ID_JNDI_NAME, rootOrganisationID);
			}
			catch (NameAlreadyBoundException e)
			{
				initialContext.rebind(Organisation.ROOT_ORGANISATION_ID_JNDI_NAME, rootOrganisationID);
			}

			try
			{
				initialContext.bind(JMSConnectionFactoryLookup.QUEUECF_JNDI_LINKNAME, "UIL2ConnectionFactory");
			}
			catch (NameAlreadyBoundException e)
			{
				initialContext.rebind(JMSConnectionFactoryLookup.QUEUECF_JNDI_LINKNAME, "UIL2ConnectionFactory");
			}

			try
			{
				initialContext.bind(JMSConnectionFactoryLookup.TOPICCF_JNDI_LINKNAME, "UIL2ConnectionFactory");
			}
			catch (NameAlreadyBoundException e)
			{
				initialContext.rebind(JMSConnectionFactoryLookup.TOPICCF_JNDI_LINKNAME, "UIL2ConnectionFactory");
			}

		} catch (Exception e) {
			logger.error("Binding some config settings into JNDI failed!", e);
			throw new ResourceException(e.getMessage());
		}

		J2EEAdapter j2EEAdapter;
		try {
			j2EEAdapter = getJ2EEVendorAdapter();
		} catch (J2EEAdapterException e) {
			throw new ResourceException(e);
		}
		try
		{
			try
			{
				initialContext.bind(J2EEAdapter.JNDI_NAME, j2EEAdapter);
			}
			catch (NameAlreadyBoundException nabe)
			{
				initialContext.rebind(J2EEAdapter.JNDI_NAME, j2EEAdapter);
			}
		}
		catch (Exception e) {
			logger.error("Binding J2EEAdapter into JNDI failed!", e);
			throw new ResourceException(e.getMessage());
		}

		try {
			SecurityReflector userResolver = j2EEAdapter.getSecurityReflector();
			if (userResolver == null)
				throw new NullPointerException("J2EEVendorAdapter "+j2EEAdapter.getClass()+".getSecurityReflector() returned null!");
			try
			{
				initialContext.bind(SecurityReflector.JNDI_NAME, userResolver);
			}
			catch (NameAlreadyBoundException e)
			{
				initialContext.rebind(SecurityReflector.JNDI_NAME, userResolver);
			}
		} catch (Exception e) {
			logger.error("Creating SecurityReflector and binding it into JNDI failed!", e);
			throw new ResourceException(e.getMessage());
		}

		try
		{
			JFireServerLocalLoginManager m = new JFireServerLocalLoginManager();
			try
			{
				initialContext.bind(JFireServerLocalLoginManager.JNDI_NAME, m);
			}
			catch (NameAlreadyBoundException e)
			{
				initialContext.rebind(JFireServerLocalLoginManager.JNDI_NAME, m);
			}
		}
		catch (Exception e) {
			logger.error("Creating JFireServerLocalLoginManager and binding it into JNDI failed!", e);
			throw new ResourceException(e.getMessage());
		}

		String property_CacheManagerFactoryCreate_key = CacheManagerFactory.class.getName() + ".create";
		String property_CacheManagerFactoryCreate_value = System.getProperty(property_CacheManagerFactoryCreate_key);
		if ("false".equals(property_CacheManagerFactoryCreate_value)) {
			logger.warn("The system property \"" + property_CacheManagerFactoryCreate_key + "\" has been set to \"" + property_CacheManagerFactoryCreate_value + "\"; the CacheManagerFactory will *not* be created!");
		}
		else {
			for (Iterator<OrganisationCf> it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ) {
				OrganisationCf organisation = it.next();
				String organisationID = organisation.getOrganisationID();

				try {
					new CacheManagerFactory(
							this, initialContext, organisation, cacheCfMod, mcf.getSysConfigDirectory()); // registers itself in JNDI
				} catch (Exception e) {
					logger.error("Creating CacheManagerFactory for organisation \""+organisationID+"\" failed!", e);
					throw new ResourceException(e.getMessage());
				}
			}
		}

		try {
			initialContext.close();
		} catch (Exception e) {
			logger.warn("Closing InitialContext failed!", e);
		}

		try {
			j2EEAdapter.registerNotificationListenerServerStarted(this);
		} catch (Exception e) {
			logger.error("Registering NotificationListener (for notification on server start) failed!", e);
//			throw new ResourceException(e.getMessage());
		}

//// Unfortunately this does not work, because the initial context is not yet existent, when
//// this is executed.
//		Thread roleImportThread = new Thread("roleImportThread") {
//			public void run() {
//				try {
//					InitialContext ctx = new InitialContext();
//			    UserTransaction ut = (UserTransaction)ctx.lookup("java:comp/ClientUserTransaction");
//			    boolean doCommit = false;
//			    ut.begin();
//			    try {
//						for (Iterator it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ) {
//							OrganisationCf org = (OrganisationCf)it.next();
//							try {
//								waitForPersistenceManager(org.getPersistenceManagerJNDIName()).close();
//								RoleImportSet roleImportSet = roleImport_prepare(org.getOrganisationID());
//								roleImport_commit(roleImportSet);
//							} catch (Exception x) {
//								LOGGER.warn("Role import failed for organisation \""+org.getOrganisationID()+"\"!", x);
//							}
//						}
//						doCommit = true;
//			    } finally {
//			    	if (doCommit) ut.commit();
//			    	else ut.rollback();
//			    }
//				} catch (Throwable x) {
//					LOGGER.error("roleImportThread.run had exception!", x);
//				}
//			}
//		};
//		roleImportThread.start();
	} // end constructor

	/**
	 * This method configures the server using the currently configured server configurator.
	 * @param delayMSec In case a reboot is necessary, the shutdown will be delayed by this time in milliseconds.
	 * @return Returns whether a reboot was necessary (and thus a shutdown was/will be initiated).
	 * @throws ServerConfigurationException If server configuration failed
	 */
	public boolean configureServerAndShutdownIfNecessary(final long delayMSec) throws ServerConfigurationException
	{
		boolean rebootRequired = ServerConfigurator.configureServer(getConfig());

		if (rebootRequired) {
			shuttingDown = true;

			logger.warn("*** REBOOT REQUIRED ***");
			logger.warn("*** REBOOT REQUIRED ***");
			logger.warn("*** REBOOT REQUIRED ***");

			logger.warn("*** REBOOT REQUIRED ***");
			logger.warn("*** REBOOT REQUIRED ***");
			logger.warn("*** REBOOT REQUIRED ***");

			logger.warn("*** REBOOT REQUIRED ***");
			logger.warn("*** REBOOT REQUIRED ***");
			logger.warn("*** REBOOT REQUIRED ***");

			logger.warn("The invoked Server Configurator indicates that the server needs to be rebooted! Hence, I will shutdown the server NOW!");
			logger.warn("If this is an error and prevents your JFire Server from starting up correctly, you must exchange the ServerConfigurator in the config module " + JFireServerConfigModule.class.getName());

			logger.warn("*** REBOOT REQUIRED ***");
			logger.warn("*** REBOOT REQUIRED ***");
			logger.warn("*** REBOOT REQUIRED ***");

			logger.warn("*** REBOOT REQUIRED ***");
			logger.warn("*** REBOOT REQUIRED ***");
			logger.warn("*** REBOOT REQUIRED ***");

			logger.warn("*** REBOOT REQUIRED ***");
			logger.warn("*** REBOOT REQUIRED ***");
			logger.warn("*** REBOOT REQUIRED ***");

			Thread thread = new Thread() {
				@Override
				public void run()
				{
					if (delayMSec > 0)
						try { Thread.sleep(delayMSec); } catch (InterruptedException ignore) { }

					try {
						getJ2EEVendorAdapter().reboot();
						logger.warn("*** REBOOT initiated ***");
					} catch (Throwable e) {
						logger.error("Shutting down server failed!", e);
					}
				}
			};
			thread.setDaemon(false);
			thread.start();
			return true;
		}
		return false;
	}

	/**
	 * The creation of an organisation is not allowed, before the datastore inits are run.
	 * If you, dear reader, believe that this is a problem, please tell me. Marco :-)
	 */
	private boolean createOrganisationAllowed = false;

	@Override
	public void serverStarted()
	{
		logger.info("Caught SERVER STARTED event!");

		try {
//			LoginContext loginContext = new LoginContext(
//						LoginData.DEFAULT_SECURITY_PROTOCOL,
//						new CallbackHandler() {
//							@Override
//							public void handle(Callback[] callbacks)
//							throws IOException, UnsupportedCallbackException
//							{
//								for (int i = 0; i < callbacks.length; ++i) {
//									Callback cb = callbacks[i];
//									if (cb instanceof NameCallback) {
//										((NameCallback)cb).setName(User.USER_ID_ANONYMOUS + User.SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID + Organisation.DEV_ORGANISATION_ID);
//									}
//									else if (cb instanceof PasswordCallback) {
//										((PasswordCallback)cb).setPassword(new char[0]);
//									}
//									else throw new UnsupportedCallbackException(cb);
//								}
//							}
//						});
//			loginContext.login();

			final InitialContext ctx = new InitialContext();
			try {
				if (configureServerAndShutdownIfNecessary(0))
					return;

				final ServerInitManager serverInitManager = new ServerInitManager(this, mcf, getJ2EEVendorAdapter());
				final OrganisationInitManager datastoreInitManager = new OrganisationInitManager(this, mcf, getJ2EEVendorAdapter());

				// do the server inits that are to be performed before the datastore inits
				logger.info("Performing early server inits...");
				serverInitManager.performEarlyInits(ctx);

				String asyncStartupString = System.getProperty(org.nightlabs.jfire.servermanager.JFireServerManagerFactory.class.getName() + ".asyncStartup");
				boolean asyncStartup = !Boolean.FALSE.toString().equals(asyncStartupString);
				if (logger.isDebugEnabled())
					logger.debug(org.nightlabs.jfire.servermanager.JFireServerManagerFactory.class.getName() + ".asyncStartup=" + asyncStartupString);

				if (!asyncStartup)
					logger.info(org.nightlabs.jfire.servermanager.JFireServerManagerFactory.class.getName() + ".asyncStartup is false! Initialising one organisation after the other (not parallel).");

				int initOrganisationThreadCount = mcf.getConfigModule().getJ2ee().getInitOrganisationOnStartupThreadCount();
				final Set<Runnable> unfinishedInitialisations = new HashSet<Runnable>();
				ThreadPoolExecutor threadPoolExecutor = asyncStartup ? new ThreadPoolExecutor(
						initOrganisationThreadCount, initOrganisationThreadCount,
						10L, TimeUnit.SECONDS,
						new LinkedBlockingQueue<Runnable>(),
						new ThreadFactory() {
							private int nextThreadGroupID = 0;
							private int nextThreadID = 0;

							@Override
							public synchronized Thread newThread(Runnable r)
							{
								ThreadGroup group = new ThreadGroup("InitOrgThreadGroup-" + (nextThreadGroupID++));
								Thread thread = new Thread(group, r);
								thread.setName("InitOrgThread-" + (nextThreadID++));
								return thread;
							}
						})
						: null;

				for (OrganisationCf org : new ArrayList<OrganisationCf>(organisationConfigModule.getOrganisations())) {
					final String organisationID = org.getOrganisationID();

//					// TODO DataNucleus WORKAROUND: force serialized startup of PMFs - there seems to be synchronization bug in loading the meta-data.
//					I think the bug is fixed. Thus, I commented out the following line. 2009-08-06. Marco.
//					getPersistenceManagerFactory(organisationID).getPersistenceManager().close();

					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							try {

								logger.info("Importing roles and rolegroups into organisation \""+organisationID+"\"...");
								try {

									UserTransaction userTransaction = getJ2EEVendorAdapter().getUserTransaction(ctx);
									boolean doCommit = false;
									userTransaction.begin();
									try {
										LoginContext loginContext = j2eeVendorAdapter.createLoginContext(
//										LoginContext loginContext = new LoginContext(
												LoginData.DEFAULT_SECURITY_PROTOCOL,
												new CallbackHandler() {
													@Override
													public void handle(Callback[] callbacks)
													throws IOException, UnsupportedCallbackException
													{
														for (int i = 0; i < callbacks.length; ++i) {
															Callback cb = callbacks[i];
															if (cb instanceof NameCallback) {
																((NameCallback)cb).setName(User.USER_ID_SYSTEM + User.SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID + organisationID);
															}
															else if (cb instanceof PasswordCallback) {
																((PasswordCallback)cb).setPassword(
																		jfireSecurity_createTempUserPassword(UserID.create(organisationID, User.USER_ID_SYSTEM)).toCharArray()
																);
															}
															else throw new UnsupportedCallbackException(cb);
														}
													}
												}
										);
										loginContext.login();
										try {

											RoleImportSet roleImportSet = roleImport_prepare(organisationID);
											roleImport_commit(roleImportSet, null);

										} finally {
											loginContext.logout();
										}

										doCommit = true;
									} finally {
										if (doCommit)
											userTransaction.commit();
										else
											userTransaction.rollback();
									}
									logger.info("Import of roles and rolegroups into organisation \""+organisationID+"\" done.");
								} catch (Exception x) {
									logger.error("Role import into organisation \""+organisationID+"\" failed!", x);
								}


								// register the cache's JDO-listeners in the PersistenceManagerFactory
								PersistenceManagerFactory pmf = null;
								try {
									CacheManagerFactory cmf = CacheManagerFactory.getCacheManagerFactory(ctx, organisationID);
									pmf = getPersistenceManagerFactory(organisationID);
									cmf.setupJdoCacheBridge(pmf);
								} catch (NameAlreadyBoundException e) {
									// ignore - might happen, if an organisation is created in an early-server-init
								} catch (Exception e) {
									logger.error("Setting up CacheManagerFactory for organisation \""+organisationID+"\" failed!", e);
								}

								if (pmf != null) {
									try {
										String createString = System.getProperty(PersistentNotificationManagerFactory.class.getName() + ".create");
										boolean create = !Boolean.FALSE.toString().equals(createString);
										if (logger.isDebugEnabled())
											logger.debug(PersistentNotificationManagerFactory.class.getName() + ".create=" + createString);

										if (!create)
											logger.info(PersistentNotificationManagerFactory.class.getName() + ".create is false! Will not create PersistentNotificationManagerFactory for organisation \"" + organisationID + "\"!");
										else {
											new PersistentNotificationManagerFactory(ctx, organisationID, JFireServerManagerFactoryImpl.this,
													getJ2EEVendorAdapter().getUserTransaction(ctx), pmf); // registers itself in JNDI
										}
									} catch (NameAlreadyBoundException e) {
										// ignore - might happen, if an organisation is created in an early-server-init
									} catch (Exception e) {
										logger.error("Creating PersistentNotificationManagerFactory for organisation \""+organisationID+"\" failed!", e);
									}

									logger.info("Initialising datastore of organisation \""+organisationID+"\"...");
									try {
										datastoreInitManager.initialiseOrganisation(
												JFireServerManagerFactoryImpl.this, mcf.getConfigModule().getLocalServer(), organisationID,
												jfireSecurity_createTempUserPassword(UserID.create(organisationID, User.USER_ID_SYSTEM))
										);

										logger.info("Datastore initialisation of organisation \""+organisationID+"\" done.");
									} catch (Exception x) {
										logger.error("Datastore initialisation of organisation \""+organisationID+"\" failed!", x);
									}
								} // if (pmf != null) {

							} finally {
								synchronized (unfinishedInitialisations) {
									unfinishedInitialisations.remove(this);
									unfinishedInitialisations.notifyAll();
								}
							}
						}
					};

					synchronized (unfinishedInitialisations) {
						unfinishedInitialisations.add(runnable);
					}
					if (asyncStartup)
						threadPoolExecutor.execute(runnable);
					else
						runnable.run();
				} // for (OrganisationCf org : organisationConfigModule.getOrganisations()) {

				if (asyncStartup) {
					synchronized (unfinishedInitialisations) {
						while (!unfinishedInitialisations.isEmpty()) {
							try {
								unfinishedInitialisations.wait(60000);
							} catch (InterruptedException x) { }
						}
					} // synchronized (unfinishedInitialisations) {

					threadPoolExecutor.shutdown();
				} // if (asyncStartup) {

				createOrganisationAllowed = true;

				// do the server inits that are to be performed after the datastore inits
				logger.info("Performing late server inits...");
				serverInitManager.performLateInits(ctx);
			} finally {
				ctx.close();
			}

			logger.info("*** JFireServer is up and running! ***");
			upAndRunning = true;

//			String shutdownAfterStartup = System.getProperty(JFireServerManagerFactory.class.getName() + ".shutdownAfterStartup");
//			if (Boolean.TRUE.toString().equals(shutdownAfterStartup)) {
//				try {
//					getJ2EEVendorAdapter().shutdown();
//				} catch (Throwable x) {
//					logger.error("shutdown via JavaEE-vendor-adapter failed!", x);
//				}
//			}

			if (logger.isDebugEnabled()) {
				logger.debug("System properties:");
				for (Map.Entry<?, ?> me : System.getProperties().entrySet()) {
					logger.debug("  * " + me.getKey() + " = " + me.getValue());
				}
			}

			ShutdownControlHandle shutdownControlHandle = shutdownAfterStartupManager.createShutdownControlHandle();
			shutdownAfterStartupManager.shutdown(shutdownControlHandle);
		} catch (Throwable x) {
			logger.fatal("Problem in serverStarted()!", x);
		}
	}

	private ShutdownAfterStartupManager shutdownAfterStartupManager = new ShutdownAfterStartupManager(this);

	protected ShutdownControlHandle shutdownAfterStartup_createShutdownControlHandle()
	{
		return shutdownAfterStartupManager.createShutdownControlHandle();
	}

	protected void shutdownAfterStartup_shutdown(ShutdownControlHandle shutdownControlHandle) {
		shutdownAfterStartupManager.shutdown(shutdownControlHandle);
	}

//	protected boolean shutdownAfterStartup_isActive(ShutdownControlHandle shutdownControlHandle) {
//		return shutdownAfterStartupManager.isActive(shutdownControlHandle);
//	}

	// **************************************
	// *** Methods from ConnectionFactory ***
	// **************************************
	@Override
	public Connection getConnection() throws ResourceException {
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": getConnection()");
		JFireServerManagerImpl ismi = (JFireServerManagerImpl)cm.allocateConnection(mcf, null);
		ismi.setJFireServerManagerFactory(this);
		return ismi;
	}

	@Override
	public Connection getConnection(ConnectionSpec cs) throws ResourceException {
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": getConnection(ConnectionSpec cs): cs = "+cs);
		return getConnection();
	}

	@Override
	public RecordFactory getRecordFactory() throws ResourceException {
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": getRecordFactory()");
		return null;
	}

	@Override
	public ResourceAdapterMetaData getMetaData() throws ResourceException {
		throw new ResourceException("NYI");
	}

	@Override
	public void setReference(Reference _ref) {
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": setReference(Reference ref): ref = "+_ref);
		this.ref = _ref;
	}

	@Override
	public Reference getReference() throws NamingException {
		return ref;
	}


	// ************************************************
	// *** Methods from JFireServerManagerFactory ***
	// ************************************************

	@Override
	public JFireServerManager getJFireServerManager()
	{
		try {
			return (JFireServerManager)getConnection();
		} catch (ResourceException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public JFireServerManager getJFireServerManager(JFirePrincipal jfirePrincipal)
	{
		try {
			JFireServerManager ism = (JFireServerManager)getConnection();
			if (jfirePrincipal != null)
				((JFireServerManagerImpl)ism).setJFirePrincipal(jfirePrincipal);
			return ism;
		} catch (ResourceException e) {
			throw new RuntimeException(e);
		}
	}

	// ************************************************
	// *** Methods executed by JFireServerManager ***
	// ************************************************

	protected boolean isNewServerNeedingSetup()
	{
		return mcf.getConfigModule().getLocalServer() == null;
	}

	/**
	 * @return Returns a clone of the internal config module.
	 */
	protected JFireServerConfigModule getJFireServerConfigModule()
	{
		JFireServerConfigModule cfMod = mcf.getConfigModule();
		cfMod.acquireReadLock();
		try {
			return (JFireServerConfigModule)cfMod.clone();
		} finally {
			cfMod.releaseLock();
		}
	}

	protected void setJFireServerConfigModule(JFireServerConfigModule cfMod)
	throws ConfigException
	{
		if (cfMod.getLocalServer() == null)
			throw new NullPointerException("localServer of config module must not be null!");

		if (cfMod.getDatabase() == null)
			throw new NullPointerException("database of config module must not be null!");

		if (cfMod.getJdo() == null)
			throw new NullPointerException("jdo of config module must not be null!");

		mcf.testConfiguration(cfMod);

		JFireServerConfigModule orgCfMod = mcf.getConfigModule();
		orgCfMod.acquireWriteLock();
		try {
			if (orgCfMod.getLocalServer() != null) {
				if (cfMod.getLocalServer().getServerID() == null)
					cfMod.getLocalServer().setServerID(orgCfMod.getLocalServer().getServerID());
				else if (!orgCfMod.getLocalServer().getServerID().equals(cfMod.getLocalServer().getServerID()))
					throw new IllegalArgumentException("Cannot change serverID after it has been set once!");
			}
			else
				if (cfMod.getLocalServer().getServerID() == null)
					throw new NullPointerException("localServer.serverID must not be null at first call to this method!");

			// ensure a reasonable SMTP-Config is set. // TODO shouldn't this code better be in the init method of the JFireServerConfigModule?! or in ManagedConnectionFactory.testConfiguration(...)? IMHO that's wrong here. Marco.
//			if (cfMod.getSmtp() == null) {
//				if (orgCfMod.getSmtp() == null) {
//					logger.warn("There are no SMTP settings set! Using fallback values. ", new NullPointerException());
//					SmtpMailServiceCf fallback = new SmtpMailServiceCf();
//					fallback.init();
//					cfMod.setSmtp(fallback);
//				} else {
//					cfMod.setSmtp(orgCfMod.getSmtp());
//				}
//			}
			// I think the above code (checking the SMTP-config) is not necessary, because it actually is already done in the init() method.

			try {
				BeanUtils.copyProperties(orgCfMod, cfMod);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e); // should never happen => RuntimeException
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e); // should never happen => RuntimeException
			}

		} finally {
			orgCfMod.releaseLock();
		}

		getConfig().save(true); // TODO force all modules to be written???

		try {
			InitialContext initialContext = new InitialContext();
			try {
				String newRootOrganisationID = cfMod.getRootOrganisation().getOrganisationID();
				String oldRootOrganisationID = Organisation.getRootOrganisationID(initialContext);
				if (!newRootOrganisationID.equals(oldRootOrganisationID)) {
					initialContext.rebind(Organisation.ROOT_ORGANISATION_ID_JNDI_NAME, newRootOrganisationID);
				}
			} finally {
				initialContext.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException(e); // should never happen => RuntimeException
		}
	}

	protected J2EEAdapter j2eeVendorAdapter = null;
	public synchronized J2EEAdapter getJ2EEVendorAdapter() throws J2EEAdapterException
	{
		if (j2eeVendorAdapter == null) {
			try {
				String j2eeVendorAdapterClassName = j2eeLocalServerCf.getJ2eeVendorAdapterClassName(); // mcf.getConfigModule().getJ2ee().getJ2eeVendorAdapterClassName();
				Class<?> j2eeVendorAdapterClass = Class.forName(j2eeVendorAdapterClassName);
				j2eeVendorAdapter = (J2EEAdapter)j2eeVendorAdapterClass.newInstance();
			} catch (Exception e) {
				logger.error("Creating JavaEE vendor adapter failed: " + e, e);
				throw new J2EEAdapterException("Error creating new J2EE vendor adapter", e);
			}
		}
		return j2eeVendorAdapter;
	}

	protected synchronized void j2ee_flushAuthenticationCache() throws J2EEAdapterException
	{
		getJ2EEVendorAdapter().flushAuthenticationCache();
	}

	protected RoleImportSet roleImport_prepare(String organisationID)
	{
		File startDir = new File(mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory());

		Set<String> roleIDs;
		try {
			roleIDs = getJ2EEVendorAdapter().getAllEjb3Roles();
		} catch (J2EEAdapterException e) {
			throw new RuntimeException(e);
		}

		JFireSecurityMan globalSecurityMan = new JFireSecurityMan(roleIDs);
		Map<String, Throwable> exceptions = new HashMap<String, Throwable>(); // key: File jar; value: Throwable exception
		roleImport_prepare_collect(startDir, globalSecurityMan, exceptions);
		globalSecurityMan.createFallbackRoleGroups(); // new here - was in roleImport_prepare_readJar before, but it's better to do it globally at the end.

		return new RoleImportSet(organisationID, globalSecurityMan, exceptions);
	}

//	private static class FileFilterDirectories implements FilenameFilter
//	{
//		@Override
//		public boolean accept(File dir, String name)
//		{
//			File f = new File(dir, name);
//			return f.isDirectory();
//		}
//	}
//	private static FileFilterDirectories fileFilterDirectories = null;
//
//	private static class FileFilterJARs implements FilenameFilter
//	{
//		@Override
//		public boolean accept(File dir, String name)
//		{
//			return name.endsWith(JAR_SUFFIX);
//		}
//	}
//	private static String JAR_SUFFIX = ".jar";
//	private static FileFilterJARs fileFilterJARs = null;

	private void roleImport_prepare_collect(File directory, JFireSecurityMan globalSecurityMan, final Map<String, Throwable> exceptions)
	{
		EARApplicationSet earApplicationSet;
		try {
			earApplicationSet = new EARApplicationSet(directory, EARModuleType.ejb);
		} catch (XMLReadException e) {
			throw new RuntimeException(e);
		}
		final Map<EARApplication, Map<String, EJBJarMan>> ear2jarName2ejbJarMan = new HashMap<EARApplication, Map<String,EJBJarMan>>();
		final Map<EARApplication, Map<String, JFireSecurityMan>> ear2jarName2securityMan = new HashMap<EARApplication, Map<String,JFireSecurityMan>>();

		// Look for all ejb-jar.xml files and create instances of EJBJarMan for each of them.
		earApplicationSet.handleJarEntries(
				new String[] { "META-INF/ejb-jar.xml" },
				new JarEntryHandler[] {
						new JarEntryHandler() {
							@Override
							public void handleJarEntry(EARApplication ear, String jarName, InputStream in) {
								try {
									Map<String, EJBJarMan> jarName2ejbJarMan = ear2jarName2ejbJarMan.get(ear);
									if (jarName2ejbJarMan == null) {
										jarName2ejbJarMan = new HashMap<String, EJBJarMan>();
										ear2jarName2ejbJarMan.put(ear, jarName2ejbJarMan);
									}

									EJBJarMan ejbJarMan = jarName2ejbJarMan.get(jarName);
									if (ejbJarMan != null)
										throw new IllegalStateException("Why the hell is there already an instance of EJBJarMan for ear=" + ear.getEar().getName() + " and jar=" + jarName + "?!");

									ejbJarMan = new EJBJarMan(jarName, in);
									jarName2ejbJarMan.put(jarName, ejbJarMan);
								} catch (Throwable t) {
									String jarIdentifier = ear.getEar().getAbsolutePath() + '#' + jarName;
									if (!exceptions.containsKey(jarIdentifier))
										exceptions.put(jarIdentifier, t);
								}
							}
						}
				}
		);

		// Search jfire-security.xml files and create JFireSecurityMan instances for each of them.
		earApplicationSet.handleJarEntries(
				new String[] { "META-INF/jfire-security.xml" },
				new JarEntryHandler[] {
						new JarEntryHandler() {
							@Override
							public void handleJarEntry(EARApplication ear, String jarName, InputStream in) {
								try {
									Map<String, EJBJarMan> jarName2ejbJarMan = ear2jarName2ejbJarMan.get(ear);
									if (jarName2ejbJarMan == null) {
										jarName2ejbJarMan = new HashMap<String, EJBJarMan>();
										ear2jarName2ejbJarMan.put(ear, jarName2ejbJarMan);
									}

									EJBJarMan ejbJarMan = jarName2ejbJarMan.get(jarName);
									if (ejbJarMan == null) {
										ejbJarMan = new EJBJarMan(jarName);
										jarName2ejbJarMan.put(jarName, ejbJarMan);
									}


									Map<String, JFireSecurityMan> jarName2securityMan = ear2jarName2securityMan.get(ear);
									if (jarName2securityMan == null) {
										jarName2securityMan = new HashMap<String, JFireSecurityMan>();
										ear2jarName2securityMan.put(ear, jarName2securityMan);
									}

									JFireSecurityMan jfireSecurityMan = jarName2securityMan.get(jarName);
									if (jfireSecurityMan != null)
										throw new IllegalStateException("Why the hell is there already an instance of JFireSecurityMan for ear=" + ear.getEar().getName() + " and jar=" + jarName + "?!");

									jfireSecurityMan = new JFireSecurityMan(ejbJarMan, in);
									jarName2securityMan.put(jarName, jfireSecurityMan);
								} catch (Throwable t) {
									String jarIdentifier = ear.getEar().getAbsolutePath() + '#' + jarName;
									if (!exceptions.containsKey(jarIdentifier))
										exceptions.put(jarIdentifier, t);
								}
							}
						}
				}
		);

		// Create JFireSecurityMan instances for every EJB-JAR, where there was only an ejb-jar.xml and no jfire-security.xml.
		for (Map.Entry<EARApplication, Map<String, EJBJarMan>> me1 : ear2jarName2ejbJarMan.entrySet()) {
			EARApplication ear = me1.getKey();
			for (Map.Entry<String, EJBJarMan> me2 : me1.getValue().entrySet()) {
				String jarName = me2.getKey();
				EJBJarMan ejbJarMan = me2.getValue();

				Map<String, JFireSecurityMan> jarName2securityMan = ear2jarName2securityMan.get(ear);
				if (jarName2securityMan == null) {
					jarName2securityMan = new HashMap<String, JFireSecurityMan>();
					ear2jarName2securityMan.put(ear, jarName2securityMan);
				}

				JFireSecurityMan jfireSecurityMan = jarName2securityMan.get(jarName);
				if (jfireSecurityMan == null) {
					try {
						jfireSecurityMan = new JFireSecurityMan(ejbJarMan);
					} catch (Throwable t) {
						String jarIdentifier = ear.getEar().getAbsolutePath() + '#' + jarName;
						if (!exceptions.containsKey(jarIdentifier))
							exceptions.put(jarIdentifier, t);
					}
					if (jfireSecurityMan != null)
						jarName2securityMan.put(jarName, jfireSecurityMan);
				}
			}
		}

		// Merge all JFireSecurityMan instances into the global one.
		for (Map.Entry<EARApplication, Map<String, JFireSecurityMan>> me1 : ear2jarName2securityMan.entrySet()) {
			for (Map.Entry<String, JFireSecurityMan> me2 : me1.getValue().entrySet()) {
				JFireSecurityMan securityMan = me2.getValue();
				globalSecurityMan.mergeSecurityMan(securityMan);
			}
		}

//		if (fileFilterDirectories == null)
//			fileFilterDirectories = new FileFilterDirectories();
//		String[] directories = directory.list(fileFilterDirectories);
//		if (directories != null) {
//			for (int i = 0; i < directories.length; ++i)
//				roleImport_prepare_collect(new File(directory, directories[i]), globalSecurityMan, exceptions);
//		} // if (directories != null) {
//
//		if (fileFilterJARs == null)
//			fileFilterJARs = new FileFilterJARs();
//		String[] jars = directory.list(fileFilterJARs);
//
//		if (jars != null) {
//			for (int i = 0; i < jars.length; ++i) {
//				File jar = new File(directory, jars[i]);
//				try {
//					JarFile jf = new JarFile(jar, true);
//					try {
//						roleImport_prepare_readJar(globalSecurityMan, jar, jf);
//					} finally {
//						jf.close();
//					}
//				} catch (Exception x) {
//					String jarFileName;
//					try {
//						jarFileName = jar.getCanonicalPath();
//						logger.warn("Processing Jar \""+jarFileName+"\" failed!", x);
//					} catch (IOException e) {
//						jarFileName = jar.getPath();
//						logger.warn("Processing Jar \""+jarFileName+"\" failed!", x);
//						logger.warn("Getting canonical path for \""+jarFileName+"\" failed!", e);
//					}
//					exceptions.put(jarFileName, x);
//				}
//			}
//		} // if (jars != null) {
	}

//	private void roleImport_prepare_readJar(JFireSecurityMan globalSecurityMan, File jar, JarFile jf)
//		throws SAXException, IOException, XMLReadException
//	{
//		JarEntry ejbJarXML = jf.getJarEntry("META-INF/ejb-jar.xml");
//		EJBJarMan ejbJarMan;
//		if (ejbJarXML == null) {
//			logger.info("Jar \""+jar.getCanonicalPath()+"\" does not contain \"META-INF/ejb-jar.xml\"!");
//			ejbJarMan = new EJBJarMan(jar.getName());
//		}
//		else {
//			if(logger.isDebugEnabled()) {
//				logger.debug("*****************************************************************");
//				logger.debug("Jar \""+jar.getCanonicalPath()+"\": ejb-jar.xml:");
//			}
//			InputStream in = jf.getInputStream(ejbJarXML);
//			try {
//				ejbJarMan = new EJBJarMan(jar.getName(), in);
//				if(logger.isDebugEnabled()) {
//					for (Iterator<RoleDef> it = ejbJarMan.getRoles().iterator(); it.hasNext(); ) {
//						RoleDef roleDef = it.next();
//						logger.debug("roleDef.roleID = "+roleDef.getRoleID());
//					}
//				}
//			} finally {
//				in.close();
//			}
//			if(logger.isDebugEnabled())
//				logger.debug("*****************************************************************");
//		}
//
//		JarEntry roleGroupXML = jf.getJarEntry("META-INF/jfire-security.xml");
//		JFireSecurityMan securityMan;
//		if (roleGroupXML == null) {
//			logger.info("Jar \""+jar.getCanonicalPath()+"\" does not contain \"META-INF/jfire-security.xml\"!");
//			securityMan = new JFireSecurityMan(ejbJarMan);
//		}
//		else {
//			if(logger.isDebugEnabled()) {
//				logger.debug("*****************************************************************");
//				logger.debug("Jar \""+jar.getCanonicalPath()+"\": jfire-security.xml:");
//			}
//			InputStream in = jf.getInputStream(roleGroupXML);
//			try {
//				securityMan = new JFireSecurityMan(ejbJarMan, in);
//				if(logger.isDebugEnabled()) {
//					for (RoleGroupDef roleGroupDef : securityMan.getRoleGroups().values()) {
//						logger.debug("roleGroupDef.roleGroupID = "+roleGroupDef.getRoleGroupID());
//						for (String includedRoleGroupID : roleGroupDef.getIncludedRoleGroupIDs()) {
//							logger.debug("  includedRoleGroupID = "+includedRoleGroupID);
//						}
//						for (String roleID : roleGroupDef.getRoleIDs()) {
//							logger.debug("  roleID = "+roleID);
//						}
//					}
//				}
//			} finally {
//				in.close();
//			}
//			if(logger.isDebugEnabled())
//				logger.debug("*****************************************************************");
//		}
////		securityMan.createFallbackRoleGroups(); // We create the fallback groups only at the end (globally), which prevents fallbacks to be created for roles that are declared in a jfire-security.xml of another EJB-jar.
//		globalSecurityMan.mergeSecurityMan(securityMan);
//	}

	/**
	 * @param roleImportSet
	 * @param pm can be <tt>null</tt>. If <tt>null</tt>, it will be obtained according to <tt>roleImportSet.getOrganisationID()</tt>.
	 */
	protected void roleImport_commit(RoleImportSet roleImportSet, PersistenceManager pm)
	{
		if (roleImportSet.getOrganisationID() == null)
			throw new IllegalArgumentException("roleImportSet.organisationID is null! Use roleImport_prepare(...) to generate a roleImportSet!");
		JFireSecurityMan securityMan = roleImportSet.getSecurityMan();
		securityMan.resolve(); // this is very likely not yet done, since we don't have a UI anymore

		if (!roleImportSet.getJarExceptions().isEmpty())
			logger.warn("roleImportSet.jarExceptions is not empty! You should execute roleImportSet.clearJarExceptions()!", new Exception("roleImportSet.jarExceptions is not empty."));

		boolean localPM = pm == null;

		if (localPM)
			pm = getPersistenceManager(roleImportSet.getOrganisationID());
		try {
			boolean successful = false;
			SecurityChangeController.beginChanging();
			try {

//				if (!localPM) { // I think it's a good idea to check this on every start-up => commented the "if" out
				// check whether PM datastore matches organisationID
				String datastoreOrgaID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
				if (!datastoreOrgaID.equals(roleImportSet.getOrganisationID()))
					throw new IllegalArgumentException("Parameter pm does not match organisationID of given roleImportSet!");
//				}

				// Find out whether any RoleGroup disappeared. This means a module has been undeployed and we need to backup the data
				// in order to restore it if the RoleGroup re-appears (if the module is re-deployed).
				Set<String> newRoleGroupIDs = securityMan.getRoleGroups().keySet();
				Set<String> oldRoleGroupIDs = new HashSet<String>();
				for (Iterator<RoleGroup> it = pm.getExtent(RoleGroup.class).iterator(); it.hasNext(); ) {
					RoleGroup roleGroup = it.next();
					oldRoleGroupIDs.add(roleGroup.getRoleGroupID());
					if (!newRoleGroupIDs.contains(roleGroup.getRoleGroupID())) {
						// the role-group is about to disappear => backup

						// in order to prevent primary key violations we make sure there is no record for this role-group
						for (UndeployedRoleGroupAuthorityUserRecord r : UndeployedRoleGroupAuthorityUserRecord.getUndeployedRoleGroupAuthorityUserRecordForRoleGroup(pm, roleGroup)) {
							pm.deletePersistent(r);
						}
						pm.flush(); // ensure, the deletions are pushed to the datastore

						UndeployedRoleGroupAuthorityUserRecord.createRecordsForRoleGroup(pm, roleGroup);
					}
				}

				AuthorityType authorityType_organisation = (AuthorityType) pm.getObjectById(AuthorityType.AUTHORITY_TYPE_ID_ORGANISATION);

				// create/update AuthorityType JDO objects (with the data in the securityMan). Note, that this already creates/updates role-groups and roles!
				for (AuthorityTypeDef authorityTypeDef : securityMan.getAuthorityTypes().values()) {
					authorityTypeDef.updateAuthorityType(
							pm,
							!authorityType_organisation.getAuthorityTypeID().equals(authorityTypeDef.getAuthorityTypeID())
					);
				}

				// create/update RoleGroup JDO objects (with the data in the securityMan)
				for (RoleGroupDef roleGroupDef : securityMan.getRoleGroups().values()) {
					RoleGroup roleGroupJDO = roleGroupDef.updateRoleGroup(pm);
					authorityType_organisation.addRoleGroup(roleGroupJDO);
				}

				// delete all roles that are not existing anymore
				{
					Set<String> currentRoleIDs = securityMan.getRoles().keySet();
					Query q = pm.newQuery(Role.class);
					pm.flush();
					for (Object o : new HashSet<Object>((Collection<?>)q.execute())) {
						Role role = (Role) o;
						if (currentRoleIDs.contains(role.getRoleID()))
							continue;

						for (RoleGroup roleGroup : new HashSet<RoleGroup>(role.getRoleGroups()))
							roleGroup.removeRole(role);

						pm.deletePersistent(role);
						pm.flush();
					}
				}

				// delete all role-groups that don't exist anymore and restore assignments to users in case a role-group re-appeared (due to re-deployment)
				{
					Collection<RoleGroup> roleGroups = CollectionUtil.castCollection((Collection<?>)pm.newQuery(RoleGroup.class).execute());
					roleGroups = new HashSet<RoleGroup>(roleGroups);
					for (Iterator<RoleGroup> it = roleGroups.iterator(); it.hasNext(); ) {
						RoleGroup roleGroup = it.next();
						if (!newRoleGroupIDs.contains(roleGroup.getRoleGroupID())) {
//							Query q2 = pm.newQuery(AuthorityType.class);
//							q2.setFilter("this.roleGroups.contains(:roleGroup)");
//							Collection<?> c2 = (Collection<?>) q2.execute(roleGroup);
//							for (Object o2 : c2) {
//							AuthorityType authorityType = (AuthorityType) o2;
//							authorityType.removeRoleGroup(roleGroup);
//							}

							pm.deletePersistent(roleGroup);
							pm.flush();
						} // if (!newRoleGroupIDs.contains(roleGroup.getRoleGroupID()))

						if (!oldRoleGroupIDs.contains(roleGroup.getRoleGroupID())) {
							// redeployed => restore assignments
							for (UndeployedRoleGroupAuthorityUserRecord r : UndeployedRoleGroupAuthorityUserRecord.getUndeployedRoleGroupAuthorityUserRecordForRoleGroup(pm, roleGroup)) {
								if (r.getAuthority() != null && r.getAuthorizedObject() != null) { // in case an authority or a group has been deleted (if this is ever possible)
									RoleGroupRef roleGroupRef = r.getAuthority().createRoleGroupRef(roleGroup);
									AuthorizedObjectRef authorizedObjectRef = r.getAuthority().createAuthorizedObjectRef(r.getAuthorizedObject());
									authorizedObjectRef.addRoleGroupRef(roleGroupRef);
								}
								pm.deletePersistent(r);
								pm.flush();
							}
						}
					}
				}

				successful = true;
			} finally {
				SecurityChangeController.endChanging(successful);
			}
		} finally {
			if (localPM)
				pm.close();
		}
	}

	private transient Object createOrganisation_mutex = new Object();

	/**
	 * This method generates a database-name out of the organisationID. Therefore,
	 * it replaces all characters which are not allowed in a database name by '_'.
	 * <p>
	 * <b>Warning:</b> This method allows name clashes, because e.g. both "a.b" and "a-b"
	 * are translated to "a_b".
	 * </p>
	 *
	 * @param organisationID The organisationID to be translated.
	 * @return the database name resulting from the given <code>organisationID</code>.
	 */
	protected String createDatabaseName(String organisationID)
	{
		StringBuffer databaseName = new StringBuffer((int) (1.5 * organisationID.length()));

		databaseName.append(organisationID.replaceAll("[^A-Za-z0-9_]", "_"));

// the following alternative code prevents name clashes. it translates all non-allowed characters are into their
// hex-value with the prefix "_" and the suffix "_".
//
//		for (char c : organisationID.toCharArray()) {
//			if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || ('0' <= c && c <= '9'))
//				databaseName.append(c);
//			else {
//				databaseName.append('_');
//				databaseName.append(ObjectIDUtil.longObjectIDFieldToString(c));
//				databaseName.append('_');
//			}
//		}

//		if (appendDatabasePrefixAndSuffix) {
			DatabaseCf dbCf = mcf.getConfigModule().getDatabase();
			databaseName.insert(0, dbCf.getDatabasePrefix());
			databaseName.append(dbCf.getDatabaseSuffix());
//		}

		return databaseName.toString();
	}

	private Map<CreateOrganisationProgressID, CreateOrganisationProgress> createOrganisationProgressMap = Collections.synchronizedMap(new HashMap<CreateOrganisationProgressID, CreateOrganisationProgress>());
	private CreateOrganisationProgressID createOrganisationProgressID = null;
	private transient Object createOrganisationProgressID_mutex = new Object();

	// TODO this method should already check all parameter - simply as much as possible in order reduce the possibility of a later failure.
	protected CreateOrganisationProgressID createOrganisationAsync(
			final String organisationID,
			final String organisationName, final String userID, final String password, final boolean isServerAdmin)
	throws BusyCreatingOrganisationException
	{
		synchronized (createOrganisationProgressID_mutex) {
			if (createOrganisationProgressID != null) {
				String busyOrganisationID = createOrganisationProgressMap.get(createOrganisationProgressID).getOrganisationID();
				throw new BusyCreatingOrganisationException(organisationID, CollectionUtil.array2HashSet(new String[] { busyOrganisationID }));
			}

			final CreateOrganisationProgress createOrganisationProgress = new CreateOrganisationProgress(organisationID);
			createOrganisationProgressMap.put(createOrganisationProgress.getCreateOrganisationProgressID(), createOrganisationProgress);
			createOrganisationProgressID = createOrganisationProgress.getCreateOrganisationProgressID();

			Thread thread = new Thread() {
				@Override
				public void run()
				{
					try {
						createOrganisation(createOrganisationProgress, organisationID, organisationName, userID, password, isServerAdmin);
					} catch (Throwable e) {
						logger.error("createOrganisationAsync.Thread.run: creating organisation \"" + organisationID + "\" failed!", e);
					}
				}
			};
			thread.start();

			return createOrganisationProgress.getCreateOrganisationProgressID();
		}
	}

	protected CreateOrganisationProgress getCreateOrganisationProgress(CreateOrganisationProgressID createOrganisationProgressID)
	{
		return createOrganisationProgressMap.get(createOrganisationProgressID);
	}

	protected void createOrganisationProgress_addCreateOrganisationStatus(
			CreateOrganisationProgressID createOrganisationProgressID, CreateOrganisationStatus createOrganisationStatus)
	{
		CreateOrganisationProgress createOrganisationProgress = createOrganisationProgressMap.get(createOrganisationProgressID);
		if (createOrganisationProgress == null)
			throw new IllegalArgumentException("No CreateOrganisationProgress known with this id: " + createOrganisationProgressID);

		createOrganisationProgress.addCreateOrganisationStatus(createOrganisationStatus);
	}

	/**
	 * This method creates a new organisation. What exactly happens, is documented in our wiki:
	 * https://www.jfire.org/modules/phpwiki/index.php/NewOrganisationCreation
	 * @param createOrganisationProgress an instance of {@link CreateOrganisationProgress} in order to track the status.
	 * @param organisationID The ID of the new organsitation, which must not be <code>null</code>. Example: "RioDeJaneiro.NightLabs.org"
	 * @param organisationName The "human" name of the organisation. Example: "NightLabs GmbH, Rio de Janeiro"
	 * @param userID The userID of the first user to be created. This will be the new organisation's administrator.
	 * @param password The password of the organisation's first user.
	 * @param isServerAdmin Whether the organisation's admin will have server-administrator privileges. This must be <tt>true</tt> if you create the first organisation on a server.
	 * @throws CreateOrganisationException If organisation creation failed
	 * @throws OrganisationInitException If organisation initialization failed
	 */
	protected void createOrganisation(
			CreateOrganisationProgress createOrganisationProgress, String organisationID,
			String organisationName, String userID, String password, boolean isServerAdmin) throws OrganisationInitException, CreateOrganisationException
//			String masterOrganisationID
//			)
	{
		if (!createOrganisationAllowed)
			throw new IllegalStateException("This method cannot be called yet. The creation of organisations is not allowed, before the datastore inits are run. If you get this exception in an early-server-init, you should switch to a late-server-init.");

		// check the parameters (only some here - some will be checked below)
		if (createOrganisationProgress == null)
			throw new IllegalArgumentException("createOrganisationProgress must not be null!");

		if (organisationID == null)
			throw new IllegalArgumentException("organisationID must not be null!");

		if ("".equals(organisationID))
			throw new IllegalArgumentException("organisationID must not be an empty string!");

		if (organisationID.indexOf('.') < 0)
			throw new IllegalArgumentException("organisationID is invalid! Must have domain-style form (e.g. \"jfire.nightlabs.de\")!");

		if (!Organisation.isValidOrganisationID(organisationID))
			throw new IllegalArgumentException("organisationID is not valid! Make sure it does not contain special characters. It should have a domain-style form!");

		if (organisationID.length() > 50)
			throw new IllegalArgumentException("organisationID has "+organisationID.length()+" chars and is too long! Maximum is 50 characters.");
//TODO Though the database definition currently allows 100 chars, we'll probably have to reduce it to 50 because of
//primary key constraints (max 1024 bytes with InnoDB) and the fact that MySQL uses 3 bytes per char when activating
//UTF8!

		if (organisationName == null)
			throw new IllegalArgumentException("organisationName must not be null!");

		if ("".equals(organisationName))
			throw new IllegalArgumentException("organisationName must not be an empty string!");

		if (!organisationID.equals(createOrganisationProgress.getOrganisationID()))
			throw new IllegalArgumentException("organisationID does not match createOrganisationProgress.getOrganisationID()!");

		OrganisationInitManager datastoreInitManager;
		try {
			datastoreInitManager = new OrganisationInitManager(this, mcf, getJ2EEVendorAdapter());
		} catch (J2EEAdapterException e) {
			throw new OrganisationInitException(e);
		}

		// the steps before OrganisationInit are defined in org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStep
		int stepsBeforeDatastoreInit = 10;
		int stepsDuringDatastoreInit = 2 * datastoreInitManager.getInits().size(); // 2 * because we track begin and end

		createOrganisationProgress.setStepsTotal(stepsBeforeDatastoreInit + stepsDuringDatastoreInit);

		try { // finally will clear this.createOrganisationProgressID, if it matches our current one
			synchronized (createOrganisation_mutex) { // TODO this is not nice and might cause the below error in extremely rare situations (because between this line and the next, createOrganisationAsync might be called)
				synchronized (createOrganisationProgressID_mutex) {
					if (createOrganisationProgressID != null && !createOrganisationProgressID.equals(createOrganisationProgress.getCreateOrganisationProgressID())) {
						String busyOrganisationID = createOrganisationProgressMap.get(createOrganisationProgressID).getOrganisationID();
						BusyCreatingOrganisationException x = new BusyCreatingOrganisationException(organisationID, CollectionUtil.array2HashSet(new String[] { busyOrganisationID }));
						logger.error("THIS SHOULD NEVER HAPPEN!", x);
						throw new CreateOrganisationException(x);
					}

					createOrganisationProgressMap.put(createOrganisationProgress.getCreateOrganisationProgressID(), createOrganisationProgress);
					createOrganisationProgressID = createOrganisationProgress.getCreateOrganisationProgressID();
				}

				try {
					// check the parameters (only some here - some are already checked above)

					if (userID == null)
						throw new IllegalArgumentException("userID must not be null!");

					if ("".equals(userID))
						throw new IllegalArgumentException("userID must not be an empty string!");

					if (!ObjectIDUtil.isValidIDString(userID))
						throw new IllegalArgumentException("userID is not a valid ID! Make sure it does not contain special characters!");

					if (userID.length() > 50)
						throw new IllegalArgumentException("userID has "+userID.length()+" chars and is too long! Maximum is 50 characters.");

					if (password == null)
						throw new IllegalArgumentException("password must NOT be null!");

					if (password.length() < 4)
						throw new IllegalArgumentException("password is too short! At least 4 characters are required! At least 8 characters are recommended!");

					if (!UserLocal.isValidPassword(password))
						throw new IllegalArgumentException("password is not valid!");

					if (isNewServerNeedingSetup())
						throw new IllegalStateException("This server is not yet set up! Please complete the basic setup before creating organisations!");

					if (!isServerAdmin && organisationConfigModule.getOrganisations().isEmpty())
						throw new NoServerAdminException(
								"You create the first organisation, hence 'isServerAdmin' must be true! " +
								"Otherwise, you would end up locked out, without any possibility to " +
						"create another organisation or change the server-configuration.");

					// Check whether another organisation with the same name already exists.
					// Unfortunately, there is currently no possibility to check in the whole
					// network, thus we check only locally.
					Map<String, OrganisationCf> organisationCfsCloned = getOrganisationCfsCloned();
					if (organisationCfsCloned.get(organisationID) != null)
						throw new DuplicateOrganisationException("An organisation with the name \""+organisationID+"\" already exists on this server!");

					//boolean creatingFirstOrganisation = isOrganisationCfsEmpty();

					InitialContext initialContext = new InitialContext();
					try {
						if (Organisation.hasRootOrganisation(initialContext)) {
							// TODO we now have root-organisation-support, hence the root-organisation should be asked, whether the given organisationID is really unique.
							// The root-organisation should block the requested organisationID for about 1 hour. It should assign it (for 1 hour) to this server,
							// so that no other server can simultaneously create an organisation with the same id, but this server can try it again, if it fails.

//							throw new DuplicateOrganisationException("An organisation with the name \""+organisationID+"\" already exists!");
						}

//						TransactionManager transactionManager = getJ2EEVendorAdapter().getTransactionManager(ctx);
						File jdoConfigDir = null;
						DatabaseAdapter databaseAdapter = null;
						boolean dropDatabase = false; // will be set true, AFTER the databaseAdapter has really created the database - this prevents a database to be dropped that was already previously existing
						OrganisationCf organisationCf = null;
						boolean doCommit = false;
//						transactionManager.begin();
						try {

							DatabaseCf dbCf = mcf.getConfigModule().getDatabase();
							JDOCf jdoCf = mcf.getConfigModule().getJdo();

							try {
								Class.forName(dbCf.getDatabaseDriverName_noTx());
							} catch (ClassNotFoundException e) {
								throw new ConfigException("Database driver class (no-tx) \""+dbCf.getDatabaseDriverName_noTx()+"\" could not be found!", e);
							}
							try {
								Class.forName(dbCf.getDatabaseDriverName_localTx());
							} catch (ClassNotFoundException e) {
								throw new ConfigException("Database driver class (local-tx) \""+dbCf.getDatabaseDriverName_localTx()+"\" could not be found!", e);
							}
							try {
								Class.forName(dbCf.getDatabaseDriverName_xa());
							} catch (ClassNotFoundException e) {
								throw new ConfigException("Database driver class (xa) \""+dbCf.getDatabaseDriverName_xa()+"\" could not be found!", e);
							}

							// create database
							String databaseName = createDatabaseName(organisationID);
							String dbURL = dbCf.getDatabaseURL(databaseName);

							createOrganisationProgress.addCreateOrganisationStatus(
									new CreateOrganisationStatus(
											CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_createDatabase_begin,
											databaseName, dbURL));

							databaseAdapter = dbCf.instantiateDatabaseAdapter();

							try {
								databaseAdapter.createDatabase(mcf.getConfigModule(), dbURL);
								dropDatabase = true;
							} catch (Throwable x) {
								throw new CreateOrganisationException("Creating database with DatabaseAdapter \"" + databaseAdapter.getClass().getName() + "\" failed!", x);
							}
							createOrganisationProgress.addCreateOrganisationStatus(
									new CreateOrganisationStatus(
											CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_createDatabase_end,
											databaseName, dbURL));

							createOrganisationProgress.addCreateOrganisationStatus(
									new CreateOrganisationStatus(
											CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_deployJDO_begin,
											databaseName, dbURL));

							jdoConfigDir = new File(jdoCf.getJdoConfigDirectory(organisationID)).getAbsoluteFile();
							File datasourceDSXML = new File(jdoConfigDir, dbCf.getDatasourceConfigFile(organisationID));
							File jdoDSXML = new File(jdoConfigDir, jdoCf.getJdoDeploymentDescriptorFile(organisationID));

							String persistenceConfig = jdoCf.getJdoPersistenceConfigurationFile(organisationID);
							File jdoPersistenceConfigurationFile = "".equals(persistenceConfig) ? null : new File(jdoConfigDir, persistenceConfig);
							String persistenceConfigTemplate = jdoCf.getJdoPersistenceConfigurationTemplateFile();
							File jdoPersistenceConfigurationTemplateFile = "".equals(persistenceConfigTemplate) ? null : new File(persistenceConfigTemplate);

							// If the peristenceConfigFile is configured, deploy it BEFORE the actual deployment descriptor
							// in order to ensure it exists, when the PMF is set up by the JavaEE server.
							if (jdoPersistenceConfigurationFile != null && jdoPersistenceConfigurationTemplateFile != null) {
								createDeploymentDescriptor(
										organisationID,
										jdoPersistenceConfigurationFile,
										jdoPersistenceConfigurationTemplateFile,
										null,
										DeployOverwriteBehaviour.EXCEPTION);
							}

							// creating deployment descriptor for datasource
							createDeploymentDescriptor(organisationID, datasourceDSXML,
									new File(dbCf.getDatasourceTemplateDSXMLFile()), null, DeployOverwriteBehaviour.EXCEPTION);

							// creating deployment descriptor for JDO PersistenceManagerFactory
							createDeploymentDescriptor(organisationID, jdoDSXML,
									new File(jdoCf.getJdoDeploymentDescriptorTemplateFile()), null, DeployOverwriteBehaviour.EXCEPTION);

							organisationCf = organisationConfigModule.addOrganisation(
									organisationID, organisationName);

							if (userID != null && isServerAdmin) organisationCf.addServerAdmin(userID);
							resetOrganisationCfs();
							try {
								getConfig().save(true); // TODO really force all modules to be written???
							} catch (ConfigException e) {
								logger.fatal("Saving config failed!", e);
							}
							logger.info("Empty organisation \""+organisationID+"\" (\""+organisationName+"\") has been created. Waiting for deployment...");

							{
								PersistenceManagerFactory pmf = null;
								// Now, we need to wait until the deployment of the x-ds.xml is complete and our
								// jdo persistencemanager is existing in JNDI.
								int tryCount = createOrganisationConfigModule.getWaitForPersistenceManager_tryCount();

								int tryNr = 0;
								while (pmf == null) {
									++tryNr;
									try {
										pmf = waitForPersistenceManagerFactory(OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE + organisationID); // org.getPersistenceManagerFactoryJNDIName());
									} catch (PersistenceManagerFactoryWaitException x) {
										if (tryNr >= tryCount) throw x;

										logger.info("Obtaining PersistenceManagerFactory failed! Touching jdo-ds-file and its directory and trying it again...");
										long now = System.currentTimeMillis();
										datasourceDSXML.setLastModified(now);
										jdoDSXML.setLastModified(now);
										jdoConfigDir.setLastModified(now);
									}
								}
								logger.info("PersistenceManagerFactory of organisation \""+organisationID+"\" (\""+organisationName+"\") has been deployed.");
							}

							createOrganisationProgress.addCreateOrganisationStatus(
									new CreateOrganisationStatus(
											CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_deployJDO_end,
											databaseName, dbURL));

							int tryCount = 0;
							boolean successful = false;
							while (!successful) {
								try {
									// populating essential data (Server, Organisation, User etc.) via OrganisationManagerBean.
									// we cannot reference the classes directly, because the project JFireBaseBean is dependent on JFireServerManager.
									// therefore, we reference it via the names.
									ServerCf localServerCf = mcf.getConfigModule().getLocalServer();
									UserID systemUserID = UserID.create(organisationID, User.USER_ID_SYSTEM);
									Properties props = InvokeUtil.getInitialContextProperties(
											this, systemUserID,
											jfireSecurity_createTempUserPassword(systemUserID));
									InitialContext initCtx = new InitialContext(props);
									try {
//										Object bean = InvokeUtil.createBean(initCtx, "jfire/ejb/JFireBaseBean/OrganisationManager");
										Object bean = initCtx.lookup(InvokeUtil.JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE + "org.nightlabs.jfire.organisation.OrganisationManagerRemote");
										Method beanMethod = bean.getClass().getMethod(
												"internalInitializeEmptyOrganisation",
												new Class[] { CreateOrganisationProgressID.class, ServerCf.class, OrganisationCf.class, String.class, String.class }
										);
										beanMethod.invoke(bean, new Object[] { createOrganisationProgress.getCreateOrganisationProgressID(), localServerCf, organisationCf, userID, password});
//										InvokeUtil.removeBean(bean);
									} finally {
										initCtx.close();
									}
									successful = true;
								} catch (Exception x) {
									if (++tryCount > 2)
										throw x;
									else
										logger.warn("Calling OrganisationManager.internalInitializeEmptyOrganisation(...) failed! Will retry again.", x);
								}
							}

							// there has been a role import, hence we need to flush the cache
							// (actually, it would be sufficient to flush it for the new organisation only, but there's no API yet and this doesn't harm)
							jfireSecurity_flushCache();

							// Because flushing the authentication cache causes trouble to currently logged in
							// clients, we only do that if we are creating the first organisation of a new server.
							// ***
							// it seems, the problem described above doesn't exist anymore. but in case it pops up again,
							// we need to uncomment the following line again
//							if (creatingFirstOrganisation)
							j2ee_flushAuthenticationCache();

							// create the CacheManagerFactory for the new organisation
							try {
								CacheManagerFactory cmf = new CacheManagerFactory(
										this, initialContext, organisationCf, cacheCfMod, mcf.getSysConfigDirectory()); // registers itself in JNDI

								// register the cache's JDO-listeners in the PersistenceManagerFactory
								PersistenceManagerFactory pmf = getPersistenceManagerFactory(organisationID);
								cmf.setupJdoCacheBridge(pmf);

//								new OrganisationSyncManagerFactory(ctx, organisationID,
//								getJ2EEVendorAdapter().getTransactionManager(ctx), pmf); // registers itself in JNDI

								String createString = System.getProperty(PersistentNotificationManagerFactory.class.getName() + ".create");
								boolean create = !Boolean.FALSE.toString().equals(createString);
								if (logger.isDebugEnabled())
									logger.debug(PersistentNotificationManagerFactory.class.getName() + ".create=" + createString);

								if (!create)
									logger.info(PersistentNotificationManagerFactory.class.getName() + ".create is false! Will not create PersistentNotificationManagerFactory for organisation \"" + organisationID + "\"!");
								else {
									new PersistentNotificationManagerFactory(initialContext, organisationID, this,
											getJ2EEVendorAdapter().getUserTransaction(initialContext), pmf); // registers itself in JNDI
								}
							} catch (Exception e) {
								logger.error("Creating CacheManagerFactory or PersistentNotificationManagerFactory for organisation \""+organisationID+"\" failed!", e);
								throw new ResourceException(e.getMessage());
							}

							doCommit = true;
						} finally {
							if (doCommit) {
//								transactionManager.commit();
							}
							else {
//								try {
//								transactionManager.rollback();
//								} catch (Throwable t) {
//								logger.error("Rolling back transaction failed!", t);
//								}

								try {
									if (jdoConfigDir != null) {
										if (!IOUtil.deleteDirectoryRecursively(jdoConfigDir))
											logger.error("Deleting JDO config directory \"" + jdoConfigDir.getAbsolutePath() + "\" failed!");;
									}
								} catch (Throwable t) {
									logger.error("Deleting JDO config directory \"" + jdoConfigDir.getAbsolutePath() + "\" failed!", t);
								}

								if (organisationCf != null) {
									try {
										if (!organisationConfigModule.removeOrganisation(organisationCf.getOrganisationID()))
											throw new IllegalStateException("Organisation was not registered in ConfigModule!");

										resetOrganisationCfs();
										organisationConfigModule._getConfig().save();
									} catch (Throwable t) {
										logger.error("Removing organisation \"" + organisationCf.getOrganisationID() + "\" from JFire server configuration failed!", t);
									}
								}

								try {
									Thread.sleep(10000); // Give server time to undeploy... postgreSQL otherwise doesn't allow DROP DATABASE :-(
								} catch (InterruptedException x) { } // ignore

								// We drop the database after rollback() and after undeploying the descriptors, because it might be the case
								// that JDO tries to do sth. with the database or the database being locked by the DB server.
								try {
									if (dropDatabase && databaseAdapter != null)
										databaseAdapter.dropDatabase();
								} catch (Throwable t) {
									logger.error("Dropping database failed!", t);
								}
							}
							databaseAdapter.close(); databaseAdapter = null;
						} // } finally {
					} finally {
						initialContext.close(); initialContext = null;
					}

				} catch (CreateOrganisationException x) {
					createOrganisationProgress.addCreateOrganisationStatus(
							new CreateOrganisationStatus(CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_error, x));
					throw x;
				} catch (Throwable x) {
					createOrganisationProgress.addCreateOrganisationStatus(
							new CreateOrganisationStatus(CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_error, x));
					throw new CreateOrganisationException(x);
				}

			} // synchronized (this) {

//			String deployBaseDir = mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();

			try {
				datastoreInitManager.initialiseOrganisation(this, mcf.getConfigModule().getLocalServer(), organisationID,
						jfireSecurity_createTempUserPassword(organisationID, User.USER_ID_SYSTEM), createOrganisationProgress);
			} catch (OrganisationInitException e) {
				logger.error("Datastore initialization for new organisation \""+organisationID+"\" failed!", e);
			}

		// OLD INIT STUFF
		// DatastoreInitializer datastoreInitializer = new DatastoreInitializer(this, mcf, getJ2EEVendorAdapter());

//		try {
//			datastoreInitializer.initializeDatastore(
//					this, mcf.getConfigModule().getLocalServer(), organisationID,
//					jfireSecurity_createTempUserPassword(organisationID, User.USER_ID_SYSTEM));
//		} catch (Exception x) {
//			logger.error("Datastore initialization for new organisation \""+organisationID+"\" failed!", x);
//		}
		} finally {
			synchronized (createOrganisationProgressID_mutex) {
				if (Util.equals(createOrganisationProgressID, createOrganisationProgress.getCreateOrganisationProgressID()))
					createOrganisationProgressID = null;

				createOrganisationProgress.done();
			}
		}
	}


	/**
	 * @throws OrganisationNotFoundException If the organisation does not exist.
	 */
	protected OrganisationCf getOrganisationConfig(String organisationID)
	throws OrganisationNotFoundException
	{
		OrganisationCf org = getOrganisationCfsCloned().get(organisationID);
		if (org == null)
			throw new OrganisationNotFoundException("No organisation with [master]organisationID=\""+organisationID+"\" existent!");
		return org;
	}

	/**
	 * @throws OrganisationNotFoundException If the organisation does not exist.
	 */
	protected void addServerAdmin(String organisationID, String userID)
		throws OrganisationNotFoundException
	{
		OrganisationCf org = null;
		for (Iterator<OrganisationCf> it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ){
			OrganisationCf o = it.next();
			if (organisationID.equals(o.getOrganisationID())) { // ||
//					organisationID.equals(o.getMasterOrganisationID())) {
				org = o;
				break;
			}
		}

		if (org == null)
			throw new OrganisationNotFoundException("No organisation with [master]organisationID=\""+organisationID+"\" existent!");

		org.addServerAdmin(userID);

		resetOrganisationCfs();
	}

	/**
	 * @throws OrganisationNotFoundException If the organisation does not exist.
	 */
	protected boolean removeServerAdmin(String organisationID, String userID)
		throws OrganisationNotFoundException
	{
		OrganisationCf org = null;
		for (Iterator<OrganisationCf> it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ){
			OrganisationCf o = it.next();
			if (organisationID.equals(o.getOrganisationID())) { // ||
//					organisationID.equals(o.getMasterOrganisationID())) {
				org = o;
				break;
			}
		}

		if (org == null)
			throw new OrganisationNotFoundException("No organisation with [master]organisationID=\""+organisationID+"\" existent!");

		boolean res = org.removeServerAdmin(userID);

		resetOrganisationCfs();
		return res;
	}

	protected Config getConfig()
	{
		return mcf.getConfig();
	}

	protected boolean isOrganisationCfsEmpty()
	{
		return organisationConfigModule.getOrganisations().isEmpty();
	}

	protected synchronized List<OrganisationCf> getOrganisationCfs(boolean sorted)
	{
		// We create a new ArrayList to avoid any problems that might occur if
		// resetOrganisationCfs() is executed (e.g. if a new organisation is added).
		ArrayList<OrganisationCf> l = new ArrayList<OrganisationCf>(getOrganisationCfsCloned().values());
		if (sorted)
			Collections.sort(l);
		return l;
	}

//	public synchronized void flushModuleCache()
//	{
//		cachedModules = null;
//	}
//
//	/**
//	 * key: ModuleType moduleType<br/>
//	 * value: List modules
//	 */
//	protected Map<ModuleType, List<ModuleDef>> cachedModules = null;
//
//	public synchronized List<ModuleDef> getModules(ModuleType moduleType) throws XMLReadException
//	{
//		if (cachedModules == null)
//			cachedModules = new HashMap<ModuleType, List<ModuleDef>>();
//
//		List<ModuleDef> modules = cachedModules.get(moduleType);
//		if (modules == null) {
//			File startDir = new File(mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory());
//			modules = new ArrayList<ModuleDef>();
//			findModules(startDir, moduleType, modules);
//			Collections.sort(modules);
//			cachedModules.put(moduleType, modules);
//		}
//		return modules;
//	}
//
//	private static class FileFilterDirectoriesExcludingEARs implements FilenameFilter
//	{
//		@Override
//		public boolean accept(File dir, String name)
//		{
//			if (name.endsWith(".ear"))
//				return false;
//			File f = new File(dir, name);
//			return f.isDirectory();
//		}
//	}
//	private static FileFilterDirectoriesExcludingEARs fileFilterDirectoriesExcludingEARs = null;
//
//	public static class FileFilterEARs implements FilenameFilter
//	{
//		@Override
//		public boolean accept(File dir, String name)
//		{
//			return name.endsWith(".ear");
//		}
//	}
//	private static FileFilterEARs fileFilterEARs = null;
//
//	private void findModules(File directory, ModuleType moduleType, List<ModuleDef> modules)
//		throws XMLReadException
//	{
//		if (fileFilterDirectoriesExcludingEARs == null)
//			fileFilterDirectoriesExcludingEARs = new FileFilterDirectoriesExcludingEARs();
//		String[] directories = directory.list(fileFilterDirectoriesExcludingEARs);
//		if (directories != null) {
//			for (int i = 0; i < directories.length; ++i)
//				findModules(new File(directory, directories[i]), moduleType, modules);
//		} // if (directories != null) {
//
//		if (fileFilterEARs == null)
//			fileFilterEARs = new FileFilterEARs();
//		String[] ears = directory.list(fileFilterEARs);
//		if (ears != null) {
//			for (int i = 0; i < ears.length; ++i) {
//				File ear = new File(directory, ears[i]);
//				findModulesInEAR(ear, moduleType, modules);
//			}
//		} // if (ears != null) {
//	}
//
//	private void findModulesInEAR(File ear, ModuleType moduleType, List<ModuleDef> modules)
//		throws XMLReadException
//	{
//// TODO So far, we only support ear directories, but no ear jars.
//// EARApplication should be extended to support both!
//		if (!ear.isDirectory()) {
//			logger.warn("Deployed EAR \""+ear.getAbsolutePath()+"\" is ignored, because only EAR directories are supported!");
//			return;
//		}
//		EARApplication earAppMan = new EARApplication(ear, moduleType);
//		for (Iterator<ModuleDef> it = earAppMan.getModules().iterator(); it.hasNext(); ) {
//			ModuleDef md = it.next();
//			modules.add(md);
//		}
//	}


	// ******************************************
	// *** Helper variables & methods ***
	// ******************************************

	/**
	 * This map holds clones of the real OrganisationCf instances within
	 * the ConfigModule.
	 * <br/><br/>
	 * key: String organisationID / String masterOrganisationID<br/>
	 * value: OrganisationCf org
	 */
	private Map<String, OrganisationCf> organisationCfsCloned = null;

	protected synchronized void resetOrganisationCfs()
	{
		organisationCfsCloned = null;
	}

	@Override
	public boolean containsOrganisation(String organisationID)
	{
		return getOrganisationCfsCloned().containsKey(organisationID);
	}

	protected synchronized Map<String, OrganisationCf> getOrganisationCfsCloned()
	{
		if (organisationCfsCloned == null)
		{
			Map<String, OrganisationCf> organisationCfsCloned = new HashMap<String, OrganisationCf>();
			organisationConfigModule.acquireReadLock();
			try {
				for (Iterator<OrganisationCf> it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ) {
					OrganisationCf org = (OrganisationCf)it.next().clone();
					org.makeReadOnly();
					organisationCfsCloned.put(org.getOrganisationID(), org);
//					if (!org.getMasterOrganisationID().equals(org.getOrganisationID()))
//						organisationCfsCloned.put(org.getMasterOrganisationID(), org);
				}
			} finally {
				organisationConfigModule.releaseLock();
			}
			this.organisationCfsCloned = Collections.unmodifiableMap(organisationCfsCloned);
		}
		return organisationCfsCloned;
	}

	public void undeploy(File deployment)
	throws IOException
	{
		if (deployment.isAbsolute())
			logger.warn("deployment should not be an absolute file: " + deployment.getPath(), new IllegalArgumentException("deployment should not be an absolute file: " + deployment.getPath()));

		if (!deployment.isAbsolute()) {
			deployment = new File(
					new File(mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory()).getAbsoluteFile().getParentFile(),
					deployment.getPath());
		}

		if (!deployment.exists()) {
			logger.warn("deployment does not exist: " + deployment.getPath(), new IllegalArgumentException("deployment does not exist: " + deployment.getPath()));
			return;
		}

		if (!IOUtil.deleteDirectoryRecursively(deployment)) {
			if (deployment.exists())
				throw new IOException("The deployment could not be undeployed: " + deployment.getPath());
			else
				logger.warn("deleting deployment failed, but it does not exist anymore (which is fine): " + deployment.getPath(), new IOException("deleting deployment failed, but it does not exist anymore (which is fine): " + deployment.getPath()));
		}
	}

	public void createDeploymentJar(String organisationID, File deploymentJar, Collection<DeploymentJarItem> deploymentJarItems, DeployOverwriteBehaviour deployOverwriteBehaviour)
	throws IOException
	{
		if (deploymentJar.isAbsolute())
			logger.warn("deploymentJar should not be an absolute file: " + deploymentJar.getPath(), new IllegalArgumentException("deploymentJar should not be an absolute file: " + deploymentJar.getPath()));

		if (!deploymentJar.isAbsolute()) {
			deploymentJar = new File(
					new File(mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory()).getAbsoluteFile().getParentFile(),
					deploymentJar.getPath());
		}

		if (deploymentJar.exists()) {
			switch (deployOverwriteBehaviour) {
				case EXCEPTION:
					throw new DeployedFileAlreadyExistsException(deploymentJar);
				case KEEP:
					logger.warn("File " + deploymentJar + " already exists. Will not change anything!");
					return; // silently return
				case OVERWRITE:
					// nothing
					break;
				default:
					throw new IllegalStateException("Unknown deployOverwriteBehaviour: " + deployOverwriteBehaviour);
			}
		}

		logger.info("Creating deploymentJar: \""+deploymentJar.getAbsolutePath()+"\"");

		// create a temporary directory
		File tmpDir;
		do {
			tmpDir = new File(
					IOUtil.getTempDir(),
					"jfire_" +
					Base62Coder.sharedInstance().encode(System.currentTimeMillis(), 1) + '-' +
					Base62Coder.sharedInstance().encode((int)(Math.random() * Integer.MAX_VALUE), 1) + ".tmp");
		} while (tmpDir.exists()); // should never happen, but it's safer ;-)

		// in case there is no DeploymentJarItem, we create the tmpDir in any case
		if (!tmpDir.mkdirs())
			throw new IOException("Could not create temporary directory: " + tmpDir);

		try { // ensure cleanup

			// we create the manifest only, if it is not contained in the deploymentJarItems
			File manifestFileRelative = new File("META-INF/MANIFEST.MF");

			// create deployment descriptors within the temporary directory
			boolean createManifest = true;
			for (DeploymentJarItem deploymentJarItem : deploymentJarItems) {
				if (manifestFileRelative.equals(deploymentJarItem.getDeploymentJarEntry()))
					createManifest = false;

				File deploymentDescriptorFile = new File(tmpDir, deploymentJarItem.getDeploymentJarEntry().getPath());
				createDeploymentDescriptor(
						organisationID,
						deploymentDescriptorFile,
						deploymentJarItem.getTemplateFile(),
						deploymentJarItem.getAdditionalVariables(),
						DeployOverwriteBehaviour.EXCEPTION); // it should not exist as we deploy into a temporary directory
			}

			// create manifest
			if (createManifest) {
				File manifestFile = new File(tmpDir, manifestFileRelative.getPath());
				if (!manifestFile.getParentFile().mkdirs())
					throw new IOException("Could not create META-INF directory: " + manifestFile.getParentFile());

				Manifest manifest = new Manifest();
				manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
				manifest.getMainAttributes().putValue("Created-By", "JFire - http://www.jfire.org");
				FileOutputStream out = new FileOutputStream(manifestFile);
				try {
					manifest.write(out);
				} finally {
					out.close();
				}
			} // if (createManifest) {

			File deploymentDirectory = deploymentJar.getParentFile();
			if (!deploymentDirectory.exists()) {
				logger.info("deploymentDirectory does not exist. Creating it: " + deploymentDirectory.getAbsolutePath());
				if (!deploymentDirectory.mkdirs())
					logger.error("Creating deploymentDirectory failed: " + deploymentDirectory.getAbsolutePath());
			}

			if (deploymentJar.exists()) {
				logger.warn("deploymentJar already exists. Replacing it: " + deploymentJar.getAbsolutePath());
				if (!deploymentJar.delete())
					throw new IOException("Deleting deploymentJar failed: " + deploymentJar.getAbsolutePath());
			}

			IOUtil.zipFolder(deploymentJar, tmpDir);
		} finally {
			IOUtil.deleteDirectoryRecursively(tmpDir);
		}
	}

	/**
	 * @param organisationID The organisation for which a new deployment-descriptor is created.
	 * @param deploymentDescriptorFile The deployment-descriptor-file (relative recommended) that shall be created. The parent-directories are implicitely created.
	 *		If this is relative, it will be created inside the deploy-directory of the jee server (i.e. within a subdirectory, if it contains a path, and as sibling
	 *		to JFire.last).
	 * @param templateFile The template file.
	 * @param additionalVariables Additional variables that shall be available besides the default variables. They override default values, if they contain colliding keys.
	 * @param deployOverwriteBehaviour TODO
	 * @throws IOException If writing/reading fails.
	 */
	public void createDeploymentDescriptor(
			String organisationID, File deploymentDescriptorFile, File templateFile, Map<String, String> additionalVariables, DeployOverwriteBehaviour deployOverwriteBehaviour)
	throws IOException
	{
		JFireServerConfigModule cfMod = mcf.getConfigModule();
		DatabaseCf dbCf = cfMod.getDatabase();

//		if (deploymentDescriptorFile.isAbsolute()) // this method is used by createDeploymentJar with an absolute file, hence we cannot warn here.
//			logger.warn("deploymentDescriptorFile should not be an absolute file: " + deploymentDescriptorFile.getPath(), new IllegalArgumentException("deploymentDescriptorFile should not be an absolute file: " + deploymentDescriptorFile.getPath()));

		if (!deploymentDescriptorFile.isAbsolute()) {
			deploymentDescriptorFile = new File(
					new File(cfMod.getJ2ee().getJ2eeDeployBaseDirectory()).getAbsoluteFile().getParentFile(),
					deploymentDescriptorFile.getPath());
		}

		if (deploymentDescriptorFile.exists()) {
			switch (deployOverwriteBehaviour) {
				case EXCEPTION:
					throw new DeployedFileAlreadyExistsException(deploymentDescriptorFile);
				case KEEP:
					logger.warn("File " + deploymentDescriptorFile + " already exists. Will not change anything!");
					return;
				case OVERWRITE:
					logger.warn("File " + deploymentDescriptorFile + " already exists. Will overwrite this file!");
					break;
				default:
					throw new IllegalStateException("Unknown deployOverwriteBehaviour: " + deployOverwriteBehaviour);
			}
		}

//		String organisationID_simpleChars = organisationID.replace('.', '_');
//
//		// generate databaseName
//		StringBuffer databaseNameSB = new StringBuffer();
//		databaseNameSB.append(dbCf.getDatabasePrefix());
//		databaseNameSB.append(organisationID_simpleChars);
//		databaseNameSB.append(dbCf.getDatabaseSuffix());
//		String databaseName = databaseNameSB.toString();
		String databaseName = createDatabaseName(organisationID);

		// get jdbc url
		String dbURL = dbCf.getDatabaseURL(databaseName);
		String datasourceJNDIName_relative = OrganisationCf.DATASOURCE_PREFIX_RELATIVE + organisationID;
		String datasourceJNDIName_absolute = OrganisationCf.DATASOURCE_PREFIX_ABSOLUTE + organisationID;
		String jdoPersistenceManagerFactoryJNDIName_relative = OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_RELATIVE + organisationID;
		String jdoPersistenceManagerFactoryJNDIName_absolute = OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE + organisationID;

		Map<String, String> variables = new HashMap<String, String>();
		variables.put("organisationID", organisationID);
//		variables.put("datasourceJNDIName_relative", datasourceJNDIName_relative);
//		variables.put("datasourceJNDIName_absolute", datasourceJNDIName_absolute);
		variables.put("datasourceJNDIName_relative_noTx", datasourceJNDIName_relative + "/no-tx");
		variables.put("datasourceJNDIName_absolute_noTx", datasourceJNDIName_absolute + "/no-tx");
		variables.put("datasourceJNDIName_relative_localTx", datasourceJNDIName_relative + "/local-tx");
		variables.put("datasourceJNDIName_absolute_localTx", datasourceJNDIName_absolute + "/local-tx");
		variables.put("datasourceJNDIName_relative_xa", datasourceJNDIName_relative + "/xa");
		variables.put("datasourceJNDIName_absolute_xa", datasourceJNDIName_absolute + "/xa");
		variables.put("datasourceMetadataTypeMapping", dbCf.getDatasourceMetadataTypeMapping());
		variables.put("jdoPersistenceManagerFactoryJNDIName_relative", jdoPersistenceManagerFactoryJNDIName_relative);
		variables.put("jdoPersistenceManagerFactoryJNDIName_absolute", jdoPersistenceManagerFactoryJNDIName_absolute);
//		variables.put("databaseDriverName", dbCf.getDatabaseDriverName());
		variables.put("databaseDriverName_noTx", dbCf.getDatabaseDriverName_noTx());
		variables.put("databaseDriverName_localTx", dbCf.getDatabaseDriverName_localTx());
		variables.put("databaseDriverName_xa", dbCf.getDatabaseDriverName_xa());
		variables.put("databaseURL", dbURL);
		variables.put("databaseName", databaseName);
		variables.put("databaseUserName", dbCf.getDatabaseUserName());
		variables.put("databasePassword", dbCf.getDatabasePassword());

//		variables.put("deploymentDescriptorDirectory", deploymentDescriptorFile.getParent());
		// We write a relative path instead - this is much cleaner and allows moving the server to a different directory.
		variables.put("deploymentDescriptorDirectory", deploymentDescriptorFile.getParent());
		variables.put("deploymentDescriptorDirectory_absolute", deploymentDescriptorFile.getParent());
		variables.put("deploymentDescriptorDirectory_relative", IOUtil.getRelativePath(new File("."), deploymentDescriptorFile.getParent()));

		variables.put("deploymentDescriptorFileName", deploymentDescriptorFile.getName());

		if (additionalVariables != null)
			variables.putAll(additionalVariables); // we put them afterwards to allow overriding

		_createDeploymentDescriptor(
				deploymentDescriptorFile,
				templateFile,
				variables);
	}

	private static enum ParserExpects {
		NORMAL,
		BRACKET_OPEN,
		VARIABLE,
		BRACKET_CLOSE
	}

	/**
	 * Generate a -ds.xml file (or any other deployment descriptor) from a template.
	 *
	 * @param deploymentDescriptorFile The file (absolute!) that shall be created out of the template.
	 * @param templateFile The template file to use. Must not be <code>null</code>.
	 * @param variables This map defines what variable has to be replaced by what value. The
	 *				key is the variable name (without brackets "{", "}"!) and the value is the
	 *				value for the variable to replace. This must not be <code>null</code>.
	 */
	private void _createDeploymentDescriptor(File deploymentDescriptorFile, File templateFile, Map<String, String> variables)
		throws IOException
	{
		if (!deploymentDescriptorFile.isAbsolute())
			throw new IllegalArgumentException("deploymentDescriptorFile is not absolute: " + deploymentDescriptorFile.getPath());

		logger.info("Creating deploymentDescriptor \""+deploymentDescriptorFile.getAbsolutePath()+"\" from template \""+templateFile.getAbsolutePath()+"\".");
		File deploymentDirectory = deploymentDescriptorFile.getParentFile();

		if (!deploymentDirectory.exists()) {
			logger.info("deploymentDirectory does not exist. Creating it: " + deploymentDirectory.getAbsolutePath());
			if (!deploymentDirectory.mkdirs())
				logger.error("Creating deploymentDirectory failed: " + deploymentDirectory.getAbsolutePath());
		}

		// Create and configure StreamTokenizer to read template file.
		FileReader fr = new FileReader(templateFile);
		try {
			StreamTokenizer stk = new StreamTokenizer(fr);
			stk.resetSyntax();
			stk.wordChars(0, Integer.MAX_VALUE);
			stk.ordinaryChar('$');
			stk.ordinaryChar('{');
			stk.ordinaryChar('}');
			stk.ordinaryChar('\n');

			// Create FileWriter
			FileWriter fw = new FileWriter(deploymentDescriptorFile);
			try {

				// Read, parse and replace variables from template and write to FileWriter fw.
				String variableName = null;
				StringBuffer tmpBuf = new StringBuffer();
				ParserExpects parserExpects = ParserExpects.NORMAL;
				while (stk.nextToken() != StreamTokenizer.TT_EOF) {
					String stringToWrite = null;

					if (stk.ttype == StreamTokenizer.TT_WORD) {
						switch (parserExpects) {
							case VARIABLE:
								parserExpects = ParserExpects.BRACKET_CLOSE;
								variableName = stk.sval;
								tmpBuf.append(variableName);
							break;
							case NORMAL:
								stringToWrite = stk.sval;
							break;
							default:
								parserExpects = ParserExpects.NORMAL;
								stringToWrite = tmpBuf.toString() + stk.sval;
								tmpBuf.setLength(0);
						}
					}
					else if (stk.ttype == '\n') {
						stringToWrite = new String(new char[] { (char)stk.ttype });

						// These chars are not valid within a variable, so we reset the variable parsing, if we're currently parsing one.
						// This helps keeping the tmpBuf small (to check for rowbreaks is not really necessary).
						if (parserExpects != ParserExpects.NORMAL) {
							parserExpects = ParserExpects.NORMAL;
							stringToWrite = tmpBuf.toString() + stringToWrite;
							tmpBuf.setLength(0);
						}
					}
					else if (stk.ttype == '$') {
						if (parserExpects != ParserExpects.NORMAL) {
							stringToWrite = tmpBuf.toString();
							tmpBuf.setLength(0);
						}
						tmpBuf.append((char)stk.ttype);
						parserExpects = ParserExpects.BRACKET_OPEN;
					}
					else if (stk.ttype == '{') {
						switch (parserExpects) {
							case NORMAL:
								stringToWrite = new String(new char[] { (char)stk.ttype });
							break;
							case BRACKET_OPEN:
								tmpBuf.append((char)stk.ttype);
								parserExpects = ParserExpects.VARIABLE;
							break;
							default:
								parserExpects = ParserExpects.NORMAL;
								stringToWrite = tmpBuf.toString() + (char)stk.ttype;
								tmpBuf.setLength(0);
						}
					}
					else if (stk.ttype == '}') {
						switch (parserExpects) {
							case NORMAL:
								stringToWrite = new String(new char[] { (char)stk.ttype });
							break;
							case BRACKET_CLOSE:
								parserExpects = ParserExpects.NORMAL;
								tmpBuf.append((char)stk.ttype);

								if (variableName == null)
									throw new IllegalStateException("variableName is null!!!");

								stringToWrite = variables.get(variableName);
								if (stringToWrite == null) {
									logger.warn("Variable " + tmpBuf.toString() + " occuring in template \"" + templateFile + "\" is unknown!");
									stringToWrite = tmpBuf.toString();
								}
								tmpBuf.setLength(0);
							break;
							default:
								parserExpects = ParserExpects.NORMAL;
								stringToWrite = tmpBuf.toString() + (char)stk.ttype;
								tmpBuf.setLength(0);
						}
					}

					if (stringToWrite != null)
						fw.write(stringToWrite);
				} // while (stk.nextToken() != StreamTokenizer.TT_EOF) {

			} finally {
				fw.close();
			}
		} finally {
			fr.close();
		}
	}

	public static PersistenceManagerFactory getPersistenceManagerFactory(String organisationID)
	{
		PersistenceManagerFactory pmf;
		try {
			InitialContext initCtx = new InitialContext();
			try {
				pmf = (PersistenceManagerFactory) initCtx.lookup(
						OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE + organisationID);
			} finally {
				initCtx.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
		return pmf;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.PersistenceManagerProvider#getPersistenceManager(java.lang.String)
	 */
	@Override
	public PersistenceManager getPersistenceManager(String organisationID)
	{
		PersistenceManagerFactory pmf = getPersistenceManagerFactory(organisationID);
		PersistenceManager pm = pmf.getPersistenceManager();
		NLJDOHelper.setThreadPersistenceManager(pm);
		return pm;
	}

	protected PersistenceManagerFactory waitForPersistenceManagerFactory(String persistenceManagerJNDIName) throws PersistenceManagerFactoryWaitException
	{
		try {
			InitialContext initCtx = new InitialContext();
			try {
				PersistenceManagerFactory pmf = null;
				long waitStartDT = System.currentTimeMillis();

				int timeout = createOrganisationConfigModule.getWaitForPersistenceManager_timeout();
				int checkPeriod = createOrganisationConfigModule.getWaitForPersistenceManager_checkPeriod();
				while (pmf == null)
				{
					try {
						pmf = (PersistenceManagerFactory)initCtx.lookup(persistenceManagerJNDIName);
					} catch (NamingException x) {
						if (System.currentTimeMillis() < waitStartDT) { // System time has been changed!
							waitStartDT = System.currentTimeMillis();
							logger.warn("While waiting for deployment of PersistenceManagerFactory \""+persistenceManagerJNDIName+"\", the system time has been changed. Resetting wait time.");
						}

						if (System.currentTimeMillis() - waitStartDT > timeout) {
							logger.fatal("PersistenceManagerFactory \""+persistenceManagerJNDIName+"\" has not become accessible in JNDI within timeout (\""+timeout+"\" msec).");
							throw x;
						}
						else
							try {
								logger.info("PersistenceManagerFactory \""+persistenceManagerJNDIName+"\" is not yet accessible in JNDI. Waiting "+checkPeriod+" msec.");
								Thread.sleep(checkPeriod);
							} catch (InterruptedException e) {
								logger.error("Sleeping has been interrupted!", e);
							}
					}
				} // while (pmf == null)

//				PersistenceManager pm = null;
//				int pmTryCount = 0;
//				while (pm == null) {
//					try {
//						pm = pmf.getPersistenceManager();
//						if (pm == null)
//							throw new NullPointerException("PersistenceManager coming out of factory should never be null!");
//					} catch (Exception x) {
//						logger.warn("getPersistenceManager() failed!", x);
//
//						if (++pmTryCount > 3)
//							throw x;
//						Thread.sleep(3000);
//					}
//				}
//
//				return pm;
				return pmf;
			} finally {
				initCtx.close();
			}
		} catch (Exception x) {
			throw new PersistenceManagerFactoryWaitException(x);
		}
	}

	protected CLRegistrar getCLRegistrar(JFireBasePrincipal principal)
	{
		return clRegistrarFactory.getCLRegistrar(principal);
	}

	/**
	 * key: UserID userID<br/>
	 * value: String password
	 */
	private Map<UserID, String> jfireSecurity_tempUserPasswords = new HashMap<UserID, String>();

	/**
	 * @deprecated Use {@link #jfireSecurity_checkTempUserPassword(UserID, String)} instead.
	 */
	@Deprecated
	protected boolean jfireSecurity_checkTempUserPassword(String organisationID, String userID, String password)
	{
		return jfireSecurity_checkTempUserPassword(UserID.create(organisationID, userID), password);
	}

	protected boolean jfireSecurity_checkTempUserPassword(UserID userID, String password)
	{
		String pw;
		synchronized(jfireSecurity_tempUserPasswords) {
			pw = jfireSecurity_tempUserPasswords.get(userID);
			if (pw == null)
				return false;
		}
		return pw.equals(password);
	}

	/**
	 * @deprecated Use {@link #jfireSecurity_createTempUserPassword(UserID)} instead!
	 */
	@Deprecated
	protected String jfireSecurity_createTempUserPassword(String organisationID, String userID)
	{
		return jfireSecurity_createTempUserPassword(UserID.create(organisationID, userID));
	}

	protected String jfireSecurity_createTempUserPassword(UserID userID)
	{
		synchronized(jfireSecurity_tempUserPasswords) {
			String pw = jfireSecurity_tempUserPasswords.get(userID);
			if (pw == null) {
				pw = UserLocal.createMachinePassword(15, 20);
				jfireSecurity_tempUserPasswords.put(userID, pw);
			}
			return pw;
		}
	}

	/**
	 * This Map caches all the roles for all the users. It does NOT expire, because
	 * it relies on that {@link #jfireSecurity_flushCache()} or {@link #jfireSecurity_flushCache(String, String)}
	 * is executed whenever access rights change!
	 *
	 * key: String userID + @ + organisationID<br/>
	 * value: SoftReference of RoleSet roleSet
	 */
	protected Map<String, SoftReference<RoleSet>> jfireSecurity_roleCache = new HashMap<String, SoftReference<RoleSet>>();

	protected void jfireSecurity_flushCache(UserID _userID)
	{
		if (User.USER_ID_OTHER.equals(_userID.userID)) {
			jfireSecurity_flushCache();
			return;
		}

		String userPK = _userID.userID + '@' + _userID.organisationID;
		synchronized (jfireSecurity_roleCache) {
			jfireSecurity_roleCache.remove(userPK);
		}
	}

	protected void jfireSecurity_flushCache()
	{
		synchronized (jfireSecurity_roleCache) {
			jfireSecurity_roleCache.clear();
		}
	}

	protected static final Principal loginWithoutWorkstationRolePrincipal = new SimplePrincipal(org.nightlabs.jfire.workstation.RoleConstants.loginWithoutWorkstation.roleID);
	protected static final Principal serverAdminRolePrincipal = new SimplePrincipal(RoleConstants.serverAdmin.roleID);
	protected static final Principal systemRolePrincipal = new SimplePrincipal(User.USER_ID_SYSTEM);
	protected static final Principal guestRolePrincipal = new SimplePrincipal(RoleConstants.guest.roleID);

	/**
	 * Get the roles that are assigned to a certain user.
	 *
	 * @param pm The PersistenceManager to be used to access the datastore. Can be <code>null</code> (in this case, the method will obtain and close a PM itself).
	 * @param organisationID The organisationID of the user.
	 * @param userID The userID of the user.
	 * @return the role-set of the specified user.
	 * @throws OrganisationNotFoundException If the organisation does not exist.
	 */
	protected RoleSet jfireSecurity_getRoleSet(PersistenceManager pm, String organisationID, String userID)
	throws OrganisationNotFoundException
	{
		String userPK = userID + User.SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID + organisationID;

		RoleSet roleSet = null;
		// lookup in cache.
		synchronized (jfireSecurity_roleCache) {
			SoftReference<RoleSet> ref = jfireSecurity_roleCache.get(userPK);
			if (ref != null)
				roleSet = ref.get();
		}

		if (roleSet != null)
			return roleSet;

		roleSet = new RoleSet(); // RoleSet.class.getName() + '[' + userPK + ']');

		roleSet.addMember(new SimplePrincipal(RoleConstants.guest.roleID)); // EVERYONE has this role (if he's logged in)!

		boolean closePM = false;
		if (pm == null) {
			closePM = true;
			pm = getPersistenceManager(organisationID);
		}
		try {
			if (Organisation.DEV_ORGANISATION_ID.equals(organisationID) && User.USER_ID_ANONYMOUS.equals(userID)) {
				// do nothing
			}
			else if (User.USER_ID_SYSTEM.equals(userID)) {
				// user is system user and needs ALL roles
				roleSet.addMember(loginWithoutWorkstationRolePrincipal);
				roleSet.addMember(serverAdminRolePrincipal);
				roleSet.addMember(systemRolePrincipal); // ONLY the system user has this role - no real user can get it as its virtual (it is ignored during import)
				for (Iterator<?> it = pm.getExtent(Role.class, true).iterator(); it.hasNext(); ) {
					Role role = (Role) it.next();
					roleSet.addMember(new SimplePrincipal(role.getRoleID()));
				}
			}
			else {
				// user is normal user and has only those roles that are assigned

				pm.getExtent(AuthorizedObjectRef.class, true);

				// If the user is marked as server admin, we give it the appropriate
				// role. For security reasons, this role is managed outside of the persistence
				// manager, because data within the organisations' database is belonging to this
				// organisation and can be changed by them. This role must not be set by the
				// organisation, but only by the administrator of the server.
				if (getOrganisationConfig(organisationID).isServerAdmin(userID))
					roleSet.addMember(new SimplePrincipal(RoleConstants.serverAdmin.roleID));

				AuthorizedObjectRef authorizedObjectRef;
				try {
					authorizedObjectRef = (AuthorizedObjectRef) pm.getObjectById(
							AuthorizedObjectRefID.create(
									organisationID,
									Authority.AUTHORITY_ID_ORGANISATION,
									UserLocalID.create(organisationID, userID, organisationID).toString()
							)
					);
				} catch (JDOObjectNotFoundException x) {
					try {
						authorizedObjectRef = (AuthorizedObjectRef) pm.getObjectById(
								AuthorizedObjectRefID.create(
										organisationID,
										Authority.AUTHORITY_ID_ORGANISATION,
										UserLocalID.create(organisationID, User.USER_ID_OTHER, organisationID).toString()
								)
						);
					} catch (JDOObjectNotFoundException e) {
						authorizedObjectRef = null;
					}
				}

				// get roleRefs
				if (authorizedObjectRef != null) {
					for (Iterator<RoleRef> it = authorizedObjectRef.getRoleRefs().iterator(); it.hasNext(); ) {
						RoleRef roleRef = it.next();
						roleSet.addMember(roleRef.getRolePrincipal());
					}
				} // if (authorizedObjectRef != null) {

			} // if (User.USER_ID_SYSTEM.equals(userID)) {

		} finally {
			if (closePM)
				pm.close();
		}

		synchronized (jfireSecurity_roleCache) {
			jfireSecurity_roleCache.put(userPK, new SoftReference<RoleSet>(roleSet));
		}
		return roleSet;
	}

	@Override
	public List<J2eeServerTypeRegistryConfigModule.J2eeRemoteServer> getJ2eeRemoteServers()
	{
		return Collections.unmodifiableList(j2eeLocalServerCf.getJ2eeRemoteServers());
	}
	@Override
	public J2eeServerTypeRegistryConfigModule.J2eeRemoteServer getJ2eeRemoteServer(String j2eeServerType)
	{
		return j2eeLocalServerCf.getJ2eeRemoteServer(j2eeServerType);
	}

//	@Override
//	public String getInitialContextFactory(String j2eeServerTypeRemote, boolean throwExceptionIfUnknownServerType)
//	{
//		J2eeServerTypeRegistryConfigModule.J2eeRemoteServer j2eeRemoteServerCf = j2eeLocalServerCf.getJ2eeRemoteServer(j2eeServerTypeRemote);
//		if (j2eeRemoteServerCf == null) {
//			if (throwExceptionIfUnknownServerType)
//				throw new IllegalArgumentException("No configuration for remote j2eeServerType \""+j2eeServerTypeRemote+"\"!");
//
//			return null;
//		}
//		return j2eeRemoteServerCf.getInitialContextFactory();
//	}

	@Override
	public String getLoginInitialContextFactory(String j2eeServerTypeRemote, String protocol, boolean throwExceptionIfNotFound)
	{
		J2eeServerTypeRegistryConfigModule.J2eeRemoteServer j2eeRemoteServerCf = j2eeLocalServerCf.getJ2eeRemoteServer(j2eeServerTypeRemote);
		if (j2eeRemoteServerCf == null) {
			if (throwExceptionIfNotFound)
				throw new IllegalArgumentException("No configuration for remote j2eeServerType \""+j2eeServerTypeRemote+"\"!");

			return null;
		}
		return j2eeRemoteServerCf.getLoginInitialContextFactory(protocol, throwExceptionIfNotFound);
	}

	@Override
	public String getAnonymousInitialContextFactory(String j2eeServerTypeRemote, String protocol, boolean throwExceptionIfNotFound)
	{
		J2eeServerTypeRegistryConfigModule.J2eeRemoteServer j2eeRemoteServerCf = j2eeLocalServerCf.getJ2eeRemoteServer(j2eeServerTypeRemote);
		if (j2eeRemoteServerCf == null) {
			if (throwExceptionIfNotFound)
				throw new IllegalArgumentException("No configuration for remote j2eeServerType \""+j2eeServerTypeRemote+"\"!");

			return null;
		}
		return j2eeRemoteServerCf.getAnonymousInitialContextFactory(protocol, throwExceptionIfNotFound);
	}

	@Override
	public ServerCf getLocalServer()
	{
		return (ServerCf) mcf.getConfigModule().getLocalServer().clone();
	}

	@Override
	public boolean isUpAndRunning()
	{
		return upAndRunning;
	}

	@Override
	public boolean isShuttingDown()
	{
		return shuttingDown;
	}

}
