package org.nightlabs.jfire.base.ui.login;

import java.util.EventObject;

import org.eclipse.jface.action.IAction;
import org.nightlabs.base.ui.login.LoginState;


/**
 * @author Fitas Amine - fitas at nightlabs dot de
 */
public class LoginStateChangeEvent
extends EventObject
{
	private static final long serialVersionUID = 1L;

	private LoginState oldLoginState  = null;
	private LoginState newLoginState  = null;
	private IAction action;

	public LoginStateChangeEvent(Object source, LoginState oldlogstate, LoginState newlogstate, IAction action)
	{
		super(source);

		if (oldlogstate == null && newlogstate == null)
			throw new NullPointerException("the loginState are null!!"); //$NON-NLS-1$

		this.oldLoginState = oldlogstate;
		this.newLoginState = newlogstate;
		this.action = action;
	}

	/**
	 * @return Returns the <code>Action</code> or <code>null</code>.
	 */
	public IAction getAction()
	{
		return this.action;
	}

	/**
	 * @return the new login state.
	 */
	public LoginState getNewLoginState()
	{
		return this.newLoginState;
	}

	/**
	 * @return the old login state.
	 */
	public LoginState getOldLoginState()
	{
		return this.oldLoginState;
	}

}
