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

package org.nightlabs.jfire.geography.admin.ui.city;

import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.dao.CityDAO;
import org.nightlabs.jfire.geography.id.CityID;
import org.nightlabs.progress.NullProgressMonitor;

public class AddCityWizard extends DynamicPathWizard
{
	private ICitySelector citySelector;
	private AddCityWizardEntryPage addCityWizardEntryPage;

	public AddCityWizard(ICitySelector citySelector)
	{
		this.citySelector = citySelector;
	}

	public ICitySelector getCitySelector()
	{
		return citySelector;
	}

	@Override
	public void addPages()
	{
		super.addPages();
		addCityWizardEntryPage = new AddCityWizardEntryPage();
		addPage(addCityWizardEntryPage);
	}

	@Override
	public boolean performFinish()
	{
		Region persistentRegion = citySelector.getRegion();
		City city = addCityWizardEntryPage.getSelectedCity().copyForJDOStorage(persistentRegion);
		CityID cityID = CityID.create(city);
		city = CityDAO.sharedInstance().importCity(
				cityID, true,
				ICitySelector.FETCH_GROUPS_REGION_CITIES,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				new NullProgressMonitor() // TODO need to do this asynchronously and use real monitor!
		);
		citySelector.addCity(city);

		return true;
	}

}
