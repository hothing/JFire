package org.nightlabs.jfire.base.admin.ui.editor.workstationgroup;

import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jfire.base.admin.ui.editor.configgroup.AbstractConfigGroupPageController;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class WorkstationGroupMemberPageController
extends AbstractConfigGroupPageController
{
	private static final long serialVersionUID = 1L;

	public WorkstationGroupMemberPageController(EntityEditor editor) {
		super(editor);
	}

	public WorkstationGroupMemberPageController(EntityEditor editor, boolean startBackgroundLoading) {
		super(editor, startBackgroundLoading);
	}

	@Override
	public void doLoad(ProgressMonitor monitor) {
		monitor.beginTask("Load Workstation Config Group Members", 1); //$NON-NLS-1$ // very fast => no externalisation necessary
		monitor.worked(1);
		monitor.done();
		fireModifyEvent(null, null);
	}

	@Override
	public boolean doSave(ProgressMonitor monitor)
	{
		for (IFormPage page : getPages()) {
			if (page instanceof WorkstationGroupMemberPage) {
				WorkstationGroupMemberPage workstationGroupMemberPage = (WorkstationGroupMemberPage) page;
				try {
					workstationGroupMemberPage.getWorkstationGroupMemberSection().getConfigGroupMembersEditComposite().save();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		fireModifyEvent(null, null);
		return true;
	}

}
