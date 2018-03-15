package org.nightlabs.jfire.base.ui.editlock;

import org.nightlabs.jfire.base.ui.resource.Messages;


public enum ProcessLockAction {
	REFRESH_AND_CONTINUE(Messages.getString("org.nightlabs.jfire.base.ui.editlock.ProcessLockAction.continueEditing")),  //$NON-NLS-1$
	RELEASE_AND_SAVE(Messages.getString("org.nightlabs.jfire.base.ui.editlock.ProcessLockAction.saveChanges")),  //$NON-NLS-1$
	RELEASE_AND_DISCARD(Messages.getString("org.nightlabs.jfire.base.ui.editlock.ProcessLockAction.discardChanges")); //$NON-NLS-1$
	
	private String description;
	private ProcessLockAction(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static ProcessLockAction getByDescription(String description) {
		for (ProcessLockAction action : values()) {
			if (action.getDescription().equals(description))
				return action;
		}
		return null;
	}
}
