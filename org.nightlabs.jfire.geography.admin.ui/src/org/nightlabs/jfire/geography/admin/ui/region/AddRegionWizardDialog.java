/**
 * 
 */
package org.nightlabs.jfire.geography.admin.ui.region;

import javax.jdo.JDOHelper;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jfire.geography.admin.ui.AbstractAddWizardDialog;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.region.RegionNewWizard;
import org.nightlabs.jfire.geography.id.CountryID;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class AddRegionWizardDialog extends AbstractAddWizardDialog {

	private IRegionSelector regionSelector;
	
	/**
	 * @param shell
	 * @param wizard
	 * @param regionSelector
	 */
	public AddRegionWizardDialog(Shell shell, DynamicPathWizard wizard, IRegionSelector regionSelector) {
		super(shell, wizard, Messages.getString("org.nightlabs.jfire.geography.admin.ui.region.AddRegionWizardDialog.button.createRegion.text")); //$NON-NLS-1$
		this.regionSelector = regionSelector;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.admin.ui.AbstractAddWizardDialog#newButtonPressed()
	 */
	@Override
	protected void newButtonPressed() {
		RegionNewWizard regionNewWizard = new RegionNewWizard(false);
		regionNewWizard.setPreselectedCountry(regionSelector.getCountry());
		WizardDialog dlg = new WizardDialog(getShell(), regionNewWizard);
		int returnCode = dlg.open();
		if (returnCode == Window.OK) {
			// reload list
			regionSelector.setCountryID((CountryID) JDOHelper.getObjectId(regionSelector.getCountry()));
		}
	}

}
