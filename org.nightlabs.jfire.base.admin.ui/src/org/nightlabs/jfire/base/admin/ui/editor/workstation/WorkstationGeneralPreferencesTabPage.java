package org.nightlabs.jfire.base.admin.ui.editor.workstation;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageControllerModifyEvent;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;

public class WorkstationGeneralPreferencesTabPage extends EntityEditorPageWithProgress {
	
	private WorkstationDataSection workstationDataSection;
	
	public static final String ID_PAGE = WorkstationGeneralPreferencesTabPage.class.getName();
	
	public static class Factory implements IEntityEditorPageFactory {
		public IFormPage createPage(FormEditor formEditor) {
			return new WorkstationGeneralPreferencesTabPage(formEditor);
		}

		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new WorkstationPreferencesController(editor);
		}
	}

	public WorkstationGeneralPreferencesTabPage(FormEditor editor) {
		super(editor, ID_PAGE, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.workstation.WorkstationGeneralPreferencesTabPage.title.general")); //$NON-NLS-1$
	}
	
	@Override
	protected void addSections(Composite parent) {
		workstationDataSection = new WorkstationDataSection(this, parent, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.workstation.WorkstationGeneralPreferencesTabPage.section.workstationData")); //$NON-NLS-1$
		getManagedForm().addPart(workstationDataSection);
	}

	@Override
	protected void handleControllerObjectModified(EntityEditorPageControllerModifyEvent modifyEvent) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				workstationDataSection.setWorkstation(((WorkstationPreferencesController)getPageController()).getControllerObject());
				switchToContent();
			}
		});
	}

	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.workstation.WorkstationGeneralPreferencesTabPage.page.title.general"); //$NON-NLS-1$
	}
}
