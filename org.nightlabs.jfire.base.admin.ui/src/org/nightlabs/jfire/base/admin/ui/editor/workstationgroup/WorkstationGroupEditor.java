package org.nightlabs.jfire.base.admin.ui.editor.workstationgroup;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jfire.base.admin.ui.editor.user.IConfigSetupEditor;
import org.nightlabs.jfire.base.ui.login.part.ICloseOnLogoutEditorPart;
import org.nightlabs.jfire.config.id.ConfigID;

public class WorkstationGroupEditor
extends EntityEditor
implements IConfigSetupEditor, ICloseOnLogoutEditorPart
{
	public static final String EDITOR_ID = WorkstationGroupEditor.class.getName();
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		super.firePropertyChange(PROP_TITLE);
	}

	public ConfigID getConfigID() {
		return ((JDOObjectEditorInput<ConfigID>) getEditorInput()).getJDOObjectID();
	}

}
