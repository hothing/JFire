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

import java.util.Collection;

import javax.jdo.FetchPlan;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.country.ICountrySelector;
import org.nightlabs.jfire.geography.id.CountryID;

public interface IRegionSelector extends ISelectionProvider
{
	public static final String[] FETCH_GROUPS_COUNTRY_REGIONS = new String[] {
		FetchPlan.DEFAULT,
		Country.FETCH_GROUP_REGIONS,
		Region.FETCH_GROUP_NAME};

	/**
	 * @return the currently selected <tt>Country</tt>, which must be
	 *		persistent or detached (it must NOT be a new instance). This RegionSelector
	 *		will only display and select regions from this country. If there is no
	 *		current country, this method will return <tt>null</tt>.
	 */
	Country getCountry();

	ICountrySelector getCountrySelector();

	void setCountrySelector(ICountrySelector countrySelector);

	/**
	 * Use this method to manually select a country for this RegionSelector. This is not
	 * necessary if this <tt>IRegionSelector</tt> knows its {@link ICountrySelector}.
	 *
	 * @param countryID The object-id of the selected <tt>Country</tt> or <tt>null</tt>.
	 *
	 * @see #setCountrySelector(ICountrySelector)
	 * @see #getCountrySelector()
	 */
	void setCountryID(CountryID countryID);

	/**
	 * @return instances of {@link Region}.
	 */
	Collection<Region> getRegions();

	/**
	 * @return <tt>null</tt> or the currently selected <tt>Region</tt>.
	 */
	Region getSelectedRegion();

	/**
	 * This method checks by primary key comparison, whether this <tt>IRegionSelector</tt>
	 * has the given <tt>Region</tt> loaded.
	 *
	 * @param region The <tt>Region</tt> for which to check whether it already exists.
	 * @return <tt>true</tt>, if the regions contain the given <tt>Region</tt>, otherwise <tt>false</tt>.
	 */
	boolean containsRegion(Region region);

	/**
	 * Add the <tt>Region</tt> to the list. This is called by the {@link AddRegionWizard},
	 * after it has stored the object into the datastore.
	 *
	 * @param region The Region to be added.
	 */
	void addRegion(Region region);
}
