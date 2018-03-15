package org.nightlabs.jfire.base.admin.ui.editor.workstation;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.dao.WorkstationDAO;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.progress.ProgressMonitor;

public class WorkstationPreferencesController extends ActiveEntityEditorPageController<Workstation> {
	
	private static final String[] FETCH_GROUPS = new String[] { Workstation.FETCH_GROUP_THIS_WORKSTATION };
	
	private WorkstationID workstationID;

	public WorkstationPreferencesController(EntityEditor editor) {
		super(editor);
		this.workstationID = ((JDOObjectEditorInput<WorkstationID>)editor.getEditorInput()).getJDOObjectID();
	}
	
	@Override
	protected String[] getEntityFetchGroups() {
		return FETCH_GROUPS;
	}

	@Override
	protected Workstation retrieveEntity(ProgressMonitor monitor) {
		return WorkstationDAO.sharedInstance().getWorkstation(workstationID, getEntityFetchGroups(), NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	@Override
	protected Workstation storeEntity(Workstation controllerObject, ProgressMonitor monitor) {
		return WorkstationDAO.sharedInstance().storeWorkstation(controllerObject, true, getEntityFetchGroups(), NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}
	
}
