/**
 * 
 */
package org.nightlabs.jfire.base.admin.ui.editor.localorganisation;

import org.eclipse.ui.IEditorInput;
import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * {@link IEditorInput} for the {@link LocalOrganisationEditor}
 * it automatically refers to the local {@link OrganisationID}. 
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class LocalOrganisationEditorInput extends JDOObjectEditorInput<OrganisationID> {

	/**
	 * Create a new {@link LocalOrganisationEditorInput}.
	 * 
	 * @param organisationID The {@link OrganisationID} for the new editor input.
	 */
	public LocalOrganisationEditorInput() {
		super(OrganisationID.create(SecurityReflector.getUserDescriptor().getOrganisationID()));
	}

}
