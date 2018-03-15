package org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup;

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

public class ConfigGroupGeneralPreferencesTabPage extends EntityEditorPageWithProgress {
	
	private ConfigGroupDataSection configGroupDataSection;
	
	public static final String ID_PAGE = ConfigGroupGeneralPreferencesTabPage.class.getName();
	
	public static class Factory implements IEntityEditorPageFactory {
		public IFormPage createPage(FormEditor formEditor) {
			return new ConfigGroupGeneralPreferencesTabPage(formEditor);
		}

		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new ConfigGroupPreferencesController(editor);
		}
	}

	public ConfigGroupGeneralPreferencesTabPage(FormEditor editor) {
		super(editor, ID_PAGE, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup.ConfigGroupGeneralPreferencesTabPage.title.general")); //$NON-NLS-1$
	}
	
	@Override
	protected void addSections(Composite parent) {
		configGroupDataSection = new ConfigGroupDataSection(this, parent, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup.ConfigGroupGeneralPreferencesTabPage.section.configGroupData")); //$NON-NLS-1$
		getManagedForm().addPart(configGroupDataSection);
	}
	
	@Override
	protected void handleControllerObjectModified(EntityEditorPageControllerModifyEvent modifyEvent) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				configGroupDataSection.setConfigGroup(((ConfigGroupPreferencesController)getPageController()).getControllerObject());
				switchToContent();
			}
		});
	}

	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup.ConfigGroupGeneralPreferencesTabPage.page.title.general"); //$NON-NLS-1$
	}
}
