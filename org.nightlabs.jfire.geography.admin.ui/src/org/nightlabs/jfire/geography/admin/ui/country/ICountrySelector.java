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

import java.util.List;

import javax.jdo.FetchPlan;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.nightlabs.jfire.geography.Country;

public interface ICountrySelector extends ISelectionProvider
{
	public static final String[] FETCH_GROUPS_COUNTRY = new String[] { FetchPlan.DEFAULT, Country.FETCH_GROUP_NAME };

	/**
	 * Add the <tt>Country</tt> to the list. This is called by the {@link AddCountryWizard},
	 * after it has stored the object into the datastore.
	 *
	 * @param country
	 */
	void addCountry(Country country);

	/**
	 * @return instances of {@link Country}.
	 */
	List<Country> getCountries();

	/**
	 * @return Returns the currently selected <tt>Country</tt> or <tt>null</tt> if
	 *		none is selected.
	 */
	Country getSelectedCountry();

	/**
	 * This method checks by primary key comparison, whether this <tt>ICountrySelector</tt>
	 * has the given <tt>Country</tt> loaded.
	 *
	 * @param country The <tt>Country</tt> for which to check whether it already exists.
	 * @return Returns <tt>true</tt>, if the countries contain the given <tt>country</tt>, otherwise <tt>false</tt>.
	 */
	boolean containsCountry(Country country);
}
