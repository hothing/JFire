package org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.jfire.config.id.ConfigID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class UserConfigGroupEditorInput
extends JDOObjectEditorInput<ConfigID>
{
	public UserConfigGroupEditorInput(ConfigID configID) {
		super(configID);
	}

}
