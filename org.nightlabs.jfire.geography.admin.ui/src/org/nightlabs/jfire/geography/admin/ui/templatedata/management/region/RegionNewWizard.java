package org.nightlabs.jfire.geography.admin.ui.templatedata.management.region;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.admin.ui.templatedata.GeographyTemplateDataAdmin;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.AbstractNewGeographyWizard;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView;
import org.nightlabs.jfire.security.SecurityReflector;

public class RegionNewWizard 
extends AbstractNewGeographyWizard 
{
	private RegionNewWizardPage page;
	private Country preselectedCountry;
	
	/**
	 * Constructor for RegionNewWizard.
	 */
	public RegionNewWizard() {
		super();
		setWindowTitle(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.region.RegionNewWizard.wizardTitle")); //$NON-NLS-1$
	}

	/**
	 * Constructor for RegionNewWizard.
	 */
	public RegionNewWizard(boolean openGeoPerspective) {
		super(openGeoPerspective);
		setWindowTitle(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.region.RegionNewWizard.wizardTitle")); //$NON-NLS-1$
	}
	
	/**
	 * Adding the page to the wizard.
	 */
	@Override
	public void addPages() {
		GeographyTemplateDataTreeView geographyTemplateDataTreeView = getGeographyTemplateDataTreeView();
		if (geographyTemplateDataTreeView != null && geographyTemplateDataTreeView.getSelectedCountryTreeNode() != null) {
			preselectedCountry = geographyTemplateDataTreeView.getSelectedCountryTreeNode().getCountry();	
		}
		page = new RegionNewWizardPage(preselectedCountry);
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
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					GeographyTemplateDataAdmin geoAdmin = new GeographyTemplateDataAdmin();
					String rootOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
					Region region = null;
					String regionID = Geography.nextRegionID(selectedCountry.getCountryID(), rootOrganisationID);
					region = new Region(rootOrganisationID , regionID, selectedCountry);
					I18nText i18nText = page.getGeographyNameTableComposite().getI18nText();
					region.getName().copyFrom(i18nText);
					geoAdmin.storeGeographyTemplateRegionData(region);
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
	 * Sets the preselectedCountry.
	 * @param preselectedCountry the preselectedCountry to set
	 */
	public void setPreselectedCountry(Country preselectedCountry) {
		this.preselectedCountry = preselectedCountry;
	}

}