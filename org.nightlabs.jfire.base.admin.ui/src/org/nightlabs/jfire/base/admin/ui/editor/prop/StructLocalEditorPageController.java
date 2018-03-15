package org.nightlabs.jfire.base.admin.ui.editor.prop;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class StructLocalEditorPageController
extends AbstractStructEditorPageController<StructLocal>
{
	public StructLocalEditorPageController(EntityEditor editor) {
		super(editor);
	}

	public StructLocalEditorPageController(EntityEditor editor, boolean startBackgroundLoading) {
		super(editor, startBackgroundLoading);
	}

	@Override
	protected String[] getEntityFetchGroups() {
		return StructLocalDAO.DEFAULT_FETCH_GROUPS;
	}

	@Override
	protected StructLocal retrieveEntity(ProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.prop.StructLocalEditorPageController.jon.loadingPropertySetSructure"), 2); //$NON-NLS-1$
		monitor.worked(1);
		StructLocal struct = StructLocalDAO.sharedInstance().getStructLocal(getStructLocalID(), new SubProgressMonitor(monitor, 10));
		monitor.done();
		return struct;
	}

	@Override
	protected StructLocal storeEntity(StructLocal controllerObject, ProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.prop.StructLocalEditorPageController.jon.savingPropertySetSructure"), 2); //$NON-NLS-1$
		monitor.worked(1);
		StructLocal struct = StructLocalDAO.sharedInstance().storeStructLocal(
				controllerObject, true, getEntityFetchGroups(), NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new SubProgressMonitor(monitor, 10));
		monitor.done();
		return struct;
	}

	private StructLocalID getStructLocalID() {
		return ((JDOObjectEditorInput<StructLocalID>)getEntityEditor().getEditorInput()).getJDOObjectID();		
	}
}
