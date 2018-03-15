/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2006 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 ******************************************************************************/
package org.nightlabs.jfire.base.admin.ui.editor.localorganisation;

import javax.jdo.FetchPlan;

import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.dao.OrganisationDAO;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * A controller that loads an {@link Organisation} user with its {@link Person}.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class LocalOrganisationPropertiesController extends ActiveEntityEditorPageController<Organisation>
{
	/**
	 * The fetch-groups used to retrieve and store the Organisation.
	 */
	private static final String[] FETCH_GROUPS = new String[] {
		FetchPlan.DEFAULT,
		Organisation.FETCH_GROUP_PERSON,
		PropertySet.FETCH_GROUP_FULL_DATA
	};

	/**
	 * The {@link OrganisationID} extracted form the editors input.
	 */
	private OrganisationID organisationID;
	/**
	 * The {@link StructLocal} the {@link Organisation}s {@link Person} is build from.
	 */
	private StructLocal structLocal;

	/**
	 * Create an instance of this controller for
	 * an {@link LocalOrganisationEditor} and load the data.
	 */
	public LocalOrganisationPropertiesController(EntityEditor editor)
	{
		super(editor);
		this.organisationID = ((LocalOrganisationEditorInput) editor.getEditorInput()).getJDOObjectID();
	}

	/**
	 * Get the {@link OrganisationID} of the current {@link Organisation}.
	 * @return The {@link OrganisationID}.
	 */
	public OrganisationID getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return The {@link StructLocal} the properties of the current
	 *         {@link Organisation}s {@link Person} are build from.
	 *         Note, that this will be available only if {@link #isLoaded()}
	 */
	public StructLocal getStructLocal() {
		return structLocal;
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController#getEntityFetchGroups()
	 */
	@Override
	protected String[] getEntityFetchGroups() {
		return FETCH_GROUPS;
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController#retrieveEntity(org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Organisation retrieveEntity(ProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.localorganisation.LocalOrganisationPropertiesController.task.loadOrganisationData"), 4); //$NON-NLS-1$
		Organisation organisation = OrganisationDAO.sharedInstance().getOrganisation(
				organisationID, FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new SubProgressMonitor(monitor, 2));
		Person person = organisation.getPerson();
		if (person == null) {
			person = new Person(organisation.getOrganisationID(), PropertySet.TEMPORARY_PROP_ID);
//			person.setStructScope(Struct.DEFAULT_SCOPE);
//			person.setStructLocalScope(StructLocal.DEFAULT_SCOPE);
			organisation.setPerson(person);
		}
		structLocal = StructLocalDAO.sharedInstance().getStructLocal(
				person.getStructLocalObjectID(), new SubProgressMonitor(monitor, 2)
		);
		monitor.done();
		return organisation;
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController#storeEntity(java.lang.Object, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Organisation storeEntity(Organisation controllerObject,
			ProgressMonitor monitor) {
		return OrganisationDAO.sharedInstance().storeLocalOrganisation(controllerObject, true, FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}
}
