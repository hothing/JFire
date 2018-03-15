package org.nightlabs.jfire.base.admin.ui.editor.workstation;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.jfire.workstation.id.WorkstationID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class WorkstationEditorInput
extends JDOObjectEditorInput<WorkstationID>
{

	public WorkstationEditorInput(WorkstationID workstationID)
	{
		super(workstationID);
	}

}
