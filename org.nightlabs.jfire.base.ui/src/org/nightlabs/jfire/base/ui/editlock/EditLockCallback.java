package org.nightlabs.jfire.base.ui.editlock;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class EditLockCallback
{
	/**
	 * After there was no user-activity for a certain time (i.e. the EditLock was not reacquired by a follow-up call to
	 * {@link EditLockMan#acquireEditLock(org.nightlabs.jfire.editLock.id.EditLockTypeID, org.nightlabs.jdo.query.ui.ObjectID, String, EditLockCallback, org.eclipse.swt.widgets.Shell, IProgressMonitor)}),
	 * this method will be called. Depending on the return value, different actions take place:
	 * <ul>
	 * <li>{@link InactivityAction#DIALOG_ABOUT_TO_EXPIRE}: A dialog is presented to notify the user about the existing locks. If the user does
	 * not react, the locks are released and the method {@link #doDiscardAndRelease()} is called.</li>
	 * <li>{@link InactivityAction#DIALOG_BLOCKING_DUE_TO_INACTIVITY}: A dialog is presented to notify the user about the existing locks. The user
	 * can specify an action for each existing lock.</li>
	 * <li>{@link InactivityAction#REFRESH_LOCK}: The corresponding lock is refreshed and no additional action is performed.</li>
	 * <li>{@link InactivityAction#RELEASE_LOCK}: The corresponding lock is released and no additional action is performed.</li>
	 * </ul>
	 */
	public abstract InactivityAction getEditLockAction(EditLockCarrier editLockCarrier);

	/**
	 * This method is called if you returned {@link InactivityAction#DIALOG_BLOCKING_DUE_TO_INACTIVITY} in {@link #getEditLockAction(EditLockCarrier)}
	 * and the user selected the action "Save changes" for this edit lock.<br />
	 * You should react on this action for example by saving the state of the editor and closing it afterwards. The corresponding edit lock is
	 * automatically released after this method call returns.
	 */
	public void doSaveAndRelease() {}
	
	/**
	 * This method is called if either
	 * <ul>
	 * <li>you returned {@link InactivityAction#DIALOG_BLOCKING_DUE_TO_INACTIVITY} in {@link #getEditLockAction(EditLockCarrier)}
	 * and the user selected the action "Discard changes" for this edit lock</li>
	 * <li>you returned {@link InactivityAction#DIALOG_ABOUT_TO_EXPIRE} in {@link #getEditLockAction(EditLockCarrier)} and the user has clicked
	 * on the OK button or the count-down has expired.</li>
	 * </ul>
	 * You should react on this action for example by discarding the state of the editor and closing it afterwards. The corresponding edit lock is
	 * automatically released after this method call returns.
	 */
	public void doDiscardAndRelease() {}
	
	/**
	 * This method is called if either
	 * <ul>
	 * <li>you returned {@link InactivityAction#DIALOG_BLOCKING_DUE_TO_INACTIVITY} in {@link #getEditLockAction(EditLockCarrier)}
	 * and the user selected the action "Continue editing" for this edit lock</li>
	 * <li>you returned {@link InactivityAction#DIALOG_BLOCKING_DUE_TO_INACTIVITY} in {@link #getEditLockAction(EditLockCarrier)} and the user has clicked
	 * on the cancel button</li>
	 * </ul>
	 * Normally, there should be no need to react on this action, but if you want to do something, use this method. The corresponding edit lock is
	 * automatically refreshed after this method call returns.
	 */
	public void doContinueAndRefresh() {}
}
