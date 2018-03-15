package org.nightlabs.jfire.geography.admin.ui.templatedata.management.location;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.Location;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.admin.ui.templatedata.GeographyTemplateDataAdmin;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.AbstractNewGeographyWizard;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView;
import org.nightlabs.jfire.security.SecurityReflector;

public class LocationNewWizard 
extends AbstractNewGeographyWizard  
{
	private LocationNewWizardPage page;
	private City preselectedCity = null;
	private Region preselectedRegion = null;
	private Country preselectedCountry = null;
	
	/**
	 * Constructor for LocationNewWizard.
	 */
	public LocationNewWizard() {
		super();
		setWindowTitle(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.location.LocationNewWizard.wizardTitle")); //$NON-NLS-1$
	}

	/**
	 * Constructor for LocationNewWizard.
	 */
	public LocationNewWizard(boolean showGeoPerspectiveWhenFinished) {
		super(showGeoPerspectiveWhenFinished);
		setWindowTitle(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.location.LocationNewWizard.wizardTitle")); //$NON-NLS-1$
	}
	
	/**
	 * Adding the page to the wizard.
	 */
	@Override
	public void addPages() {		
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
		page = new LocationNewWizardPage(preselectedCountry, preselectedRegion, preselectedCity);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		final City selectedCity = page.getSelectedCity();

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					GeographyTemplateDataAdmin geoAdmin = new GeographyTemplateDataAdmin();
					String rootOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID(); //TODO Change to root orgID
					Location location = null;
					String locationID = Geography.nextLocationID(selectedCity.getCountryID(), rootOrganisationID);
					location = new Location(rootOrganisationID , locationID, selectedCity);
					I18nText i18nText = page.getGeographyNameTableComposite().getI18nText();
					location.getName().copyFrom(i18nText);
					geoAdmin.storeGeographyTemplateLocationData(location);
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

	/**
	 * Sets the preselectedCity.
	 * @param preselectedCity the preselectedCity to set
	 */
	public void setPreselectedCity(City preselectedCity) {
		this.preselectedCity = preselectedCity;
	}

	/**
	 * Sets the preselectedRegion.
	 * @param preselectedRegion the preselectedRegion to set
	 */
	public void setPreselectedRegion(Region preselectedRegion) {
		this.preselectedRegion = preselectedRegion;
	}

	/**
	 * Sets the preselectedCountry.
	 * @param preselectedCountry the preselectedCountry to set
	 */
	public void setPreselectedCountry(Country preselectedCountry) {
		this.preselectedCountry = preselectedCountry;
	}

}