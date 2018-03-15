package org.nightlabs.jfire.base.admin.ui.editor.user;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.User;

public class UserPersonPreferencePage extends PersonPreferencesPage {
	
	private UserDataSection userDataSection;

	/**
	 * The Factory is registered to the extension-point and creates
	 * new instances of {@link PersonPreferencesPage}.
	 */
	public static class Factory implements IEntityEditorPageFactory {
		public IFormPage createPage(FormEditor formEditor) {
			return new UserPersonPreferencePage(formEditor);
		}
		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new PersonPreferencesController(editor);
		}
	}

	public UserPersonPreferencePage(FormEditor editor) {
		super(editor);
	}
	
	@Override
	protected void addSections(Composite parent) {
		userDataSection = new UserDataSection(this, parent, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserPersonPreferencePage.userDataSectionTitle")); //$NON-NLS-1$
		getManagedForm().addPart(userDataSection);
		super.addSections(parent);
	}
	
	@Override
	protected void updateGui(PersonPreferencesController controller) {
		User user = controller.getUser();
		userDataSection.setUser(user);
	}
}
