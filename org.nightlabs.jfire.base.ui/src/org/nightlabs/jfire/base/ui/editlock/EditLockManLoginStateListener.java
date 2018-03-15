package org.nightlabs.jfire.base.ui.editlock;

import org.nightlabs.base.ui.login.LoginState;
import org.nightlabs.jfire.base.ui.login.LoginStateChangeEvent;
import org.nightlabs.jfire.base.ui.login.LoginStateListener;

public class EditLockManLoginStateListener
implements LoginStateListener
{
	private boolean loggingOut = false;
	
	@Override
	public void loginStateChanged(LoginStateChangeEvent event)
	{
		if (event.getNewLoginState() == LoginState.ABOUT_TO_LOG_OUT) {
			loggingOut = true;
			EditLockMan.sharedInstance(); // ensure that all classes are loaded before status LOGGED_OUT (in case EditLockMan.sharedInstance() was not called before).
		}
		else if (loggingOut && event.getNewLoginState() == LoginState.LOGGED_OUT) {
			loggingOut = false;
			EditLockMan.sharedInstance().cancelAllJobs();
		}
	}
}
