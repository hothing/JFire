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

import javax.jdo.FetchPlan;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.region.IRegionSelector;
import org.nightlabs.jfire.geography.id.RegionID;

public interface ICitySelector extends ISelectionProvider
{
	public static final String[] FETCH_GROUPS_REGION_CITIES = new String[] {
		FetchPlan.DEFAULT,
		Region.FETCH_GROUP_CITIES,
		City.FETCH_GROUP_NAME};

	City getSelectedCity();

	boolean containsCity(City city);

	Region getRegion();

	void addCity(City city);
	
	IRegionSelector getRegionSelector();
	
	void setRegionID(RegionID regionID);
}
