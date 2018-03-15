/**
 * 
 */
package org.nightlabs.jfire.geography.admin.ui.templatedata.management.country;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.admin.ui.templatedata.GeographyTemplateDataAdmin;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.AbstractNewGeographyWizard;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class CountryNewWizard 
extends AbstractNewGeographyWizard
{
	private CountryNewPage page;
	
	public CountryNewWizard() {
		super();
	}

	/**
	 * Adding the page to the wizard.
	 */
	@Override
	public void addPages() {
		page = new CountryNewPage();
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) 
			throws InvocationTargetException 
			{
				try {
					GeographyTemplateDataAdmin geoAdmin = new GeographyTemplateDataAdmin();
					String countryID = page.getCountryIsoCode();
					Country country = new Country(Geography.sharedInstance(), countryID);
					I18nText i18nText = page.getGeographyNameTableComposite().getI18nText();
					country.getName().copyFrom(i18nText);
					geoAdmin.storeGeographyTemplateCountryData(country);
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
