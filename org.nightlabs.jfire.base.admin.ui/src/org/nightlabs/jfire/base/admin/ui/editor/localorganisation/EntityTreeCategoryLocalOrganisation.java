/**
 * 
 */
package org.nightlabs.jfire.base.admin.ui.editor.localorganisation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.ui.IEditorInput;
import org.nightlabs.base.ui.entity.tree.IEntityTreeCategory;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.entity.tree.ActiveJDOEntityTreeCategory;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.dao.OrganisationDAO;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * An {@link IEntityTreeCategory} that shows the current users organisation.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class EntityTreeCategoryLocalOrganisation extends ActiveJDOEntityTreeCategory<OrganisationID, Organisation> {

	public static String[] FETCH_GROUPS_ORGANISATION = new String[] {
		FetchPlan.DEFAULT, Organisation.FETCH_GROUP_PERSON
	};
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.entity.tree.ActiveJDOEntityTreeCategory#getJDOObjectClass()
	 */
	@Override
	protected Class<Organisation> getJDOObjectClass() {
		return Organisation.class;
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.entity.tree.ActiveJDOEntityTreeCategory#retrieveJDOObjects(java.util.Set, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Collection<Organisation> retrieveJDOObjects(
			Set<OrganisationID> objectIDs, ProgressMonitor monitor) {
		return Collections.singleton(
				OrganisationDAO.sharedInstance().getOrganisation(
						OrganisationID.create(SecurityReflector.getUserDescriptor().getOrganisationID()),
						FETCH_GROUPS_ORGANISATION, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor));
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.entity.tree.ActiveJDOEntityTreeCategory#retrieveJDOObjects(org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Collection<Organisation> retrieveJDOObjects(
			ProgressMonitor monitor) {
		return Collections.singleton(
				OrganisationDAO.sharedInstance().getOrganisation(
						OrganisationID.create(SecurityReflector.getUserDescriptor().getOrganisationID()),
						FETCH_GROUPS_ORGANISATION, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor));
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.entity.tree.ActiveJDOEntityTreeCategory#sortJDOObjects(java.util.List)
	 */
	@Override
	protected void sortJDOObjects(List<Organisation> objects) {
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.base.ui.entity.tree.IEntityTreeCategory#createEditorInput(java.lang.Object)
	 */
	@Override
	public IEditorInput createEditorInput(Object o) {
		if (o instanceof Organisation) {
			return new LocalOrganisationEditorInput();
		}
		return null;
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.base.ui.entity.tree.IEntityTreeCategory#createLabelProvider()
	 */
	@Override
	public ITableLabelProvider createLabelProvider() {
		return new TableLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				switch (columnIndex) {
					case 0:
						if (element instanceof Organisation)
							return getOrganisationName((Organisation) element);
						else
							return String.valueOf(element);

					default:
							return ""; //$NON-NLS-1$
				}
			}
			@Override
			public String getText(Object element) {
				if (element instanceof Organisation)
					return getOrganisationName((Organisation) element);
				else
					return String.valueOf(element);
			}
			
			private String getOrganisationName(Organisation o) {
				return String.format("%s (%s)", o.getOrganisationID(), o.getPerson().getDisplayName()); //$NON-NLS-1$
			}
		};
	}
}
