package org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jfire.base.admin.ui.editor.user.IConfigSetupEditor;
import org.nightlabs.jfire.base.ui.login.part.ICloseOnLogoutEditorPart;
import org.nightlabs.jfire.config.id.ConfigID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class UserConfigGroupEditor
extends EntityEditor
implements IConfigSetupEditor, ICloseOnLogoutEditorPart
{
	/**
	 * The editor id.
	 */
	public static final String EDITOR_ID = UserConfigGroupEditor.class.getName();

	public UserConfigGroupEditor() {
		super();
	}

	public ConfigID getConfigID() {
		return (ConfigID)((JDOObjectEditorInput<?>) getEditorInput()).getJDOObjectID();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		super.firePropertyChange(PROP_TITLE);
	}
}
