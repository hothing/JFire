package org.nightlabs.jfire.base.ui.exceptionhandler;

import org.nightlabs.base.ui.exceptionhandler.errorreport.ErrorReportSenderCfMod;
import org.nightlabs.base.ui.login.LoginState;
import org.nightlabs.config.Config;
import org.nightlabs.jfire.base.RoleConstants;
import org.nightlabs.jfire.base.ui.login.LoginStateChangeEvent;
import org.nightlabs.jfire.base.ui.login.LoginStateListener;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 * @author marco schulze - marco at nightlabs dot de
 */
public class LoginStateListenerForScreenShotCfg 
implements LoginStateListener 
{
	@Override
	public void loginStateChanged(LoginStateChangeEvent event) {
		if (event.getNewLoginState() == LoginState.LOGGED_IN) {
			ErrorReportSenderCfMod cfMod = Config.sharedInstance().createConfigModule(ErrorReportSenderCfMod.class);
			cfMod.setAttachScreenShotToErrorReport_default(
					SecurityReflector.authorityContainsRoleRef(null, RoleConstants.attachScreenShotToErrorReport_default)
			);
			cfMod.setAttachScreenShotToErrorReport_decide(
					SecurityReflector.authorityContainsRoleRef(null, RoleConstants.attachScreenShotToErrorReport_decide)
			);
		}
	}
}
