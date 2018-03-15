package org.nightlabs.jfire.base.ui.login;

import org.nightlabs.base.ui.login.LoginState;
import org.nightlabs.jfire.classloader.remote.JFireRCDLDelegate;

/**
 * @author Marius Heinzmann <!-- marius[at]nightlabs[dot]de -->
 *
 */
public class ClearRCLCacheListener
	implements LoginStateListener
{
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.login.LoginStateListener#loginStateChanged(org.nightlabs.jfire.base.ui.login.LoginStateChangeEvent)
	 */
	@Override
	public void loginStateChanged(LoginStateChangeEvent event)
	{
		if (! LoginState.LOGGED_OUT.equals(event.getNewLoginState()))
			return;
		
		if (JFireRCDLDelegate.isSharedInstanceExisting())
			JFireRCDLDelegate.sharedInstance().clearCache();
	}
}
