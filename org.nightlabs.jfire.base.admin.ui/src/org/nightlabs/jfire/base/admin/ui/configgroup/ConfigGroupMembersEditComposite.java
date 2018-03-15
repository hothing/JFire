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

import java.rmi.RemoteException;
import java.util.HashSet;

import javax.security.auth.login.LoginException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.ModuleException;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.notification.IDirtyStateManager;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigManagerRemote;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.dao.ConfigSetupDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */

public class ConfigGroupMembersEditComposite
extends XComposite
{
//	private XComposite wrapperComposite;

//	private TableViewer negativeViewer;
//	private TableViewer positiveViewer;
//	private ConfigListContentProvider contentProvider;
//	private ConfigListLabelProvider labelProvider;
//
//	private Label groupMembersTitle;
//	private Label availConfigsTitle;

//	private Button removeFromGroupButton;
//	private Button addToGroupButton;

	private ConfigSetup configSetup;
//	private ConfigID currentConfigGroupID = null;

//	private List<Config> assignedConfigs;
//	private List<Config> excludedConfigs;

	private IDirtyStateManager dirtyStateManager;
//	private String availableMessage;
//	private String selectedMessage;
	private String memberName;

	private ConfigGroupMembersTable memberTable;
private ConfigGroupModel model;

//	public ConfigGroupMembersEditComposite(Composite parent, int style, boolean setLayoutData) {
//	this(parent, style, setLayoutData, null);
//	}

	public ConfigGroupMembersEditComposite(Composite parent, int style, boolean setLayoutData,
			IDirtyStateManager dirtyStateManager, String memberName)
	{
		super(parent, style, LayoutMode.ORDINARY_WRAPPER, setLayoutData ? LayoutDataMode.GRID_DATA : null);
		this.dirtyStateManager = dirtyStateManager;
//		this.availableMessage = availableMessage;
//		this.selectedMessage = selectedMessage;
		this.memberName = memberName;

		createContents(this);
		try {
			Login.getLogin();
		}
		catch (LoginException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent)
	{
		memberTable = new ConfigGroupMembersTable(parent, SWT.NONE, dirtyStateManager, memberName);
		return memberTable;
//		wrapperComposite = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
//		wrapperComposite.getGridLayout().numColumns = 3;
//
//		availConfigsTitle = new Label(wrapperComposite, SWT.WRAP);
////		availConfigsTitle.setText("Users available\n(and in other groups)");
//		availConfigsTitle.setText(availableMessage);
//		availConfigsTitle.setLayoutData(new GridData());
//
//		Composite buttonComp = new Composite(wrapperComposite, SWT.NONE);
//		GridData gd = new GridData(GridData.VERTICAL_ALIGN_CENTER);
//		gd.verticalSpan = 2;
//		buttonComp.setLayoutData(gd);
//		RowLayout rl = new RowLayout(SWT.VERTICAL);
//		buttonComp.setLayout(rl);
//
//		Button buttonAssign = new Button(buttonComp, SWT.PUSH);
//		buttonAssign.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.configgroup.ConfigGroupMembersEditComposite.buttonAssign.text")); //$NON-NLS-1$
//		buttonAssign.addSelectionListener(
//				new SelectionListener()
//				{
//					public void widgetSelected(SelectionEvent event)
//					{
//						assignConfigs();
//					}
//					public void widgetDefaultSelected(SelectionEvent event)
//					{
//						assignConfigs();
//					}
//				}
//		);
//
//		Button buttonRemove = new Button(buttonComp, SWT.PUSH);
//		buttonRemove.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.configgroup.ConfigGroupMembersEditComposite.buttonRemove.text")); //$NON-NLS-1$
//		buttonRemove.addSelectionListener(
//				new SelectionListener()
//				{
//					public void widgetSelected(SelectionEvent event)
//					{
//						removeConfigs();
//					}
//					public void widgetDefaultSelected(SelectionEvent event)
//					{
//						removeConfigs();
//					}
//				}
//		);
//
//		groupMembersTitle = new Label(wrapperComposite, SWT.WRAP);
////		groupMembersTitle.setText("Users in the \nselected group");
//		groupMembersTitle.setText(selectedMessage);
//		groupMembersTitle.setLayoutData(new GridData());
//
//		contentProvider = new ConfigListContentProvider();
//		labelProvider = new ConfigListLabelProvider();
//		labelProvider.setConfigGroupID(this.currentConfigGroupID);
//
//		negativeViewer = new TableViewer(wrapperComposite, SWT.BORDER | SWT.MULTI);
//		negativeViewer.setContentProvider(contentProvider);
//		negativeViewer.setLabelProvider(labelProvider);
//		Table t = negativeViewer.getTable();
//		t.setLinesVisible(true);
//		new TableColumn(t, SWT.LEFT, 0).setText("Label from ConfigSetupVisualiser"); //$NON-NLS-1$ // TODO obtain the correct label!
//		GridData tgd = new GridData(GridData.FILL_BOTH);
//		t.setLayoutData(tgd);
//		t.setLayout(new WeightedTableLayout(new int[] {1}));
//
//		positiveViewer = new TableViewer(wrapperComposite, SWT.BORDER | SWT.MULTI);
//		positiveViewer.setContentProvider(contentProvider);
//		positiveViewer.setLabelProvider(labelProvider);
//		t = positiveViewer.getTable();
//		t.setLinesVisible(true);
//		new TableColumn(t, SWT.LEFT, 0).setText("Label from ConfigSetupVisualiser"); //$NON-NLS-1$ // TODO obtain the correct label!
//		tgd = new GridData(GridData.FILL_BOTH);
//		t.setLayoutData(tgd);
//		t.setLayout(new WeightedTableLayout(new int[] {1}));
//
//		return wrapperComposite;
	}

	public void setEntity(Object entity) {
		if (entity instanceof ConfigID)
			setConfigGroupID((ConfigID)entity);
	}

	public void setConfigGroupID(final ConfigID configGroupID)
	{
		if (model != null && model.configGroupID.equals(configGroupID))
			return;

		configSetup = null;

		model = new ConfigGroupModel();
		model.configGroupID = configGroupID;
		model.assignedConfigs = new HashSet<Config>(getConfigSetup().getConfigsForGroup(configGroupID.configKey));
		model.notAssignedConfigs = new HashSet<Config>(getConfigSetup().getConfigsNotInGroup(configGroupID.configKey));

		Display.getDefault().syncExec(
				new Runnable()
				{
					public void run()
					{
						if (model != null)
						{
							memberTable.setModel(model);
							dirtyStateManager.markUndirty();
						}
					}
				}
		);

	}

	public void refresh()
	{
		memberTable.refresh();
	}

//	private void assignConfigs()
//	{
//		if(negativeViewer.getSelection() != null)
//		{
//			if(negativeViewer.getSelection() instanceof Config)
//				assignConfig((Config)negativeViewer.getSelection());
//			else
//			{
//				Iterator<Config> i = ((IStructuredSelection)negativeViewer.getSelection()).iterator();
//				while(i.hasNext())
//					assignConfig(i.next());
//			}
//			refresh();
//
//			if (dirtyStateManager != null)
//				dirtyStateManager.markDirty();
//		}
//	}
//
//	private void assignConfig(Config conf)
//	{
//		excludedConfigs.remove(conf);
//		assignedConfigs.add(conf);
//		getConfigSetup().moveConfigToGroup(conf, currentConfigGroupID.configKey);
//	}
//
//	private void removeConfigs()
//	{
//		if(positiveViewer.getSelection() != null)
//		{
//			if(positiveViewer.getSelection() instanceof RoleGroupContainer)
//				removeConfig((Config)positiveViewer.getSelection());
//			else
//			{
//				Iterator<Config> i = ((IStructuredSelection)positiveViewer.getSelection()).iterator();
//				while(i.hasNext())
//					removeConfig(i.next());
//			}
//			refresh();
//
//			if (dirtyStateManager != null)
//				dirtyStateManager.markDirty();
//		}
//	}

//	private void removeConfig(Config conf)
//	{
//		assignedConfigs.remove(conf);
//		excludedConfigs.add(conf);
//		getConfigSetup().moveConfigToGroup(conf, null);
//	}



	public void save() throws ModuleException, RemoteException {
		try {
			ConfigManagerRemote configManager = JFireEjb3Factory.getRemoteBean(ConfigManagerRemote.class, Login.getLogin().getInitialContextProperties());
			for (Config config : model.addedConfigs) {
				getConfigSetup().moveConfigToGroup(config, model.configGroupID.configKey);
			}

			for (Config config : model.removedConfigs) {
				getConfigSetup().moveConfigToGroup(config, null);
			}

			model.assignedConfigs.addAll(model.addedConfigs);
			model.notAssignedConfigs.addAll(model.removedConfigs);
			model.assignedConfigs.removeAll(model.removedConfigs);
			model.notAssignedConfigs.removeAll(model.addedConfigs);
			model.addedConfigs.clear();
			model.removedConfigs.clear();

			configManager.storeConfigSetup(getConfigSetup().getModifiedConfigs());
			getConfigSetup().clearModifiedConfigs();
			configSetup = null;

			// TODO: Now get all modified and restore
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	protected void refreshConfigSetup()
	{
		if (model == null)
			throw new IllegalStateException("Can not retrieve ConfigSetup as no ConfigGroup is selected"); //$NON-NLS-1$
		configSetup = ConfigSetupDAO.sharedInstance().getConfigSetupForGroup(model.configGroupID, new NullProgressMonitor());
	}

	protected ConfigSetup getConfigSetup()
	{
		if (configSetup == null)
			refreshConfigSetup();
		return configSetup;
	}

}
