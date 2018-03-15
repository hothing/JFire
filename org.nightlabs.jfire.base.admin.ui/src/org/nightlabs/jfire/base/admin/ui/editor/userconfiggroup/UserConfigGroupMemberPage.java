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

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class UserConfigGroupMemberPage
extends EntityEditorPageWithProgress
{
	/**
	 * The Factory is registered to the extension-point and creates
	 * new instances of {@link UserConfigGroupMemberPage}.
	 */
	public static class Factory implements IEntityEditorPageFactory {
		public IFormPage createPage(FormEditor formEditor) {
			return new UserConfigGroupMemberPage(formEditor);
		}

		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new UserConfigGroupMemberPageController(editor);
		}
	}
	
	public UserConfigGroupMemberPage(FormEditor editor) {
		super(editor, UserConfigGroupMemberPage.class.getName(), Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup.UserConfigGroupMemberPage.title")); //$NON-NLS-1$
	}

	private UserConfigGroupMemberSection userConfigGroupMemberSection;
	public UserConfigGroupMemberSection getUserConfigGroupMemberSection() {
		return userConfigGroupMemberSection;
	}
	
	@Override
	protected void addSections(Composite parent)
	{
		userConfigGroupMemberSection = new UserConfigGroupMemberSection(this, parent);
		getManagedForm().addPart(userConfigGroupMemberSection);
	}
	
	@Override
	protected void handleControllerObjectModified(EntityEditorPageControllerModifyEvent modifyEvent) {
		final UserConfigGroupMemberPageController controller = (UserConfigGroupMemberPageController) getPageController();
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				userConfigGroupMemberSection.getConfigGroupMembersEditComposite().setConfigGroupID(
						controller.getConfigID());
				switchToContent();
			}
		});
	}

	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup.UserConfigGroupMemberPage.pageFormTitle"); //$NON-NLS-1$
	}

}

