/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.geography.admin.ui.region;

import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.dao.RegionDAO;
import org.nightlabs.jfire.geography.id.RegionID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class AddRegionWizard extends DynamicPathWizard
{
	private IRegionSelector regionSelector;

	private AddRegionWizardEntryPage addRegionWizardEntryPage;

	public AddRegionWizard(IRegionSelector regionSelector)
	{
		this.regionSelector = regionSelector;
	}

	@Override
	public void addPages()
	{
		super.addPages();
		addRegionWizardEntryPage = new AddRegionWizardEntryPage();
		addPage(addRegionWizardEntryPage);
	}

	@Override
	public boolean performFinish()
	{
		Country persistentCountry = regionSelector.getCountry();
		Region region = addRegionWizardEntryPage.getSelectedRegion().copyForJDOStorage(persistentCountry);
		RegionID regionID = RegionID.create(region);
		region = RegionDAO.sharedInstance().importRegion(
				regionID,
				true,
				IRegionSelector.FETCH_GROUPS_COUNTRY_REGIONS,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				new NullProgressMonitor() // TODO asnychronous and real monitor!
		);
		regionSelector.addRegion(region);

		return true;
	}

	public IRegionSelector getRegionSelector()
	{
		return regionSelector;
	}

}
