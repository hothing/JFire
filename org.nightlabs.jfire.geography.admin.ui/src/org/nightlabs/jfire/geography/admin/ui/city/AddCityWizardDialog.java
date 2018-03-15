/**
 * 
 */
package org.nightlabs.jfire.geography.admin.ui.city;

import javax.jdo.JDOHelper;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jfire.geography.admin.ui.AbstractAddWizardDialog;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.city.CityNewWizard;
import org.nightlabs.jfire.geography.id.RegionID;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class AddCityWizardDialog extends AbstractAddWizardDialog {

	private ICitySelector citySelector;
	
	/**
	 * @param shell
	 * @param wizard
	 * @param citySelector
	 */
	public AddCityWizardDialog(Shell shell, DynamicPathWizard wizard, ICitySelector citySelector) {
		super(shell, wizard, Messages.getString("org.nightlabs.jfire.geography.admin.ui.city.AddCityWizardDialog.button.createCity.text")); //$NON-NLS-1$
		this.citySelector = citySelector;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.admin.ui.AbstractAddWizardDialog#newButtonPressed()
	 */
	@Override
	protected void newButtonPressed() {
		CityNewWizard cityNewWizard = new CityNewWizard(false);
		cityNewWizard.setPreselectedRegion(citySelector.getRegion());
		cityNewWizard.setPreselectedCountry(citySelector.getRegionSelector().getCountry());
		WizardDialog dlg = new WizardDialog(getShell(), cityNewWizard);
		int returnCode = dlg.open();
		if (returnCode == Window.OK) {
			// reload list
			citySelector.setRegionID((RegionID) JDOHelper.getObjectId(citySelector.getRegion()));
		}
	}

}
