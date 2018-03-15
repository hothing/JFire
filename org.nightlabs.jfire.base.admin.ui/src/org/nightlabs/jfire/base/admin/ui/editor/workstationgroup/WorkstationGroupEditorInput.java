package org.nightlabs.jfire.base.admin.ui.editor.workstationgroup;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.jfire.config.id.ConfigID;

public class WorkstationGroupEditorInput
extends JDOObjectEditorInput<ConfigID>
{
	public WorkstationGroupEditorInput(ConfigID workstationConfigGroupID) {
		super(workstationConfigGroupID);
	}
}
