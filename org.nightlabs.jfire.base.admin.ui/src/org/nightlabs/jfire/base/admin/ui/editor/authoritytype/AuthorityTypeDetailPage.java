package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageControllerModifyEvent;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.AuthorityType;

public class AuthorityTypeDetailPage extends EntityEditorPageWithProgress
{
	/**
	 * The Factory is registered to the extension-point and creates
	 * new instances of {@link AuthorityTypeDetailPage}.
	 */
	public static class Factory implements IEntityEditorPageFactory {
		public IFormPage createPage(FormEditor formEditor) {
			return new AuthorityTypeDetailPage(formEditor);
		}

		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new AuthorityTypeDetailPageController(editor);
		}
	}

	private AuthorityTypeSection authorityTypeSection;
	private RoleGroupsSection roleGroupsSection;
	private AuthorityListSection authorityListSection;

	public AuthorityTypeDetailPage(FormEditor editor) {
		super(editor, AuthorityTypeDetailPage.class.getName(), Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authoritytype.AuthorityTypeDetailPage.title.general")); //$NON-NLS-1$
	}

	@Override
	protected void addSections(Composite parent) {
		authorityTypeSection = new AuthorityTypeSection(this, parent);
		getManagedForm().addPart(authorityTypeSection);

		roleGroupsSection = new RoleGroupsSection(this, parent);
		getManagedForm().addPart(roleGroupsSection);

		authorityListSection = new AuthorityListSection(this, parent);
		getManagedForm().addPart(authorityListSection);
	}

	private AuthorityType authorityType;

	@Override
	protected void handleControllerObjectModified(EntityEditorPageControllerModifyEvent modifyEvent) {
		getManagedForm().getForm().getDisplay().asyncExec(new Runnable() {
			public void run() {
				authorityType = ((AuthorityTypeDetailPageController)getPageController()).getAuthorityType();

				authorityTypeSection.setAuthorityType(authorityType);		
				roleGroupsSection.setRoleGroups(authorityType.getRoleGroups());
				authorityListSection.setAuthorityType(authorityType);
				switchToContent();
			}
		});
	}

	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authoritytype.AuthorityTypeDetailPage.page.title.general"); //$NON-NLS-1$
	}

}
