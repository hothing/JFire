package org.nightlabs.jfire.geography.admin.ui.templatedata.management.district;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.District;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.admin.ui.templatedata.GeographyTemplateDataAdmin;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.AbstractNewGeographyWizard;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView;
import org.nightlabs.jfire.security.SecurityReflector;

public class DistrictNewWizard 
extends AbstractNewGeographyWizard  
{
	private DistrictNewWizardPage page;

	/**
	 * Constructor for DistrictNewWizard.
	 */
	public DistrictNewWizard(GeographyTemplateDataTreeView parentView) {
		super();
		setWindowTitle(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.district.DistrictNewWizard.wizardTitle")); //$NON-NLS-1$
	}

	/**
	 * Adding the page to the wizard.
	 */

	@Override
	public void addPages() {
		City preselectedCity = null;
		Region preselectedRegion = null;
		Country preselectedCountry = null;
		GeographyTemplateDataTreeView geographyTemplateDataTreeView = getGeographyTemplateDataTreeView();
		if (geographyTemplateDataTreeView != null) {
			if (geographyTemplateDataTreeView.getSelectedCityTreeNode() != null) {
				preselectedCity = geographyTemplateDataTreeView.getSelectedCityTreeNode().getCity();	
			}
			if (geographyTemplateDataTreeView.getSelectedRegionTreeNode() != null) {
				preselectedRegion = geographyTemplateDataTreeView.getSelectedRegionTreeNode().getRegion();	
			}
			if (geographyTemplateDataTreeView.getSelectedCountryTreeNode() != null) {
				preselectedCountry = geographyTemplateDataTreeView.getSelectedCountryTreeNode().getCountry();
			}			
		}
		page = new DistrictNewWizardPage(preselectedCountry, preselectedRegion, preselectedCity);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		final Country selectedCountry = page.getSelectedCountry();
		final City selectedCity = page.getSelectedCity();
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					// TODO District does not have a name but needs instead langitute and longitute
					GeographyTemplateDataAdmin geoAdmin = new GeographyTemplateDataAdmin();
					String rootOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID(); //TODO Change to root orgID
					District district = null;
					String districtID = Geography.nextDistrictID(selectedCountry.getCountryID(), rootOrganisationID);
					district = new District(rootOrganisationID, districtID, selectedCity);
					district.setLatitude(page.getLatitude());
					district.setLongitude(page.getLongitude());
					geoAdmin.storeGeographyTemplateDistrictData(district);
					openGeoPerspective();
				}//try
				finally {
					monitor.done();
				}//finally
			}
		};

		try {
			getContainer().run(true, false, op);
		}//try
		catch (InterruptedException e) {
			return false;
		}//catch
		catch (InvocationTargetException e) {
			throw new RuntimeException(e.getTargetException());
		}//catch

		return true;
	}

}