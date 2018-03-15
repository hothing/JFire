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

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.nightlabs.base.ui.editor.Editor2PerspectiveRegistry;
import org.nightlabs.base.ui.progress.ProgressMonitorWrapper;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jfire.base.admin.ui.editor.usersecuritygroup.UserSecurityGroupEditor;
import org.nightlabs.jfire.base.admin.ui.editor.usersecuritygroup.UserSecurityGroupEditorInput;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.dao.UserSecurityGroupDAO;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class CreateUserGroupWizard extends DynamicPathWizard implements INewWizard
{
	private CreateUserGroupPage cugPage;
	private UserSecurityGroupID createdUserSecurityGroupID;
//	private BlockBasedPropertySetEditorWizardHop propertySetEditorWizardHop;

	public CreateUserGroupWizard()
	throws LoginException, NamingException, RemoteException
	{
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle(Messages.getString("org.nightlabs.jfire.base.admin.ui.usersecuritygroup.CreateUserGroupWizard.windowTitle")); //$NON-NLS-1$
	}

	@Override
	public void addPages()
	{
		Person person = new Person(IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class));
		StructLocal personStruct = StructLocalDAO.sharedInstance().getStructLocal(
				person.getStructLocalObjectID(),
				new NullProgressMonitor()
		);
		person.inflate(personStruct);
		cugPage = new CreateUserGroupPage();
		addPage(cugPage);

//		Tobias: We don't want a usergroup to have a person, thus we prohibit editing here :)
//		propertySetEditorWizardHop = new BlockBasedPropertySetEditorWizardHop(person);
//		String msg = "Here you can edit all information for the selected contact";
//		propertySetEditorWizardHop.addWizardPage(null, "RemainingData", "Remaining data", msg);
//		addPage(propertySetEditorWizardHop.getEntryPage());
	}

	@Override
	public boolean performFinish()
	{
		final boolean[] result = new boolean[] {false};
		try {
			getContainer().run(false, false, new IRunnableWithProgress(){
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException
				{
					try {
						final UserSecurityGroupID groupID = UserSecurityGroupID.create(
								SecurityReflector.getUserDescriptor().getOrganisationID(),
								cugPage.getUserGroupID());
						UserSecurityGroup newGroup = new UserSecurityGroup(
								groupID.organisationID, groupID.userSecurityGroupID);
						newGroup.setName(cugPage.getUserName());
						newGroup.setDescription(cugPage.getUserGroupDescription());

//						newGroup.setPerson((Person)propertySetEditorWizardHop.getPropertySet());
//						newGroup.getPerson().deflate();

						UserSecurityGroupDAO.sharedInstance().storeUserSecurityGroup(
								newGroup,
								false,
								(String[]) null,
								1,
								new ProgressMonitorWrapper(monitor));

						createdUserSecurityGroupID = groupID;
						result[0] = true;
						if (!getContainer().getShell().isDisposed()) {
							getContainer().getShell().getDisplay().asyncExec(new Runnable(){
								@Override
								public void run() {
									try {
										Editor2PerspectiveRegistry.sharedInstance().openEditor(
												new UserSecurityGroupEditorInput(groupID), UserSecurityGroupEditor.EDITOR_ID);
									} catch (Exception e) {
										throw new RuntimeException(e);
									}
								}
							});
						}
					} catch (RuntimeException e) {
						throw e;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result[0];
	}

//	public UserSecurityGroupID getCreatedUserSecurityGroupID() {
//		return createdUserSecurityGroupID;
//	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// do nothing
	}
}
