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

package org.nightlabs.jfire.base.ui.login;

import org.eclipse.jface.action.IAction;
import org.nightlabs.base.ui.login.LoginState;

/**
 * LoginStateListeners are notified whenever the login state
 * of the RCP client changes.
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public interface LoginStateListener {
	
	/**
	 * Called whenever the login state changed to one of the {@link LoginState} values.
	 * Note that the param action is likely to be null, depending on what was
	 * passed to {@link Login#addLoginStateListener(LoginStateListener)}
	 * or {@link Login#addLoginStateListener(LoginStateListener, IAction)}
	 * @param oldLoginState the login state before the change.
	 * @param newLoginState the new login state, the user switched to, which is now the current state already.
	 * @param action A action associated to this listener
	 *
	 * @see Login#addLoginStateListener(LoginStateListener)
	 * @see Login#addLoginStateListener(LoginStateListener, IAction)
	 */
	public void loginStateChanged(LoginStateChangeEvent event);

//	/**
//	 * Called before the login state changes. Especially in case of logout, this
//	 * might be useful since the user is still logged in when this method is called.
//	 * @param oldLoginState the login state before the change, which is still the current state.
//	 * @param newLoginState the new login state, the system is about to switch to.
//	 * @param action
//	 */
//	public void beforeLoginStateChange(LoginStateChangeEvent event);
}
