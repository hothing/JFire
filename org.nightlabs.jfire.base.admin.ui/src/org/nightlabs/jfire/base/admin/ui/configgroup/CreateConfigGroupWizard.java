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

import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.config.ConfigManagerRemote;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class CreateConfigGroupWizard extends DynamicPathWizard {

	private CreateConfigGroupPage createConfigGroupPage;
	private ConfigID createdConfigID;

	public CreateConfigGroupWizard(String configGroupType, String configGroupWizardTitle, String configGroupPageTitle) {
		super();
		setWindowTitle(configGroupWizardTitle);
		createConfigGroupPage = new CreateConfigGroupPage(configGroupPageTitle, configGroupType);
		addPage(createConfigGroupPage);
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		try {
			ConfigID configID = ConfigID.create(
					SecurityReflector.getUserDescriptor().getOrganisationID(),
					createConfigGroupPage.getConfigGroupKey(),
					createConfigGroupPage.getConfigGroupType());
			ConfigManagerRemote configManager = JFireEjb3Factory.getRemoteBean(ConfigManagerRemote.class, Login.getLogin().getInitialContextProperties());
			configManager.addConfigGroup(
					configID.configKey,
					configID.configType,
					createConfigGroupPage.getConfigGroupName(),
					false,
					null, 0
				);
			createdConfigID = configID;
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ConfigID getCreatedConfigID() {
		return createdConfigID;
	}
}
