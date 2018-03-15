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

package org.nightlabs.jfire.base.admin.ui.user;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.Formular;
import org.nightlabs.base.ui.composite.FormularChangeListener;
import org.nightlabs.base.ui.composite.FormularChangedEvent;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.wizard.DynamicPathWizardPage;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.UserID;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class CreateUserPage extends DynamicPathWizardPage implements FormularChangeListener
{
	private Text userID;
	private Text password0;
	private Text password1;
	private Text description;
	private Text name;
	private Button autogenerateNameCheckbox;

	public CreateUserPage()
	{
		super(
				CreateUserPage.class.getName(),
				Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserPage.title"),  //$NON-NLS-1$
				SharedImages.getWizardPageImageDescriptor(BaseAdminPlugin.getDefault(), CreateUserPage.class)
		);
		setDescription(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserPage.description")); //$NON-NLS-1$
	}

	@Override
	public Control createPageContents(Composite parent) {
		Formular f = new Formular(parent, SWT.NONE, this);

		userID = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserPage.userID.labelText"), null); //$NON-NLS-1$
		autogenerateNameCheckbox = f.addCheckBox(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserPage.checkbox.autogenerateName"), false); //$NON-NLS-1$
		name = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserPage.name.labelText"), null); //$NON-NLS-1$
		description = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserPage.description.labelText"), null); //$NON-NLS-1$
		password0 = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserPage.password.labelText"), null); //$NON-NLS-1$
		password0.setEchoChar('*');
		password1 = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserPage.passwordConfirmation.labelText"), null); //$NON-NLS-1$
		password1.setEchoChar('*');

		// listeners
		autogenerateNameCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				autogenerateNameCheckboxToggled();
			}
		});
		autogenerateNameCheckbox.setSelection(true);
		autogenerateNameCheckboxToggled();

//		verifyInput();
		setControl(f);

		return f;
	}

	private void autogenerateNameCheckboxToggled() {
		name.setEnabled(!autogenerateNameCheckbox.getSelection());
	}
	
	private boolean pristine = true;

	private void verifyInput()
	{
		try
		{
			if(getUserID().length() == 0)
				updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserPage.errorUserIDMissing")); //$NON-NLS-1$
			else if (getUserID().startsWith(User.USER_ID_PREFIX_TYPE_ORGANISATION))
				updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserPage.errorUserIDBeginsWithIllegalCharacter") + User.USER_ID_PREFIX_TYPE_ORGANISATION); //$NON-NLS-1$
			else if (!ObjectIDUtil.isValidIDString(getUserID()))
				updateStatus("The user-ID is not a valid identifier!");
			//      else if (getUserID().startsWith(User.USERID_PREFIX_TYPE_USERGROUP))
			//      	updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserPage.errorUserIDBeginsWithIllegalCharacter") + User.USERID_PREFIX_TYPE_USERGROUP); //$NON-NLS-1$
			else {
				// TODO we should ask for a list of all userIDs and cache it here - so we don't need to ask the server again and again
				// especially, all expensive work should be done asynchronously - not on the UI thread!
				JFireSecurityManagerRemote userManager = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, Login.getLogin().getInitialContextProperties());
				if (userManager.userIDAlreadyRegistered(UserID.create(Login.getLogin().getOrganisationID(), getUserID())) == true)
					updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserPage.errorUserIDCollision")); //$NON-NLS-1$
				else if("".equals(getPassword0())) //$NON-NLS-1$ //$NON-NLS-2$
					updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserPage.errorPasswordMissing")); //$NON-NLS-1$
				else if (!UserLocal.isValidPassword(getPassword0()))
					updateStatus("The password is not valid (probably it's too short).");
				else if(!getPassword0().equals(getPassword1()))
					updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserPage.errorPasswordConfirmationDoesNotMatch")); //$NON-NLS-1$
				else
					updateStatus(null);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean isPageComplete() {
		return !pristine && super.isPageComplete();
	}

	/**
	 * Get the user name
	 * @return the user name
	 */
	public String getUserName() {
		return name.getText();
	}

	/**
	 * Get the user description.
	 * @return the user description
	 */
	public String getUserDescription()
	{
		return description.getText();
	}

	/**
	 * Get the password0.
	 * @return the password0
	 */
	public String getPassword0()
	{
		return password0.getText();
	}

	/**
	 * Get the password1.
	 * @return the password1
	 */
	public String getPassword1()
	{
		return password1.getText();
	}

	/**
	 * Get the userID.
	 * @return the userID
	 */
	public String getUserID()
	{
		return userID.getText();
	}

	/**
	 * Returns whether the user name should be generated automatically or not.
	 * @return whether the user name should be generated automatically or not.
	 */
	public boolean isAutogenerateName() {
		return autogenerateNameCheckbox.getSelection();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.composite.FormularChangeListener#formularChanged(org.nightlabs.base.ui.composite.FormularChangedEvent)
	 */
	public void formularChanged(FormularChangedEvent event)
	{
		pristine = false;
		verifyInput();
	}


}
