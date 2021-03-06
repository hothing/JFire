package org.nightlabs.jfire.servermanager.createorganisation;


public enum CreateOrganisationStep {

// The expected steps to be performed in this order:
	JFireServerManagerFactory_createOrganisation_createDatabase_begin,
	JFireServerManagerFactory_createOrganisation_createDatabase_end,
	JFireServerManagerFactory_createOrganisation_deployJDO_begin,
	JFireServerManagerFactory_createOrganisation_deployJDO_end,

	OrganisationManagerHelper_initializeEmptyOrganisation_step1_begin,
	OrganisationManagerHelper_initializeEmptyOrganisation_step1_end,
	OrganisationManagerHelper_initializeEmptyOrganisation_step2_begin,
	OrganisationManagerHelper_initializeEmptyOrganisation_step2_end,
	OrganisationManagerHelper_initializeEmptyOrganisation_step3_begin,
	OrganisationManagerHelper_initializeEmptyOrganisation_step3_end,

	DatastoreInitManager_initialiseDatastore_begin,
	DatastoreInitManager_initialiseDatastore_endWithSuccess,
	DatastoreInitManager_initialiseDatastore_endWithError,

// The sourceID for an error that happened during createOrganisation
// and escalated (the datastore-init errors don't esacalate).
	JFireServerManagerFactory_createOrganisation_error

}
