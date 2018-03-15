package org.nightlabs.jfire.base.ui.login;

import java.io.Serializable;

import org.nightlabs.concurrent.DeadLockException;
import org.nightlabs.concurrent.RWLockable;
import org.nightlabs.j2ee.LoginData;

/**
 * This class holds a single login configuration without the password. It is intended to be used in {@link LoginConfigModule} to
 * be able to store multiple login identities to let the user select the one to be used for the next login.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Marius Heinzmann -- Marius[at]NightLabs[dot]de
 */
public class LoginConfiguration
implements Serializable, Cloneable, RWLockable
{
	private static final long serialVersionUID = 5L;

	private LoginData loginData;
	private boolean automaticUpdate = false;

	private String name = null;

	public LoginConfiguration() {
		this.loginData = new LoginData();
	}

	/**
	 * TODO amend for use with automatic update
	 *
	 * @param _loginData
	 */
	public LoginConfiguration(LoginData _loginData, LoginConfigModule loginConfigModule)
	{
		this.loginData = new LoginData(_loginData);
		this.loginConfigModule = loginConfigModule;
	}

	public boolean isAutomaticUpdate() {
		return automaticUpdate;
	}

	public void setAutomaticUpdate(boolean automaticUpdate) {
		this.automaticUpdate = automaticUpdate;
	}

	private boolean currentlySaving = false;

	protected void beforeSave()
	{
		currentlySaving = true;
	}
	protected void afterSave()
	{
		currentlySaving = false;
	}

	/**
	 * Get the {@link LoginData} that has previously been set by {@link #setLoginData(LoginData)}.
	 * <p>
	 * This method is called by the bean-serialiser (when saving the config-module) and by other
	 * code retrieving what it has set before. Hence this method behaves differently, depending
	 * on whether it is called inbetween {@link #beforeSave()} and {@link #afterSave()}. If it is
	 * called between these two method calls, the method returns a copy of the original login-data
	 * without password, sessionID and some other non-persistent data.
	 * </p>
	 *
	 * @return the loginData of this configuration.
	 */
	public LoginData getLoginData() {
		acquireReadLock(); // ensure that accessing the flag currentlySaving is secure (it is set "true" while a write-lock is acquired)
		try {
			if (!currentlySaving)
				return loginData;

			if (loginData == null)
				return null;

			LoginData res = new LoginData(loginData);
			res.setPassword(null);
			if (res.getAdditionalParams() != null)
				res.getAdditionalParams().remove(LoginData.SESSION_ID);
			return res;
		} finally {
			releaseLock();
		}
	}

	/**
	 * @param loginData the loginData to use.
	 */
	public void setLoginData(LoginData loginData) {
		this.loginData = loginData;
	}

	public String getName() {
		return name;
	}

	public void setName(String configurationName) {
		this.name = configurationName;
	}

	@Override
	public String toString() {
		if (name == null || "".equals(name)) //$NON-NLS-1$
			return loginData.getUserID() + "@" + loginData.getOrganisationID() + " (" + loginData.getWorkstationID() + ") (" + loginData.getProviderURL() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		else
			return name;
	}

	public String toShortString() {
		if (name == null || "".equals(name)) { //$NON-NLS-1$
			return shorten(loginData.getUserID(), 8) +	"@" + shorten(loginData.getOrganisationID(), 8) + " (" + shorten(loginData.getWorkstationID(), 8) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return name;
	}

	public String shorten(String target, int count) {
		if (target.length() <= count)
			return target;

		int tailCount = count/2;
		int headCount = count - tailCount;
		String front = target.substring(0, headCount);
		String tail = target.substring(target.length()-tailCount);
		return front + ".." + tail; //$NON-NLS-1$
	}

	@Override
	public int hashCode() {
		if (name != null)
			return name.hashCode();
		else
			return loginData.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;

		final LoginConfiguration other = (LoginConfiguration) obj;

		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;

		// both descriptions == null
		return loginData.equals(other.loginData);
	}

	@Override
	protected LoginConfiguration clone()
	{
		try {
			LoginConfiguration clone = (LoginConfiguration) super.clone();
			clone.loginData = new LoginData(loginData);
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e); // should never happen!
		}
	}

	private LoginConfigModule loginConfigModule;

	protected void setLoginConfigModule(LoginConfigModule loginConfigModule)
	{
		this.loginConfigModule = loginConfigModule;
	}

	public void acquireReadLock()
	throws DeadLockException
	{
		if (loginConfigModule != null) // the bean-serializer creates a new instance to compare default values. that's why this might be null.
			loginConfigModule.acquireReadLock();
	}

	public void acquireWriteLock()
	throws DeadLockException
	{
		if (loginConfigModule != null)
			loginConfigModule.acquireWriteLock();
	}

	public void releaseLock()
	{
		if (loginConfigModule != null)
			loginConfigModule.releaseLock();
	}
}