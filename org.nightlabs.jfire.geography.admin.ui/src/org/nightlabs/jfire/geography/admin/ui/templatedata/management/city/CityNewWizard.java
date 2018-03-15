package org.nightlabs.jfire.geography.admin.ui.templatedata.management.city;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.admin.ui.templatedata.GeographyTemplateDataAdmin;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.AbstractNewGeographyWizard;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView;
import org.nightlabs.jfire.security.SecurityReflector;

public class CityNewWizard extends AbstractNewGeographyWizard 
{
	private CityNewWizardPage page;
	private Region preselectedRegion = null;
	private Country preselectedCountry = null;

	/**
	 * Constructor for CityNewWizard.
	 */
	public CityNewWizard() {
		super();
		setWindowTitle(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.city.CityNewWizard.wizardTitle")); //$NON-NLS-1$
	}

	/**
	 * Constructor for CityNewWizard.
	 */
	public CityNewWizard(boolean openGeoPerspective) {
		super(openGeoPerspective);
		setWindowTitle(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.city.CityNewWizard.wizardTitle")); //$NON-NLS-1$
	}
	
	/**
	 * Adding the page to the wizard.
	 */
	@Override
	public void addPages() {
		GeographyTemplateDataTreeView geographyTemplateDataTreeView = getGeographyTemplateDataTreeView() ;
		if (geographyTemplateDataTreeView != null) {
			if (geographyTemplateDataTreeView.getSelectedRegionTreeNode() != null) {
				preselectedRegion = geographyTemplateDataTreeView.getSelectedRegionTreeNode().getRegion();	
			}
			if (geographyTemplateDataTreeView.getSelectedCountryTreeNode() != null) {
				preselectedCountry = geographyTemplateDataTreeView.getSelectedCountryTreeNode().getCountry();
			}
		}
		page = new CityNewWizardPage(preselectedCountry, preselectedRegion);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		final Region selectedRegion = page.getSelectedRegion();

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					GeographyTemplateDataAdmin geoAdmin = new GeographyTemplateDataAdmin();
					String rootOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID(); //TODO Change to root orgID
					City city = null;
					String cityID = Geography.nextCityID(selectedRegion.getCountryID(), rootOrganisationID);
					city = new City(rootOrganisationID , cityID, selectedRegion);
					I18nText i18nText = page.getGeographyNameTableComposite().getI18nText();
					city.getName().copyFrom(i18nText);
					geoAdmin.storeGeographyTemplateCityData(city);
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