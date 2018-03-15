package org.nightlabs.jfire.base.admin.ui.editor.configgroup;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.jfire.config.id.ConfigID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class AbstractConfigGroupPageController
extends EntityEditorPageController
{

	public AbstractConfigGroupPageController(EntityEditor editor) {
		super(editor);
		configID = ((JDOObjectEditorInput<ConfigID>)editor.getEditorInput()).getJDOObjectID();
	}

	public AbstractConfigGroupPageController(EntityEditor editor, boolean startBackgroundLoading) {
		super(editor, startBackgroundLoading);
		configID = ((JDOObjectEditorInput<ConfigID>)editor.getEditorInput()).getJDOObjectID();
	}
	
	protected ConfigID configID;
	public ConfigID getConfigID() {
		return configID;
	}
	
}
