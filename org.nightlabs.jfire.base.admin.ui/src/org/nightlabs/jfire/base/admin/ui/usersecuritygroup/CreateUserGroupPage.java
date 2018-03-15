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

package org.nightlabs.jfire.base.admin.ui.usersecuritygroup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.Formular;
import org.nightlabs.base.ui.composite.FormularChangeListener;
import org.nightlabs.base.ui.composite.FormularChangedEvent;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.wizard.DynamicPathWizardPage;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class CreateUserGroupPage extends DynamicPathWizardPage implements FormularChangeListener
{
//	private Text userGroupID;
	private Text description;
	private Text name;

	public CreateUserGroupPage()
	{
		super(CreateUserGroupPage.class.getName(), Messages.getString("org.nightlabs.jfire.base.admin.ui.usersecuritygroup.CreateUserGroupPage.title"), null); //$NON-NLS-1$
		setImageDescriptor(
				SharedImages.getWizardPageImageDescriptor(
						BaseAdminPlugin.getDefault(),
						CreateUserGroupPage.class
				));
		setDescription(Messages.getString("org.nightlabs.jfire.base.admin.ui.usersecuritygroup.CreateUserGroupPage.description")); //$NON-NLS-1$
	}

	@Override
	public Control createPageContents(Composite parent)
	{
		Formular f = new Formular(parent, SWT.NONE, this);
//		userGroupID = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.usersecuritygroup.CreateUserGroupPage.userGroupID.labelText"), null); //$NON-NLS-1$
		name = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.usersecuritygroup.CreateUserGroupPage.name.labelText"), null); //$NON-NLS-1$
		description = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.usersecuritygroup.CreateUserGroupPage.description.labelText"), null); //$NON-NLS-1$

		verifyInput();
		setControl(f);
		
		return f;
	}

	private void verifyInput()
	{
		try {
			if("".equals(getUserGroupID()))  //$NON-NLS-1$
				updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.usersecuritygroup.CreateUserGroupPage.errorUserGroupIDMissing")); //$NON-NLS-1$
			else {
				// TODO: Check for already existing UserSecurityGroup as this is no subclass of user anymore
//				JFireSecurityManagerRemote = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, Login.getLogin().getInitialContextProperties());
//				if (userManager.userIDAlreadyRegistered(UserID.create(Login.getLogin().getOrganisationID(), getUserGroupID())))
//					updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.usersecuritygroup.CreateUserGroupPage.errorUserGroupIDConflict")); //$NON-NLS-1$
//				else
				updateStatus(null);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return Returns the userSecurityGroupID.
	 */
	public String getUserGroupID()
	{
		return ObjectIDUtil.makeValidIDString(name.getText(), true);
	}

	/**
	 * Get the user name.
	 * @return the user name
	 */
	public String getUserName() {
		return name.getText();
	}

	/**
	 * @return Returns the description.
	 */
	public String getUserGroupDescription()
	{
		return description.getText();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.composite.FormularChangeListener#formularChanged(org.nightlabs.base.ui.composite.FormularChangedEvent)
	 */
	public void formularChanged(FormularChangedEvent event)
	{
		verifyInput();
	}
}
