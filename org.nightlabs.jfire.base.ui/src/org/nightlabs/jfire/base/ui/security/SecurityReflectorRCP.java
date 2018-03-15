package org.nightlabs.jfire.base.ui.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.InitialContext;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.ui.jdo.notification.JDOLifecycleAdapterJob;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.security.AuthorizedObjectRefLifecycleListenerFilter;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.NoUserException;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.security.id.UserLocalID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class SecurityReflectorRCP
extends SecurityReflector
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SecurityReflectorRCP.class);

	@Override
	public UserDescriptor _getUserDescriptor() throws NoUserException {
		if (logger.isDebugEnabled())
			logger.debug("_getUserDescriptor: enter"); //$NON-NLS-1$

		Login l;
		try {
			l = Login.getLogin();
		} catch (Exception e) {
			throw new NoUserException(e);
		}
		return new UserDescriptor(l.getOrganisationID(), l.getUserID(), l.getWorkstationID(), l.getSessionID());
	}

	@Override
	protected InitialContext _createInitialContext() throws NoUserException {
		try {
			return Login.getLogin().createInitialContext();
		} catch (Exception e) {
			throw new NoUserException(e);
		}
	}

	@Override
	protected Properties _getInitialContextProperties() throws NoUserException {
		try {
			return Login.getLogin().getInitialContextProperties();
		} catch (Exception e) {
			throw new NoUserException(e);
		}
	}

	private Map<AuthorityID, Set<RoleID>> cache_authorityID2roleIDSet = new HashMap<AuthorityID, Set<RoleID>>();

	@Override
	protected synchronized Set<RoleID> _getRoleIDs(AuthorityID authorityID) throws NoUserException
	{
		Set<RoleID> result = cache_authorityID2roleIDSet.get(authorityID);
		if (result != null)
			return result;

		try {
			JFireSecurityManagerRemote jfireSecurityManager = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, _getInitialContextProperties());
			result = jfireSecurityManager.getRoleIDs(authorityID);
		} catch (NoUserException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			if (ExceptionUtils.indexOfThrowable(e, NoUserException.class) < 0)
				throw new RuntimeException(e);
			else
				throw new NoUserException(e);
		}

		cache_authorityID2roleIDSet.put(authorityID, result);
		return result;
	}

	private JDOLifecycleListener authorizedObjectRefLifecycleListener = null;

	private class AuthorizedObjectRefLifecycleListener extends JDOLifecycleAdapterJob
	{
		private IJDOLifecycleListenerFilter filter;

		public AuthorizedObjectRefLifecycleListener() {
			UserDescriptor userDescriptor = _getUserDescriptor();
			filter = new AuthorizedObjectRefLifecycleListenerFilter(
					UserLocalID.create(userDescriptor.getOrganisationID(), userDescriptor.getUserID(), userDescriptor.getOrganisationID()),
					JDOLifecycleState.DIRTY, JDOLifecycleState.DELETED
			);
		}

		@Override
		public IJDOLifecycleListenerFilter getJDOLifecycleListenerFilter() {
			return filter;
		}

		@Override
		public void notify(JDOLifecycleEvent event) {
			synchronized (SecurityReflectorRCP.this) {
				cache_authorityID2roleIDSet.clear();
			}
		}
	}

	protected synchronized void unregisterAuthorizedObjectRefLifecycleListener()
	{
		if (authorizedObjectRefLifecycleListener != null) {
			JDOLifecycleManager.sharedInstance().removeLifecycleListener(authorizedObjectRefLifecycleListener);
			authorizedObjectRefLifecycleListener = null;
			cache_authorityID2roleIDSet.clear();
		}
	}

	protected synchronized void registerAuthorizedObjectRefLifecycleListener()
	{
		unregisterAuthorizedObjectRefLifecycleListener();

		authorizedObjectRefLifecycleListener = new AuthorizedObjectRefLifecycleListener();
		JDOLifecycleManager.sharedInstance().addLifecycleListener(authorizedObjectRefLifecycleListener);
		cache_authorityID2roleIDSet.clear();
	}
}
