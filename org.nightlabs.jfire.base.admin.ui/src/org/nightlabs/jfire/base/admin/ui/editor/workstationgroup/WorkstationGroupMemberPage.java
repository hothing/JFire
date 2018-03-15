package org.nightlabs.jfire.base.admin.ui.editor.workstationgroup;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageControllerModifyEvent;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup.UserConfigGroupMemberPage;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class WorkstationGroupMemberPage
extends EntityEditorPageWithProgress
{
	/**
	 * The Factory is registered to the extension-point and creates
	 * new instances of {@link UserConfigGroupMemberPage}.
	 */
	public static class Factory implements IEntityEditorPageFactory {
		public IFormPage createPage(FormEditor formEditor) {
			return new WorkstationGroupMemberPage(formEditor);
		}

		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new WorkstationGroupMemberPageController(editor);
		}
	}
	
	public WorkstationGroupMemberPage(FormEditor editor) {
		super(editor, WorkstationGroupMemberPage.class.getName(), Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.workstationgroup.WorkstationGroupMemberPage.title")); //$NON-NLS-1$
	}

	private WorkstationGroupMemberSection workstationGroupMemberSection;
	public WorkstationGroupMemberSection getWorkstationGroupMemberSection() {
		return workstationGroupMemberSection;
	}
	
	@Override
	protected void addSections(Composite parent) {
		workstationGroupMemberSection = new WorkstationGroupMemberSection(this, parent);
		getManagedForm().addPart(workstationGroupMemberSection);
	}

	@Override
	protected void handleControllerObjectModified(EntityEditorPageControllerModifyEvent modifyEvent) {
		final WorkstationGroupMemberPageController controller = (WorkstationGroupMemberPageController) getPageController();
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				workstationGroupMemberSection.getConfigGroupMembersEditComposite().setConfigGroupID(
						controller.getConfigID());
				switchToContent();
			}
		});
	}

	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.workstationgroup.WorkstationGroupMemberPage.pageFormTitle"); //$NON-NLS-1$
	}

}
