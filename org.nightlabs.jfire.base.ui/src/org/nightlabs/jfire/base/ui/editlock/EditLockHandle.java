package org.nightlabs.jfire.base.ui.editlock;

import org.eclipse.swt.widgets.Shell;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.editlock.id.EditLockTypeID;
import org.nightlabs.progress.ProgressMonitor;

public class EditLockHandle {
	private EditLockTypeID editLockTypeID;
	private ObjectID objectID;
	private String description;
	private EditLockCallback editLockCallback;
	private Shell shell;

	public EditLockHandle(EditLockTypeID editLockTypeID, ObjectID objectID, String description, EditLockCallback editLockCallback, Shell shell) {
		super();
		this.editLockTypeID = editLockTypeID;
		this.objectID = objectID;
		this.description = description;
		this.editLockCallback = editLockCallback;
		this.shell = shell;
	}
	
	public EditLockTypeID getEditLockTypeID() {
		return editLockTypeID;
	}
	public ObjectID getObjectID() {
		return objectID;
	}
	public String getDescription() {
		return description;
	}
	public EditLockCallback getEditLockCallback() {
		return editLockCallback;
	}
	public Shell getShell() {
		return shell;
	}

	/**
	 * Asynchronously refresh the lock referenced by this handle (this method returns immediately). The API-consumer should call this method repeatedly
	 * in order to signal user-activity.
	 */
	public void refresh() {
		EditLockMan.sharedInstance().acquireEditLockAsynchronously(editLockTypeID, objectID, description, editLockCallback);
	}

	/**
	 * Synchronously refresh the lock referenced by this handle.
	 *
	 * @param monitor the monitor for progress feedback.
	 */
	public void refresh(ProgressMonitor monitor) {
		EditLockMan.sharedInstance().acquireEditLock(editLockTypeID, objectID, description, editLockCallback, shell, monitor);
	}

	/**
	 * Asynchronously release the lock referenced by this handle. This method returns immediately and spawns a <code>Job</code> to do the work.
	 */
	@SuppressWarnings("deprecation") //$NON-NLS-1$
	public void release() {
		EditLockMan.sharedInstance().releaseEditLock(objectID);
	}

	/**
	 * Synchronously release the lock reference by this handle. Since this method is blocking, you should
	 * not execute it on the UI thread, but either call {@link #release()} or manage a <code>Job</code> yourself.
	 *
	 * @param monitor the monitor for progress feedback.
	 */
	@SuppressWarnings("deprecation") //$NON-NLS-1$
	public void release(ProgressMonitor monitor) {
		EditLockMan.sharedInstance().releaseEditLock(objectID, monitor);
	}
}
