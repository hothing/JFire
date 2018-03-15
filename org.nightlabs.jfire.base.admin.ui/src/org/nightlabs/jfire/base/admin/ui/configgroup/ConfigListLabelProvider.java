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

import javax.jdo.JDOHelper;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.nightlabs.jfire.base.ui.config.ConfigSetupRegistry;
import org.nightlabs.jfire.base.ui.config.ConfigSetupVisualiser;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.dao.ConfigSetupDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class ConfigListLabelProvider implements ITableLabelProvider
{
	private ConfigID configGroupID = null;
	
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}

	public String getColumnText(Object element, int columnIndex)
	{
		if(configGroupID == null)
			throw new IllegalStateException("No config group id set"); //$NON-NLS-1$
			
    if(!(element instanceof Config))
      throw new RuntimeException("Invalid object type, expected Config"); //$NON-NLS-1$

    Config conf = (Config)element;
		ConfigSetup configSetup = ConfigSetupDAO.sharedInstance().getConfigSetupForGroup(configGroupID, new NullProgressMonitor());
		ConfigSetupVisualiser visualiser = ConfigSetupRegistry.sharedInstance().getVisualiser(configSetup.getConfigSetupType());

    switch(columnIndex)
    {
      case 0:
        return visualiser.getKeyObjectName((ConfigID)JDOHelper.getObjectId(conf));
    }
    return null;
	}

	public void addListener(ILabelProviderListener listener)
	{
	}

	public void dispose()
	{
	}

	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	public void removeListener(ILabelProviderListener listener)
	{
	}

	public ConfigID getConfigGroupID()
	{
		return configGroupID;
	}

	public void setConfigGroupID(ConfigID configGroupID)
	{
		this.configGroupID = configGroupID;
	}
}
