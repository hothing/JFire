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

package org.nightlabs.jfire.base.ui.login;

import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.ejb.EJBAccessException;
import javax.naming.CommunicationException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerRegistry.Mode;
import org.nightlabs.base.ui.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.login.LoginState;
import org.nightlabs.base.ui.notification.SelectionManager;
import org.nightlabs.base.ui.progress.ProgressMonitorWrapper;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.classloader.osgi.DelegatingClassLoaderOSGI;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.eclipse.ui.dialog.ChangePasswordDialog;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.j2ee.JFireJ2EEPlugin;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.classloader.remote.JFireRCDLDelegate;
import org.nightlabs.jfire.classloader.remote.backend.JFireRCLBackendRemote;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.math.Base62Coder;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Defines a client login to the JFire server
 * <p>
 * Use the static function getLogin. If logged in an instance of this class is returned.
 * If not and all attempts to authenticate on the server fail a {@link javax.security.auth.login.LoginException} is thrown.
 * <p>
 * In general all code that requires the user to log in should place a line like
 * <code>Login.getLogin();</code>
 * somewhere before. If the user is already logged in this method immediately exits and returns
 * the static Login member. If the user some time before decided to work OFFLINE this method
 * will throw an {@link LoginAbortedException} to indicate this and not make any attempts to
 * login unless {@link #setForceLogin(boolean)} was not set to true. This means that user interface
 * actions have to do something like the following to login:
 * <pre>
 *   Login.getLogin(false).setForceLogin(true);
 *   Login.getLogin();
 * </pre>
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class Login
extends AbstractEPProcessor
//implements InitialContextProvider
{
	private static final String CLASS_ELEMENT = "class"; //$NON-NLS-1$
	private static final String LOGIN_STATE_LISTENER_ELEMENT = "loginStateListener"; //$NON-NLS-1$

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(Login.class);

	public static final long WORK_OFFLINE_TIMEOUT = 60000; // One minute

	/**
	 * Loginstate: Logged in
	 *
	 * Loginstate: Logged out

	 * Loginstate: Working OFFLINE indicating also that the user wants
	 * to stay OFFLINE and causing the Login to fail for a certain time
	 * when not forced.
	 * @see LoginStateListener
	 */

	//public enum LoginState { LOGINSTATE_LOGGED_IN, LOGINSTATE_LOGGED_OUT, LOGINSTATE_OFFLINE }


	private static Login sharedInstanceLogin = null;

	private boolean forceLogin = false;

	//	private long lastWorkOfflineDecisionTime = System.currentTimeMillis();

	private volatile LoginState  currentLoginState = LoginState.LOGGED_OUT;


	/**
	 * Class used to pass the result of
	 * login procedures back to {@link Login}
	 * @author alex
	 */
	public static class AsyncLoginResult {
		private Throwable exception = null;
		private boolean success = false;
		private String message = ""; //$NON-NLS-1$
		private boolean loginAborted = false;

		private boolean wasAuthenticationErr = false;
		private boolean wasCommunicationErr = false;
		private boolean wasSocketTimeout = false;

		public void reset() {
			exception = null;
			success = false;
			message = ""; //$NON-NLS-1$
			loginAborted = false;

			wasAuthenticationErr = false;
			wasCommunicationErr = false;
			wasSocketTimeout = false;
		}

		public Throwable getException() {
			return exception;
		}
		public void setException(Throwable exception) {
			this.exception = exception;
		}
		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public boolean isLoginAborted() {
			return loginAborted;
		}
		public void setLoginAborted(boolean workOffline) {
			this.loginAborted = workOffline;
		}
		/**
		 * @return Returns the wasAuthenticationErr.
		 */
		public boolean isWasAuthenticationErr() {
			return wasAuthenticationErr;
		}
		/**
		 * @param wasAuthenticationErr The wasAuthenticationErr to set.
		 */
		public void setWasAuthenticationErr(boolean wasAuthenticationErr) {
			this.wasAuthenticationErr = wasAuthenticationErr;
		}
		/**
		 * @return Returns the wasCommunicationErr.
		 */
		public boolean isWasCommunicationErr() {
			return wasCommunicationErr;
		}
		/**
		 * @param wasCommunicationErr The wasCommunicationErr to set.
		 */
		public void setWasCommunicationErr(boolean wasCommunicationErr) {
			this.wasCommunicationErr = wasCommunicationErr;
		}
		/**
		 * @return Returns the wasSocketTimeout.
		 */
		public boolean isWasSocketTimeout() {
			return wasSocketTimeout;
		}
		/**
		 * @param wasSocketTimeout The wasSocketTimeout to set.
		 */
		public void setWasSocketTimeout(boolean wasSocketTimeout) {
			this.wasSocketTimeout = wasSocketTimeout;
		}

		public void copyValuesTo(AsyncLoginResult loginResult) {
			loginResult.exception = this.exception;
			loginResult.success = this.success;
			loginResult.message = this.message;
			loginResult.loginAborted = this.loginAborted;

			loginResult.wasAuthenticationErr = this.wasAuthenticationErr;
			loginResult.wasCommunicationErr = this.wasCommunicationErr;
			loginResult.wasSocketTimeout = this.wasSocketTimeout;
		}
	}

	/**
	 * Used internally within static block, in order to create the shared instance as soon as the class is loaded.
	 */
	protected static void createLogin() {
		Login.sharedInstanceLogin = new Login();
		org.nightlabs.base.ui.login.Login.sharedInstance(); // force processing of extension point
	}

	static {
		Login.createLogin();
	}

	public String getSessionID()
	{
		if (loginData != null) {
			if (loginData.getSessionID() == null) {
				Base62Coder coder = Base62Coder.sharedInstance();
				loginData.setSessionID(
						coder.encode(System.currentTimeMillis(), 1) + '-' +
						coder.encode((long)(Math.random() * 14776335), 1)); // 14776335 is the highest value encoded in 4 digits ("zzzz")
			}
			return loginData.getSessionID();
		}
		return null;
	}

	/**
	 * Checks if currently logged in.
	 * @return
	 */
	public static boolean isLoggedIn() {
		if (Login.sharedInstanceLogin == null)
			return false;

		LoginState ls = Login.sharedInstanceLogin.currentLoginState;
		return ls == LoginState.LOGGED_IN || ls == LoginState.ABOUT_TO_LOG_OUT;
	}

	/**
	 * Get the current {@link LoginState}.
	 *
	 * @return the <code>LoginState</code>.
	 */
	public LoginState getLoginState() {
		return currentLoginState;
	}

	private volatile boolean logoutInProcess = false;
	private Object logoutInProcessMutex = new Object();

	public void logout() {
		if (currentLoginState == LoginState.LOGGED_OUT || currentLoginState == LoginState.ABOUT_TO_LOG_IN)
			return; // we are logged out or performing a login right now => return silently

		synchronized (logoutInProcessMutex) {
			if (logoutInProcess)
				return; // Should not be a problem to return before the logout really finished. it's probably a very rare situation anyway.

			logoutInProcess = true;
		}
		try {
			Exception ex = null;

			changeLoginStateAndNotifyListeners(LoginState.ABOUT_TO_LOG_OUT);

			try {
				Cache.sharedInstance().close(); // cache has threads running => should be shutdown first
				// remove class loader delegate
				JFireRCDLDelegate.sharedInstance().unregister(DelegatingClassLoaderOSGI.getSharedInstance());
				// logout
				loginData = null;
				flushInitialContextProperties();
			} catch (Exception e) {
				ex = e;
			}
			changeLoginStateAndNotifyListeners(LoginState.LOGGED_OUT);
			if (ex != null)
				throw new RuntimeException(ex);

		} finally {
			logoutInProcess = false;
		}
	}

	private volatile LoginData handlingLoginData = null;

	private AsyncLoginResult loginResult = new AsyncLoginResult();

	protected LoginConfigModule getRuntimeConfigModule()
	{
		if (_runtimeConfigModule == null) {
			try {
				LoginConfigModule _loginConfigModule = (Config.sharedInstance().createConfigModule(LoginConfigModule.class));
				if (_loginConfigModule != null) {
					_runtimeConfigModule = (LoginConfigModule) _loginConfigModule.clone();
				}
			} catch (ConfigException e) {
				// no previously stored ConfigModule was found -> do nothing.
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return _runtimeConfigModule;
	}

	/**
	 * Set only when {@link #loginHandlerRunnable} runs.
	 * Used to ensure that the runnable runs only once.
	 */
	private volatile boolean loginHandlerRunnableRunning = false;
	/**
	 * Sets {@link #loginHandlerRunnableRunning} to true synchronized
	 * and throws an exception if it was true already.
	 * Used to ensure that {@link #loginHandlerRunnable} runs only once.
	 */
	private synchronized void acquireLoginHandlerRunnableRunning() {
		if (loginHandlerRunnableRunning)
			throw new IllegalStateException("While a loginHandlerRunnable was running, anotherone was started"); //$NON-NLS-1$
		loginHandlerRunnableRunning = true;
	}
	/**
	 * The runnable that calls the {@link ILoginHandler}.
	 */
	private Runnable loginHandlerRunnable = new Runnable() {
		private boolean logoutFirst = false;

		public void run() {
			acquireLoginHandlerRunnableRunning();
			acquireHandlingLogin();
			try {
				if (logoutFirst)
					logout();

				try{
					if (Login.sharedInstanceLogin == null)
						Login.createLogin();

					loginResult.reset();
					// find a login handler
					ILoginHandler lHandler = getLoginHandler();
					if (lHandler == null)
						throw new LoginException("Cannot login, loginHandler is not set!"); //$NON-NLS-1$

					Login.logger.debug("Calling login handler"); //$NON-NLS-1$
					// let the handler populate the login data
					lHandler.handleLogin(handlingLoginData, Login.sharedInstanceLogin.getRuntimeConfigModule(), loginResult);


					if ((!loginResult.isSuccess()) || (loginResult.getException() != null)) {
						//						loginData = null;
						// login unsuccessful
						return;
					}

					if (handlingLoginData != null) {
						// handle the login only if the handling
						// was not taken over by a readAndDispach further up the stack
						handleSuccessfulLogin();
					}

				} catch(Throwable t){
					Login.logger.error("Exception thrown while logging in.",t); //$NON-NLS-1$
					loginResult.setException(t);
				}
			} finally {
				handlingLoginData = null;
				synchronized (loginResult) {
					Login.logger.debug("Login handler done notifying loginResult"); //$NON-NLS-1$
					loginResult.notifyAll();
				}
				loginHandlerRunnableRunning = false;
			}
		}
	};

	/**
	 * Performs the actions necessary when login was successful.
	 * Currently this registers the remote classloading delegate.
	 */
	private void handleSuccessfulLogin() {
		// set the login data
		flushInitialContextProperties(); // should be done by logout() but it doesn't hurt
		loginData = new LoginData(handlingLoginData);
		// done should be logged in by now

		// at the end, we register the JFireRCDLDelegate
		JFireRCDLDelegate.sharedInstance().register(DelegatingClassLoaderOSGI.getSharedInstance()); // this method does nothing, if already registered.
		boolean needRestart = false;
		try {
			needRestart = JFireJ2EEPlugin.getDefault().updateManifest();
		} catch (Exception e) {
			logger.error(e, e);
			needRestart = false;
		}
		if (needRestart) {
			// Set the exception-handler mode to bypass
			ExceptionHandlerRegistry.sharedInstance().setMode(Mode.bypass);
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					Shell shell = RCPUtil.getActiveShell();
					MessageDialog.openInformation(
							shell,
							Messages.getString("org.nightlabs.jfire.base.ui.login.Login.rebootDialogTitle"), //$NON-NLS-1$
							Messages.getString("org.nightlabs.jfire.base.ui.login.Login.rebootDialogMessage")); //$NON-NLS-1$

					safeRestart();
				}
			});
		}
		forceLogin = false;

		// notify loginstate listeners
		changeLoginStateAndNotifyListeners(LoginState.LOGGED_IN);

		handlingLoginData = null;
	}

	/**
	 * This method is necessary, because the restart may be required at a very early stage. Thus,
	 * this method will recursively delay the restart until it can be performed without an exception.
	 */
	private void safeRestart()
	{
		if (RCPUtil.getActiveWorkbenchShell() != null)
			PlatformUI.getWorkbench().restart();
		else { // too early!
			new org.eclipse.core.runtime.jobs.Job("Restart") { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor monitor)
				{
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// ignore
					}
					Display.getDefault().asyncExec(new Runnable()
					{
						public void run()
						{
							safeRestart();
						}
					});
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	/**
	 * Actually performs the login procedure.<br/>
	 * This method calls {@link #doLogin(boolean)} with parameter forceLogoutFirst
	 * set to false, so nothing will happen if already logged in.
	 *
	 * @throws LoginException Exception is thrown whenever some error occurs during login.
	 * But not that the user might be presented the possibility to work OFFLINE.
	 * In this case a LoginException is thrown as well with a {@link LoginAbortedException} as cause.
	 *
	 * @see ILoginHandler
	 * @see Login#setLoginHandler(ILoginHandler)
	 * @see #doLogin(boolean)
	 */
	public void doLogin() throws LoginException {

		doLogin(false);
	}

	/**
	 * This method checks if the login is already handled by
	 * checking {@link #handlingLoginData} and will return <code>false</code>
	 * if this is not null. Otherwise it will assign the
	 * {@link LoginData} used to handle the login and return <code>true</code>.
	 */
	private synchronized boolean acquireHandlingLogin()
	{
		if (handlingLoginData != null)
			return false;

		handlingLoginData = new LoginData();
		return true;
	}
	/**
	 * Checks whether {@link #handlingLoginData} is assigned
	 * and will return <code>true</code> if so.
	 */
	private boolean isHandlingLogin() {
		return handlingLoginData != null;
	}

	private volatile boolean loginHandlerRunnablePending = false;

	/**
	 * Actually performs the login procedure.
	 * To do so it calls {@link ILoginHandler#handleLogin(LoginData, LoginConfigModule, Login.AsyncLoginResult)}
	 * of the LoginHandler defined with {@link #setLoginHandler(ILoginHandler)}.
	 *
	 * @param forceLogoutFirst Defines weather to logout first
	 *
	 * @throws LoginException Exception is thrown whenever some error occurs during login.
	 * But not that the user might be presented the possibility to work OFFLINE.
	 * In this case a LoginException is thrown as well with a {@link LoginAbortedException} as cause.
	 *
	 * @see ILoginHandler
	 * @see Login#setLoginHandler(ILoginHandler)
	 */
	private void doLogin(final boolean forceLogoutFirst) throws LoginException
	{
		Login.logger.debug("Login requested by thread "+Thread.currentThread());		 //$NON-NLS-1$
		//		if ((currLoginState == LoginState.OFFLINE)){
		//			long elapsedTime = System.currentTimeMillis() - lastWorkOfflineDecisionTime;
		//			if (!forceLogin && elapsedTime < WORK_OFFLINE_TIMEOUT) {
		//				LoginException lEx = new LoginException();
		//				lEx.initCause(new LoginAbortedException());
		//				throw lEx;
		//			}
		//		}

		if (currentLoginState == LoginState.LOGGED_IN || currentLoginState == LoginState.ABOUT_TO_LOG_OUT) {
			Login.logger.debug("Already logged in, returning. Thread "+Thread.currentThread()); //$NON-NLS-1$
			if (forceLogin) forceLogin = false;
			return;
		}
		boolean iAmHandlingLogin = acquireHandlingLogin();
		//		if (!Display.getDefault().getThread().equals(Thread.currentThread())) {
		if (Display.getCurrent() == null) {
			if (iAmHandlingLogin) {
				final String threadName = Thread.currentThread().getName();
				Login.logger.info("Non-UI thread (" + threadName + ") is responsible for login. Delegating loginHandlerRunnable to UI thread."); //$NON-NLS-1$ //$NON-NLS-2$
				loginHandlerRunnablePending = true;
				Display.getDefault().syncExec(new Runnable() {
					public void run()
					{
						if (loginHandlerRunnablePending) {
							changeLoginStateAndNotifyListeners(LoginState.ABOUT_TO_LOG_IN);
							loginHandlerRunnablePending = false;
							loginHandlerRunnable.run();
						}
						else
							Login.logger.info("Non-UI thread (" + threadName + ") was not responsible anymore for login when the scheduled Runnable was finally executed on the UI thread. Skipped login in this runnable."); //$NON-NLS-1$ //$NON-NLS-2$
					}
				});

				Login.logger.info("... syncExec for loginHandlerRunnable returned."); //$NON-NLS-1$
			}
			else {
				Login.logger.debug("Login requestor-thread "+Thread.currentThread()+" waiting for login handler");		 //$NON-NLS-1$ //$NON-NLS-2$
				synchronized (loginResult) {
					while (isHandlingLogin()) {
						try {
							loginResult.wait(5000);
						} catch (InterruptedException e) { }
					}
				}
			}

			Login.logger.debug("Login requestor-thread "+Thread.currentThread()+" returned");		 //$NON-NLS-1$ //$NON-NLS-2$
		}
		else {
			if (iAmHandlingLogin) {
				Login.logger.info("UI thread (" + Thread.currentThread().getName() + ") is responsible for login. Calling loginHandlerRunnable.run()..."); //$NON-NLS-1$ //$NON-NLS-2$

				changeLoginStateAndNotifyListeners(LoginState.ABOUT_TO_LOG_IN);
				loginHandlerRunnable.run();
				Login.logger.info("...loginHandlerRunnable.run() returned."); //$NON-NLS-1$
			}
			else {
				Display display = Display.getCurrent();
				while (isHandlingLogin() && !loginResult.isSuccess() && !loginResult.isLoginAborted()) {
					display.readAndDispatch();

					// During start-up, the syncExecs are *not* executed, because this obviously is deferred till the workbench is completely up.
					// Hence, we must take over the login-process here in order to prevent dead-lock.
					if (loginHandlerRunnablePending) {
						Login.logger.info("Hijacking responsibility for login, since I'm the UI thread and want to login, too. Will execute login now here."); //$NON-NLS-1$

						changeLoginStateAndNotifyListeners(LoginState.ABOUT_TO_LOG_IN);
						loginHandlerRunnablePending = false;
						loginHandlerRunnable.run();
					}
				}

				// if we come here inside a nested readAndDispatch while another login process
				// is performed on the UI thread outside (in the outer readAndDispatch), we have
				// to hijack control, too and do everything already here that's normally done by the outer login process.
				if (isHandlingLogin() && loginResult.isSuccess()) {
					handleSuccessfulLogin();
				}
			}
		}
		// exception throwing
		if (loginResult.getException() != null){
			if (loginResult.getException() instanceof LoginException)
				throw (LoginException)loginResult.getException();
			else
				Login.logger.error("Exception thrown while logging in.",loginResult.getException()); //$NON-NLS-1$
			throw new LoginException(loginResult.getException().getMessage());
		}
		if (!loginResult.isSuccess()) {
			if (loginResult.isLoginAborted()) {
				// if user decided to work OFFLINE first notify loginstate listeners
				//				currLoginState = LoginState.LOGGED_OUT;
				//				notifyLoginStateListeners_afterChange(currLoginState);
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						changeLoginStateAndNotifyListeners(LoginState.LOGGED_OUT);
					}
				});
				// but then still throw Exception with WorkOffline as cause
				LoginException lEx = new LoginException(loginResult.getMessage());
				lEx.initCause(new LoginAbortedException(loginResult.getMessage()));
				throw lEx;
			}
			else
				throw new LoginException(loginResult.getMessage());
		}

		Login.logger.debug("Login OK. Thread "+Thread.currentThread()); //$NON-NLS-1$
	}

	private org.nightlabs.jfire.base.jdo.JDOObjectID2PCClassNotificationInterceptor objectID2PCClassNotificationInterceptor = null;

	/**
	 * Sets whether to force login on next attempt even if login state is
	 * {@link #LOGINSTATE_OFFLINE}.
	 *
	 * @param forceLogin Whether to force Login on the next attempt.
	 */
	public void setForceLogin(boolean forceLogin) {
		this.forceLogin = forceLogin;
	}

	/**
	 * If not logged in by now does so and
	 * returns the static instance of Login.
	 * <p>
	 * This method can be called on every {@link Thread} (no matter, whether UI or not).
	 * </p>
	 *
	 * @return
	 * @throws LoginException
	 */
	public static Login getLogin()
	throws LoginException
	{
		return Login.getLogin(true);
	}

	/**
	 * This method can be called on every {@link Thread} (no matter, whether UI or not). It returns the shared instance of Login
	 * without causing a popup dialog to appear.
	 */
	public static Login sharedInstance()
	{
		if (Login.sharedInstanceLogin == null)
			throw new NullPointerException("createLogin has not been called! SharedInstance is null!"); //$NON-NLS-1$
		return Login.sharedInstanceLogin;
	}

	/**
	 * Returns the static instance of Login.
	 * If doLogin is true the login procedure is started.
	 * <p>
	 * This method can be called on every {@link Thread} (no matter, whether UI or not).
	 * </p>
	 *
	 * @param doLogin specifies weather the login procedure should be started
	 * @throws LoginException
	 * @see Login#doLogin()
	 */
	public static Login getLogin(boolean doLogin)
	throws LoginException
	{
		if (Login.sharedInstanceLogin == null)
			throw new NullPointerException("createLogin not called! sharedInstance is null!"); //$NON-NLS-1$

		if (doLogin) {
			Login.sharedInstanceLogin.doLogin();
		}
		return Login.sharedInstanceLogin;
	}

	/**
	 * This method spawns a {@link Job} and calls {@link #getLogin()} on it. This method
	 * should be used in login-aware UI to cause a login-dialog to popup without blocking
	 * the UI.
	 * <p>
	 * This method can be called on every {@link Thread} (no matter, whether UI or not).
	 * </p>
	 */
	public static void loginAsynchronously()
	{
		Job job = new Job(Messages.getString("org.nightlabs.jfire.base.ui.login.Login.authenticationJob")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor arg0) throws Exception
			{
				try {
					Login.getLogin();
				} catch (LoginException e) {
					// we ignore it as the UI should not required to be logged in (if it's implemented correctly)
				}

				return Status.OK_STATUS;
			}
		};
		job.setPriority(org.eclipse.core.runtime.jobs.Job.SHORT);
		job.schedule();
	}

	private volatile LoginData loginData;

	//	public LoginData getLoginDataCopy() {
	//	return new LoginData(loginData);
	//	}

	protected LoginData getLoginData() {
		return loginData;
	}

	private LoginConfigModule _runtimeConfigModule = null; // new LoginConfigModule();

	// ILoginHandler to handle the user interaction
	private ILoginHandler loginHandler = null;
	public ILoginHandler getLoginHandler() {
		return loginHandler;
	}
	/**
	 * Here one can hook a {@link ILoginHandler} to handle user interaction for login.
	 * @param loginHandler
	 */
	public void setLoginHandler(ILoginHandler loginHandler) {
		this.loginHandler = loginHandler;
	}

	/**
	 * Creates a new Login.
	 *
	 * @throws NamingException
	 */
	protected Login() {}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		if (loginData == null)
			return null;

		return loginData.getOrganisationID();
	}
	/**
	 * @return Returns the userID.
	 */
	public String getUserID() {
		if (loginData == null)
			return null;

		return loginData.getUserID();
	}

	public String getPrincipalName() {
		if (loginData == null)
			return null;

		//		return loginData.getPrincipalName();
		return loginData.getUserID() + LoginData.USER_ORGANISATION_SEPARATOR + loginData.getOrganisationID();
	}

	/**
	 * Returns the {@link UserID} of the user
	 * @return the {@link UserID} of the user
	 */
	public UserID getUserObjectID() {
		if (loginData == null)
			return null;

		return UserID.create(getOrganisationID(), getUserID());
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		if (loginData == null)
			return null;

		return loginData.getPassword();
	}

	/**
	 * Set the new password. This method should normally not be called! It's only purpose is to switch to a new password
	 * after the user changed his password (see {@link ChangePasswordDialog})
	 *
	 * @param password
	 */
	public void setPassword(String password) {
		this.loginData.setPassword(password);
	}

	/**
	 * @return Returns the workstationID.
	 */
	public String getWorkstationID() {
		if (loginData == null)
			return null;

		return loginData.getAdditionalParams().get(LoginData.WORKSTATION_ID);
	}

	public User getUser(String fetchGroups[], int maxFetchDepth, ProgressMonitor monitor) {
		return UserDAO.sharedInstance().getUser(
				UserID.create(loginData.getOrganisationID(), loginData.getUserID()),
				fetchGroups, maxFetchDepth, monitor
		);
	}

	/**
	 * @deprecated Use {@link #getUser(String[], int, ProgressMonitor)} instead!
	 */
	@Deprecated
	public User getUser(String fetchGroups[], int maxFetchDepth, IProgressMonitor monitor) {
		return UserDAO.sharedInstance().getUser(
				UserID.create(loginData.getOrganisationID(), loginData.getUserID()),
				fetchGroups, maxFetchDepth, new ProgressMonitorWrapper(monitor)
		);
	}

	protected transient Properties initialContextProperties = null;
	protected transient InitialContext initialContext = null;

	public void flushInitialContextProperties() {
		initialContextProperties = null;
	}

	/**
	 * Returns InitialContextProperties
	 */
	public Properties getInitialContextProperties() // throws LoginException
	{
		if (initialContextProperties == null) {
			initialContextProperties = loginData.getInitialContextProperties();
		}
		return initialContextProperties;
	}

	public InitialContext createInitialContext()
	{
		try {
			return new InitialContext(getInitialContextProperties());
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	//	/**
	//	* @deprecated Do not use anymore! Use
	//	*/
	//	@Deprecated
	//	public InitialContext getInitialContext() throws NamingException, LoginException
	//	{
	////	logger.debug("getInitialContext(): begin"); //$NON-NLS-1$
	////	doLogin();
	////	logger.debug("getInitialContext(): logged in"); //$NON-NLS-1$
	////	if (initialContext != null)
	////	return initialContext;

	////	logger.debug("getInitialContext(): creating new initctx."); //$NON-NLS-1$
	////	initialContext = new InitialContext(getInitialContextProperties());
	////	return initialContext;
	//	}

	/**
	 * Returns the runtime (not the persitent) LoginConfigModule. The persistent
	 * one can be obtained via {@link Config}.
	 *
	 * @return The runtime (not the persitent) LoginConfigModule.
	 */
	public LoginConfigModule getLoginConfigModule() {
		return getRuntimeConfigModule();
	}

	/**
	 * Simple class to hold {@link LoginStateListener}
	 * and their associated {@link IAction}.
	 */
	protected static class LoginStateListenerRegistryItem {
		private LoginStateListener loginStateListener;
		private IAction action;
		public LoginStateListenerRegistryItem(LoginStateListener loginStateListener, IAction action) {
			super();
			this.loginStateListener = loginStateListener;
			this.action = action;
		}
		public IAction getAction() {
			return action;
		}
		public LoginStateListener getLoginStateListener() {
			return loginStateListener;
		}
		private boolean checkActionOnEquals = true;

		public boolean isCheckActionOnEquals() {
			return checkActionOnEquals;
		}
		public void setCheckActionOnEquals(boolean checkActionOnEquals) {
			this.checkActionOnEquals = checkActionOnEquals;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof LoginStateListenerRegistryItem) {
				if ( ((LoginStateListenerRegistryItem)o).getLoginStateListener().equals(this.loginStateListener)) {
					if (isCheckActionOnEquals()) {
						return ((LoginStateListenerRegistryItem)o).getAction().equals(this.action);
					}
					else
						return true;
				}
				else
					return false;
			}
			else
				return false;
		}
	}

	/**
	 * Holds instances of {@link Login.LoginStateListenerRegistryItem}.
	 */
	private List<LoginStateListenerRegistryItem> loginStateListenerRegistry = new LinkedList<LoginStateListenerRegistryItem>();

	public void addLoginStateListener(LoginStateListener loginStateListener) {
		addLoginStateListener(loginStateListener,null);
	}

	public void addLoginStateListener(LoginStateListener loginStateListener, IAction action) {
		synchronized (loginStateListenerRegistry) {
			LoginStateListenerRegistryItem regItem = new LoginStateListenerRegistryItem(loginStateListener,action);
			loginStateListenerRegistry.add(regItem);
			// we cannot trigger the beforeLoginStateChange here - that doesn't make much sense... or should we? marco.
			// and we pass the same value as old and new value since don't really know the old value. does this make sense? marco.

			LoginStateChangeEvent event = new LoginStateChangeEvent(this,
					getLoginState(), getLoginState(),
					action);

			loginStateListener.loginStateChanged(event);
		}
	}

	/**
	 * Removes all occurrences of the given {@link LoginStateListener}
	 * @param loginStateListener
	 */
	public synchronized void removeLoginStateListener(LoginStateListener loginStateListener) {
		removeLoginStateListener(loginStateListener,null,true);
	}

	/**
	 * Removes either the first or all occurences of the given {@link LoginStateListener}
	 * @param loginStateListener
	 * @param allOccurences
	 */
	public synchronized void removeLoginStateListener(LoginStateListener loginStateListener, boolean allOccurences) {
		removeLoginStateListener(loginStateListener,null,allOccurences);
	}

	/**
	 * Removes only the {@link LoginStateListener} associated to the given {@link IAction}.
	 * @param loginStateListener
	 * @param action
	 */
	public synchronized void removeLoginStateListener(LoginStateListener loginStateListener, IAction action) {
		removeLoginStateListener(loginStateListener,action,false);
	}

	/**
	 * Removes either all occurences of the given {@link LoginStateListener} or only
	 * the one associated to the given {@link IAction}.
	 * @param loginStateListener
	 * @param action
	 * @param allOccurencesOfListener
	 */
	public void removeLoginStateListener(LoginStateListener loginStateListener, IAction action, boolean allOccurencesOfListener) {
		synchronized (loginStateListenerRegistry) {
			LoginStateListenerRegistryItem searchItem = new LoginStateListenerRegistryItem(loginStateListener,action);
			if (allOccurencesOfListener) {
				searchItem.setCheckActionOnEquals(false);
			}
			if (!allOccurencesOfListener) {
				loginStateListenerRegistry.remove(searchItem);
			}
			else {
				while (loginStateListenerRegistry.contains(searchItem)) {
					loginStateListenerRegistry.remove(searchItem);
				}
			}
		}
	}

	//	protected void notifyLoginStateListeners_beforeChange(LoginState newLoginState) {
	//		synchronized (loginStateListenerRegistry) {
	//			try {
	//				checkProcessing();
	//
	//				for (LoginStateListenerRegistryItem item : new LinkedList<LoginStateListenerRegistryItem>(loginStateListenerRegistry)) {
	//					try {
	//
	//						LoginStateChangeEvent event = new LoginStateChangeEvent(this,
	//								getLoginState(), newLoginState,
	//								item.getAction());
	//
	//						item.getLoginStateListener().beforeLoginStateChange(event);
	//
	//
	//					} catch (Throwable t) {
	//						logger.warn("Caught exception while notifying LoginStateListener. Continue.", t); //$NON-NLS-1$
	//					}
	//				}
	//			} catch (Throwable t) {
	//				logger.warn("Caught exception while notifying LoginStateListeners. Abort.", t); //$NON-NLS-1$
	//			}
	//		}
	//	}

	//	protected void notifyLoginStateListeners_afterChange(LoginState newLoginState) {
	protected void changeLoginStateAndNotifyListeners(final LoginState newLoginState) {
		if (Display.getCurrent() == null)
			throw new IllegalStateException("This method must be called on the SWT UI thread!"); //$NON-NLS-1$

		final LoginState oldLoginState;
		final LinkedList<LoginStateListenerRegistryItem> loginStateListenerRegistryItems;

		synchronized (loginStateListenerRegistry) {
			try {
				checkProcessing();

				oldLoginState = currentLoginState;
				if (oldLoginState == newLoginState)
					return;

				currentLoginState = newLoginState;

				Login.logger.info("changeLoginStateAndNotifyListeners: changing from " + oldLoginState + " to " + newLoginState); //$NON-NLS-1$ //$NON-NLS-2$

				//				if (currLoginState == LoginState.OFFLINE)
				//					lastWorkOfflineDecisionTime = System.currentTimeMillis();

				// We should be logged in now, open the cache if not already open
				if (newLoginState == LoginState.LOGGED_IN) {
					try {
						Cache.sharedInstance().open(getSessionID()); // the cache is opened implicitely now by default, but it is closed *after* a logout.
					} catch (Throwable t) {
						Login.logger.debug("Cache could not be opened!", t); //$NON-NLS-1$
					}
				}

				if (LoginState.LOGGED_IN == newLoginState && objectID2PCClassNotificationInterceptor == null) {
					objectID2PCClassNotificationInterceptor = new org.nightlabs.jfire.base.jdo.JDOObjectID2PCClassNotificationInterceptor();
					SelectionManager.sharedInstance().addInterceptor(objectID2PCClassNotificationInterceptor);
					JDOLifecycleManager.sharedInstance().addInterceptor(objectID2PCClassNotificationInterceptor);
				}

				//				if (LoginState.LOGGED_IN != newLoginState && objectID2PCClassNotificationInterceptor != null) {
				if (LoginState.LOGGED_OUT == newLoginState && objectID2PCClassNotificationInterceptor != null) {
					SelectionManager.sharedInstance().removeInterceptor(objectID2PCClassNotificationInterceptor);
					JDOLifecycleManager.sharedInstance().removeInterceptor(objectID2PCClassNotificationInterceptor);
					objectID2PCClassNotificationInterceptor = null;
				}

				loginStateListenerRegistryItems = new LinkedList<LoginStateListenerRegistryItem>(loginStateListenerRegistry);
			} catch (Throwable t) {
				Login.logger.warn("Caught exception while changing LoginState and obtaining LoginStateListeners.", t); //$NON-NLS-1$
				return;
			}
		} // synchronized (loginStateListenerRegistry) {

		// We trigger the LoginStateListeners on the UI thread and outside of the synchronized block.
		// It is important to trigger them outside of this block, because this prevents dead-locks.
		// We ensure now above already that we are on the UI thread! But it's still important to trigger the listeners
		// outside of the synchronized block.
		//		Display.getDefault().syncExec(new Runnable() {
		//			public void run() {
		for (LoginStateListenerRegistryItem item : loginStateListenerRegistryItems) {
			try {
				LoginStateChangeEvent event = new LoginStateChangeEvent(this,
						oldLoginState, newLoginState,
						item.getAction());

				item.getLoginStateListener().loginStateChanged(event);
			} catch (Throwable t) {
				Login.logger.warn("Caught exception while notifying LoginStateListener.", t); //$NON-NLS-1$
			}
		}
		//			}
		//		});
	}

	//	/** // I think this method is not called anymore. Marco :-)
	//	 * Do not call this method yourself.<br/>
	//	 * It is used to trigger the notification right after the
	//	 * WorkbenchWindow is shown, as Login can be requested
	//	 * at a point in startup when actions and other
	//	 * LoginStateListeners are not build yet.<br/>
	//	 */
	//	protected void triggerLoginStateNotification() {
	//		notifyLoginStateListeners_afterChange(getLoginState());
	//	}

	/**
	 * @see org.nightlabs.base.ui.extensionpoint.AbstractEPProcessor#getExtensionPointID()
	 */
	@Override
	public String getExtensionPointID()
	{
		return "org.nightlabs.jfire.base.ui.loginstatelistener"; //$NON-NLS-1$
	}

	/**
	 * @see org.nightlabs.base.ui.extensionpoint.AbstractEPProcessor#processElement(IExtension, org.eclipse.core.runtime.IConfigurationElement)
	 */
	@Override
	public void processElement(IExtension extension, IConfigurationElement element) throws Exception
	{
		if (Login.LOGIN_STATE_LISTENER_ELEMENT.equals(element.getName())) {
			LoginStateListener listener = (LoginStateListener) element.createExecutableExtension(Login.CLASS_ELEMENT);
			addLoginStateListener(listener);
		}
	}

	public static Login.AsyncLoginResult testLogin(LoginData _loginData) {
		Login.AsyncLoginResult loginResult = new Login.AsyncLoginResult();
		loginResult.setSuccess(false);
		loginResult.setMessage(null);
		loginResult.setException(null);

		// verify login
		JFireRCLBackendRemote jfireRCLBackend = null;
		if (jfireRCLBackend == null) {
			try {
				Login.logger.debug(Thread.currentThread().getContextClassLoader());
				Login.logger.debug(JFireBasePlugin.class.getClassLoader());
				Login.logger.debug("**********************************************************"); //$NON-NLS-1$
				Login.logger.debug("Create testing login"); //$NON-NLS-1$
				InitialContext initialContext = new InitialContext(_loginData.getInitialContextProperties());
				try {
					jfireRCLBackend = (JFireRCLBackendRemote) initialContext.lookup("ejb/byRemoteInterface/" + JFireRCLBackendRemote.class.getName());
					jfireRCLBackend.ping("testLogin");
				} finally {
					initialContext.close();
				}
				Login.logger.debug("**********************************************************"); //$NON-NLS-1$
				loginResult.setSuccess(true);
			} catch (EJBAccessException x) {
				loginResult.setWasAuthenticationErr(true);
//			} catch (RemoteException remoteException) {
//				Throwable cause = remoteException.getCause();
//				if (cause != null && cause.getCause() instanceof EJBException) {
//					EJBException ejbE = (EJBException)cause.getCause();
//					if (ejbE != null) {
//						if (ejbE.getCausedByException() instanceof SecurityException)
//							// SecurityException authentication failure
//							loginResult.setWasAuthenticationErr(true);
//					}
//				}
//				else if (cause != null && ExceptionUtils.indexOfThrowable(cause, LoginException.class) >= 0) {
//					loginResult.setWasAuthenticationErr(true);
//					loginResult.setException(remoteException);
//				}
//				else {
//					if (ExceptionUtils.indexOfThrowable(cause, SecurityException.class) >= 0) {
//						loginResult.setWasAuthenticationErr(true);
//						loginResult.setSuccess(false);
//					}
//					else {
//						loginResult.setException(remoteException);
//						loginResult.setSuccess(false);
//					}
//				}
			} catch (Exception x) {
				if (x instanceof CommunicationException) {
					loginResult.setWasCommunicationErr(true);
				}
				if (x instanceof SocketTimeoutException) {
					loginResult.setWasSocketTimeout(true);
				}
				// cant create local bean stub
				Login.logger.error("Login failed!", x); //$NON-NLS-1$
				LoginException loginE = new LoginException(x.getMessage());
				loginE.initCause(x);
				loginResult.setMessage(Messages.getString("org.nightlabs.jfire.base.ui.login.Login.errorUnhandledExceptionMessage")); //$NON-NLS-1$
				loginResult.setException(loginE);
			}
		}

		return loginResult;
	}

}
