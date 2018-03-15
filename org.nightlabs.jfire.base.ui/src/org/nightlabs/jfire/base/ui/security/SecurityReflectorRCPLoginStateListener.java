package org.nightlabs.jfire.base.ui.security;

import org.nightlabs.base.ui.login.LoginState;
import org.nightlabs.jfire.base.ui.login.LoginStateChangeEvent;
import org.nightlabs.jfire.base.ui.login.LoginStateListener;
import org.nightlabs.jfire.security.SecurityReflector;

public class SecurityReflectorRCPLoginStateListener
implements LoginStateListener
{
	@Override
	public void loginStateChanged(LoginStateChangeEvent event) {
		if (LoginState.LOGGED_IN == event.getNewLoginState()) {
			SecurityReflectorRCP securityReflector = (SecurityReflectorRCP) SecurityReflector.sharedInstance();
			securityReflector.registerAuthorizedObjectRefLifecycleListener();
		}
		if (LoginState.ABOUT_TO_LOG_OUT == event.getNewLoginState()) {
			SecurityReflectorRCP securityReflector = (SecurityReflectorRCP) SecurityReflector.sharedInstance();
			securityReflector.unregisterAuthorizedObjectRefLifecycleListener();
		}
	}
}
