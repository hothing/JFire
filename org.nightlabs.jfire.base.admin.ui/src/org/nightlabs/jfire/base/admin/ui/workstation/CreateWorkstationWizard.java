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

package org.nightlabs.jfire.base.admin.ui.workstation;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.nightlabs.base.ui.editor.Editor2PerspectiveRegistry;
import org.nightlabs.base.ui.progress.ProgressMonitorWrapper;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jfire.base.admin.ui.editor.workstation.WorkstationEditor;
import org.nightlabs.jfire.base.admin.ui.editor.workstation.WorkstationEditorInput;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.dao.WorkstationDAO;
import org.nightlabs.jfire.workstation.id.WorkstationID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class CreateWorkstationWizard extends DynamicPathWizard implements INewWizard
{
	private CreateWorkstationPage createWorkstationPage;
	private WorkstationID createdWorkstationID;

	public CreateWorkstationWizard() {
		super();
		setWindowTitle(Messages.getString("org.nightlabs.jfire.base.admin.ui.workstation.CreateWorkstationWizard.wizardTitle")); //$NON-NLS-1$
		createWorkstationPage = new CreateWorkstationPage(Messages.getString("org.nightlabs.jfire.base.admin.ui.workstation.CreateWorkstationWizard.pageTitle")); //$NON-NLS-1$
		addPage(createWorkstationPage);
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
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
						Workstation workstation;
						WorkstationID workstationID = WorkstationID.create(SecurityReflector.getUserDescriptor().getOrganisationID(), createWorkstationPage.getWorkstationID());
						workstation = new Workstation(workstationID.organisationID, workstationID.workstationID);
						workstation.setDescription(createWorkstationPage.getWorkstationDescription());
//						WorkstationManagerRemote workstationManager = JFireEjb3Factory.getRemoteBean(WorkstationManagerRemote.class, Login.getLogin().getInitialContextProperties());
//						workstationManager.storeWorkstation(workstation, false, null, -1);
						WorkstationDAO.sharedInstance().storeWorkstation(workstation, false, null, -1, new ProgressMonitorWrapper(monitor));
						createdWorkstationID = workstationID;
						result[0] = true;
						if (!getContainer().getShell().isDisposed()) {
							getContainer().getShell().getDisplay().asyncExec(new Runnable(){
								@Override
								public void run() {
									try {
										Editor2PerspectiveRegistry.sharedInstance().openEditor(
												new WorkstationEditorInput(createdWorkstationID), WorkstationEditor.EDITOR_ID);
									} catch (Exception e) {
										throw new RuntimeException(e);
									}
								}
							});
						}
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

//	public WorkstationID getCreatedWorkstationID() {
//		return createdWorkstationID;
//	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// do nothing
	}
}
