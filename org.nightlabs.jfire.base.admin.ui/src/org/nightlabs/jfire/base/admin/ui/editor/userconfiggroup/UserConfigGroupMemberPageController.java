package org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup;

import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jfire.base.admin.ui.editor.configgroup.AbstractConfigGroupPageController;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class UserConfigGroupMemberPageController
//extends EntityEditorPageController
extends AbstractConfigGroupPageController
{
	public UserConfigGroupMemberPageController(EntityEditor editor) {
		super(editor);
	}

	public UserConfigGroupMemberPageController(EntityEditor editor, boolean startBackgroundLoading) {
		super(editor, startBackgroundLoading);
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.base.ui.entity.editor.IEntityEditorPageController#doLoad(org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	public void doLoad(ProgressMonitor monitor)
	{
		monitor.beginTask("Load User Config Group Members", 2); //$NON-NLS-1$ // this is probably never shown since this method finishes really quickly (there's nothing to do) => we don't localise it
		monitor.worked(1);
		monitor.worked(1);
		fireModifyEvent(null, null);
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.base.ui.entity.editor.IEntityEditorPageController#doSave(org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	public boolean doSave(ProgressMonitor monitor)
	{
		for (IFormPage page : getPages()) {
			if (page instanceof UserConfigGroupMemberPage) {
				UserConfigGroupMemberPage userConfigGroupMemberPage = (UserConfigGroupMemberPage) page;
				try {
					userConfigGroupMemberPage.getUserConfigGroupMemberSection().getConfigGroupMembersEditComposite().save();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}			
		}
		fireModifyEvent(null, null);
		return true;
	}

}
