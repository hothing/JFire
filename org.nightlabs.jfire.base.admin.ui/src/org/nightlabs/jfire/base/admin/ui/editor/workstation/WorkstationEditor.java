package org.nightlabs.jfire.base.admin.ui.editor.workstation;

import javax.jdo.FetchPlan;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.editor.user.IConfigSetupEditor;
import org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditor;
import org.nightlabs.jfire.base.ui.login.part.ICloseOnLogoutEditorPart;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.dao.WorkstationDAO;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.progress.ProgressMonitor;

public class WorkstationEditor extends ActiveEntityEditor
implements IConfigSetupEditor, ICloseOnLogoutEditorPart
{
	/**
	 * The editor id.
	 */
	public static final String EDITOR_ID = WorkstationEditor.class.getName();

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.entityeditor.EntityEditor#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		super.init(site, input);
		super.firePropertyChange(PROP_TITLE);
	}

	// :: --- [ ~~ ActiveEntiyEditor ] -------------------------------------------------------------------------->>---|
	@Override
	protected String getEditorTitleFromEntity(Object entity) {
		return entity instanceof Workstation ? getWorkstationID().workstationID : null;
	}

	@Override
	protected Object retrieveEntityForEditorTitle(ProgressMonitor monitor) {
		return WorkstationDAO.sharedInstance().getWorkstation(getWorkstationID(), new String[] {FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}
	// :: --- [ ~~ ActiveEntiyEditor ] --------------------------------------------------------------------------<<---|


//	/* (non-Javadoc)
//	 * @see org.eclipse.ui.part.WorkbenchPart#getTitle()
//	 */
//	@Override
//	public String getTitle()
//	{
//		if(getEditorInput() == null)
//			return super.getTitle();
//		return (((JDOObjectEditorInput<WorkstationID>)getEditorInput()).getJDOObjectID()).workstationID;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.eclipse.ui.part.WorkbenchPart#getTitleImage()
//	 */
//	@Override
//	public Image getTitleImage()
//	{
//		return super.getTitleImage();
//	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#getTitleToolTip()
	 */
	@Override
	public String getTitleToolTip()
	{
		// TODO: Better tool-tip
		return getWorkstationID().workstationID; // (((JDOObjectEditorInput<WorkstationID>)getEditorInput()).getJDOObjectID()).workstationID;
	}

	public ConfigID getConfigID() {
		return ConfigID.create(getWorkstationID().organisationID, getWorkstationID(), Workstation.class);
	}

	public WorkstationID getWorkstationID() {
		return ((WorkstationEditorInput)getEditorInput()).getJDOObjectID();
	}

}
