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

package org.nightlabs.jfire.base.admin.ui.configgroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.dao.ConfigSetupDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class ConfigListContentProvider implements IStructuredContentProvider
{
	public Object[] getElements(Object o)
	{
		if(o instanceof ArrayList)
			return ((ArrayList<Object>)o).toArray();
		else
			return new Object[0];
	}

	public void dispose()
	{
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

	public List<Config> getIncludedItems(ConfigID id)
	{
		ArrayList<Config> ret = new ArrayList<Config>();
		ConfigSetup configSetup = ConfigSetupDAO.sharedInstance().getConfigSetupForGroup(id, new NullProgressMonitor());
		List<Config> configs = configSetup.getConfigsForGroup(id.configKey);
		for (Iterator<Config> iter = configs.iterator(); iter.hasNext();)
			ret.add(iter.next());
		return ret;
	}

	public List<Config> getExcludedItems(ConfigID id)
	{
		ArrayList<Config> ret = new ArrayList<Config>();
		ConfigSetup configSetup = ConfigSetupDAO.sharedInstance().getConfigSetupForGroup(id, new NullProgressMonitor());
		List<Config> configs = configSetup.getConfigsNotInGroup(id.configKey);
		for (Iterator<Config> iter = configs.iterator(); iter.hasNext();)
			ret.add(iter.next());
		return ret;
	}
}
