package org.nightlabs.jfire.base.ui.organisation;

import java.util.Collection;

import org.nightlabs.jfire.organisation.id.OrganisationID;

public interface OrganisationIDDataSource
{
	Collection<OrganisationID> getOrganisationIDs();
}
