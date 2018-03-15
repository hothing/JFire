package org.nightlabs.jfire.base.ui.organisation;

import org.nightlabs.jfire.organisation.Organisation;

public interface OrganisationFilter
{
	boolean includeOrganisation(Organisation organisation);
}
