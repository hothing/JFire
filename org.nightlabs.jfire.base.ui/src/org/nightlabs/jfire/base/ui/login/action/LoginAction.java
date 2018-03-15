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

package org.nightlabs.jfire.base.ui.login.action;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.base.ui.login.LoginState;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.resource.SharedImages.ImageDimension;
import org.nightlabs.base.ui.resource.SharedImages.ImageFormat;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.login.LoginAbortedException;
import org.nightlabs.jfire.base.ui.login.LoginStateChangeEvent;
import org.nightlabs.jfire.base.ui.resource.Messages;

/**
 * @author Alexander Bieber
 * @author marco schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class LoginAction
extends LSDWorkbenchWindowActionDelegate
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(LoginAction.class);

	private static ImageDescriptor loginIcon_menu = null;
	private static ImageDescriptor logoutIcon_menu = null;

	private static ImageDescriptor loginIcon_toolbar = null;
	private static ImageDescriptor logoutIcon_toolbar = null;


	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void init(IWorkbenchWindow window) {
		super.init(window);

		if (loginIcon_menu == null)
			loginIcon_menu = SharedImages.getSharedImageDescriptor(JFireBasePlugin.getDefault(), LoginAction.class, "Login", ImageDimension._16x16, ImageFormat.png); //$NON-NLS-1$

		if (logoutIcon_menu == null)
			logoutIcon_menu = SharedImages.getSharedImageDescriptor(JFireBasePlugin.getDefault(), LoginAction.class, "Logout", ImageDimension._16x16, ImageFormat.png); //$NON-NLS-1$

		if (loginIcon_toolbar == null)
			loginIcon_toolbar = SharedImages.getSharedImageDescriptor(JFireBasePlugin.getDefault(), LoginAction.class, "Login", ImageDimension._24x24, ImageFormat.png); //$NON-NLS-1$

		if (logoutIcon_toolbar == null)
			logoutIcon_toolbar = SharedImages.getSharedImageDescriptor(JFireBasePlugin.getDefault(), LoginAction.class, "Logout", ImageDimension._24x24, ImageFormat.png); //$NON-NLS-1$

		if (Login.isLoggedIn() && action != null) {
			loginStateChanged(new LoginStateChangeEvent(this, Login.sharedInstance().getLoginState(), Login.sharedInstance().getLoginState(), action));
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		try {
			Login login = Login.getLogin(false);
			if (Login.isLoggedIn()) {
				login.logout();
//				login.workOffline();
			}
			else {
				try {
					Login.getLogin(false).setForceLogin(true);
					Login.getLogin();
				} catch (Exception e) {
					if (e instanceof LoginAbortedException || ExceptionUtils.indexOfThrowable(e, LoginException.class) >= 0)
						logger.info("User aborted login."); //$NON-NLS-1$
					else {
//					if (Login.sharedInstance().getLoginState() != LoginState.OFFLINE)
						ExceptionHandlerRegistry.asyncHandleException(e);
					}
//					logger.error("Login failed",e); //$NON-NLS-1$
				}
			}
		} catch (LoginException e) {
			ExceptionHandlerRegistry.asyncHandleException(e);
			logger.error("Login failed",e); //$NON-NLS-1$
		}
	}

	@Override
	public void loginStateChanged(LoginStateChangeEvent event)

	{
//		super.loginStateChanged(event);

		ImageDescriptor loginIcon = null;
		ImageDescriptor logoutIcon = null;

		if (action.getId().endsWith("#menu")) { //$NON-NLS-1$
			loginIcon = loginIcon_menu;
			logoutIcon = logoutIcon_menu;
		}
		else if (action.getId().endsWith("#toolbar")) { //$NON-NLS-1$
			loginIcon = loginIcon_toolbar;
			logoutIcon = logoutIcon_toolbar;
		}
		else
			throw new IllegalStateException("This action.id does not end on #menu or #toolbar!"); //$NON-NLS-1$

		if(event.getNewLoginState()== LoginState.LOGGED_IN)
		{
			action.setImageDescriptor(logoutIcon);
			action.setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.login.action.LoginAction.action.toolTipText_loggedIn")); //$NON-NLS-1$
			action.setHoverImageDescriptor(logoutIcon);
		}

		if(event.getNewLoginState() == LoginState.LOGGED_OUT)
		{
			action.setImageDescriptor(loginIcon);
			action.setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.login.action.LoginAction.action.toolTipText_loggedOut")); //$NON-NLS-1$
			action.setHoverImageDescriptor(loginIcon);
		}

//		if(event.getNewLoginState() ==LoginState.OFFLINE)
//		{
//			action.setImageDescriptor(loginIcon);
//			action.setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.login.action.LoginAction.action.toolTipText_offline")); //$NON-NLS-1$
//			action.setHoverImageDescriptor(loginIcon);
//		}
	
}

private IAction action = null;
@Override
public void selectionChanged(IAction action, ISelection selection) {
	this.action = action;
	super.selectionChanged(action, selection);
}
}
