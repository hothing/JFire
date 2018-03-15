package org.nightlabs.jfire.web.demoshop;

import java.util.Properties;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.NoUserException;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.web.login.Login;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class WebSecurityReflector extends SecurityReflector
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	public static void register()
	{
		System.setProperty("org.nightlabs.jfire.security.SecurityReflector", WebSecurityReflector.class.getName());
	}

	@Override
	protected InitialContext _createInitialContext() throws NoUserException
	{
		try {
			return new InitialContext(_getInitialContextProperties());
		} catch (NamingException e) {
			throw new RuntimeException("Getting initial context properties failed", e);
		}
	}

	@Override
	protected Properties _getInitialContextProperties() throws NoUserException
	{
		return Login.getLogin().getInitialContextProperties();
	}

	@Override
	public UserDescriptor _getUserDescriptor() throws NoUserException
	{
		return new UserDescriptor(
				Login.getLogin().getOrganisationID(),
				Login.getLogin().getUserID(),
				Login.getLogin().getWorkstationID(),
				Login.class.getName()); // TODO we should find a solution for the session - maybe extend Login?!
//		return new UserDescriptor(ManagerProvider.ORGANISATIONID, ManagerProvider.USERID, "bums");
	}

	@Override
	protected Set<RoleID> _getRoleIDs(AuthorityID authorityID) throws NoUserException
	{
		// TODO cache the role-ids!!!

		try {
			JFireSecurityManagerRemote jfireSecurityManager = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, _getInitialContextProperties());
			return jfireSecurityManager.getRoleIDs(authorityID);
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
	}
}
