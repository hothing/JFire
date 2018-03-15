/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.config;

import org.eclipse.ui.PlatformUI;
import org.nightlabs.base.ui.login.LoginState;
import org.nightlabs.jfire.base.ui.login.LoginStateChangeEvent;
import org.nightlabs.jfire.base.ui.login.LoginStateListener;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class PropertySetFieldBasedEditLayoutPreferenceLoginStateListener implements
		LoginStateListener {

	/**
	 * 
	 */
	public PropertySetFieldBasedEditLayoutPreferenceLoginStateListener() {
	}

	@Override
	public void loginStateChanged(LoginStateChangeEvent event) {
		if (event.getNewLoginState() == LoginState.LOGGED_IN) {
			PlatformUI.getWorkbench().getPreferenceManager().addTo("org.nightlabs.base.ui.preference.UIPreferencePage", new PropertySetFieldBasedEditLayoutPreferenceRootNode()); //$NON-NLS-1$
		} else if (event.getNewLoginState() == LoginState.LOGGED_OUT) {
			PlatformUI.getWorkbench().getPreferenceManager().remove("org.nightlabs.base.ui.preference.UIPreferencePage/"+ PropertySetFieldBasedEditLayoutPreferenceRootNode.class.getName()); //$NON-NLS-1$
		}
	}

}
