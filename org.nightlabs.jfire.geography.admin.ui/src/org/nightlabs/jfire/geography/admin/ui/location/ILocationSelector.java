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

package org.nightlabs.jfire.geography.admin.ui.location;

import java.util.Collection;

import javax.jdo.FetchPlan;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Location;
import org.nightlabs.jfire.geography.admin.ui.city.ICitySelector;
import org.nightlabs.jfire.geography.id.CityID;

public interface ILocationSelector extends ISelectionProvider
{
	public static final String[] FETCH_GROUPS_CITY_LOCATIONS = new String[] {
		FetchPlan.DEFAULT,
		City.FETCH_GROUP_LOCATIONS,
		Location.FETCH_GROUP_NAME};

	/**
	 * @return Returns the currently selected <tt>City</tt>, which must be
	 *		persistent or detached (it must NOT be a new instance). This VenueSelector
	 *		will only display and select locations from this city. If there is no
	 *		current city, this method will return <tt>null</tt>.
	 */
	City getCity();

	ICitySelector getCitySelector();

	void setCitySelector(ICitySelector citySelector);

	/**
	 * Use this method to manually select a city for this LocationSelector. This is not
	 * necessary if this <tt>ILocationSelector</tt> knows its {@link ICitySelector}.
	 *
	 * @param cityID The object-id of the selected <tt>City</tt> or <tt>null</tt>.
	 *
	 * @see #setCitySelector(ICitySelector)
	 * @see #getCitySelector()
	 */
	void setCityID(CityID cityID);

	/**
	 * @return instances of {@link Location}.
	 */
	Collection<Location> getLocations();

	/**
	 * @return Returns <tt>null</tt> or the currently selected <tt>Location</tt>.
	 */
	Location getSelectedLocation();

	/**
	 * This method checks by primary key comparison, whether this <tt>ILocationSelector</tt>
	 * has the given <tt>Location</tt> loaded.
	 *
	 * @param location The <tt>Location</tt> for which to check whether it already exists.
	 * @return Returns <tt>true</tt>, if the locations contain the given <tt>Location</tt>, otherwise <tt>false</tt>.
	 */
	boolean containsLocation(Location location);

	/**
	 * Add the <tt>Region</tt> to the list. This is called by wizards
	 * after they have stored the object into the datastore.
	 *
	 * @param location The Location to be added.
	 */
	void addLocation(Location location);

	Action getAddLocationAction();

	void setAddLocationAction(Action addLocationAction);
}
