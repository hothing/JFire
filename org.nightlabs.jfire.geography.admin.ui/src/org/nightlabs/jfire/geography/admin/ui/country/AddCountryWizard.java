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

package org.nightlabs.jfire.geography.admin.ui.country;

import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.dao.CountryDAO;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.progress.NullProgressMonitor;

public class AddCountryWizard extends DynamicPathWizard
{
	private ICountrySelector countrySelector;

	private AddCountryWizardEntryPage addCountryWizardEntryPage;

	public AddCountryWizard(ICountrySelector countrySelector)
	{
		this.countrySelector = countrySelector;
		this.setForcePreviousAndNextButtons(false);
	}

	@Override
	public void addPages()
	{
		super.addPages();
		addCountryWizardEntryPage = new AddCountryWizardEntryPage();
		addPage(addCountryWizardEntryPage);
	}

	@Override
	public boolean performFinish()
	{
		Country country = addCountryWizardEntryPage.getSelectedCountry().copyForJDOStorage();
		CountryID countryID = CountryID.create(country);
		country = CountryDAO.sharedInstance().importCountry(
				countryID,
				true,
				ICountrySelector.FETCH_GROUPS_COUNTRY,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				new NullProgressMonitor() // TODO asynchronous and real monitor!
		);
		countrySelector.addCountry(country);

		return true;
	}

	public ICountrySelector getCountrySelector()
	{
		return countrySelector;
	}
}
