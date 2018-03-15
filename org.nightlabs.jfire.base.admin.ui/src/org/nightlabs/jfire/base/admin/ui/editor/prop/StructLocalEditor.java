/**
 *
 */
package org.nightlabs.jfire.base.admin.ui.editor.prop;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jfire.base.ui.login.part.ICloseOnLogoutEditorPart;
import org.nightlabs.jfire.prop.id.StructLocalID;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class StructLocalEditor extends EntityEditor
implements ICloseOnLogoutEditorPart
{

	/**
	 *
	 */
	public StructLocalEditor() {
		super();
	}

	@Override
	public String getTitle() {
		if(getEditorInput() == null)
			return super.getTitle();

		final StructLocalID structID = (StructLocalID)((JDOObjectEditorInput<?>)getEditorInput()).getJDOObjectID();
		return structID.linkClass.substring(structID.linkClass.lastIndexOf(".")+1); //$NON-NLS-1$
	}
}
