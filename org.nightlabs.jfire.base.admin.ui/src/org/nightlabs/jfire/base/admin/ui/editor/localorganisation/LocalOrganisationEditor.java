/**
 * 
 */
package org.nightlabs.jfire.base.admin.ui.editor.localorganisation;

import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jfire.base.ui.login.part.ICloseOnLogoutEditorPart;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * {@link EntityEditor} to edit the local {@link Organisation}s. 
 * Opened this editor with an {@link LocalOrganisationEditorInput}.
 *   
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class LocalOrganisationEditor extends EntityEditor implements ICloseOnLogoutEditorPart {

	public static final String ID_EDITOR = LocalOrganisationEditor.class.getName(); 
	
	/**
	 * Create a new {@link LocalOrganisationEditor}. 
	 */
	public LocalOrganisationEditor() {
	}
	

}
