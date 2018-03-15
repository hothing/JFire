package org.nightlabs.jfire.base.ui.login;

import javax.security.auth.login.LoginException;

import org.nightlabs.base.ui.login.ILoginDelegate;
import org.nightlabs.base.ui.login.LoginState;

public class LoginDelegate implements ILoginDelegate {

	public LoginState getLoginState()
	{
				
		return Login.sharedInstance().getLoginState();
		

	}

	public void login()
	throws LoginException
	{
		Login.getLogin();
	}

	public void logout() {
		Login.sharedInstance().logout();
	}

}
