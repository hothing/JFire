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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.composite.LabeledText;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.base.ui.wizard.DynamicPathWizardPage;
import org.nightlabs.base.ui.wizard.IDynamicPathWizardPage;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.config.ConfigManagerRemote;
import org.nightlabs.jfire.config.UserConfigSetup;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class AddConfigGroupWizard extends DynamicPathWizard {

	public static class EntryPage extends DynamicPathWizardPage {

		private XComposite wrapper;
		private LabeledText groupIDText;
		private LabeledText groupNameText;
		private AddConfigGroupWizard configGroupWizard;
		private ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				configGroupWizard.getDynamicWizardDialog().update();
			}
		};

		public EntryPage(AddConfigGroupWizard configGroupWizard, String title) {
			super(EntryPage.class.getName(), title);
			this.configGroupWizard = configGroupWizard;
		}

		@Override
		public Control createPageContents(Composite parent) {
			wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);

			groupIDText = new LabeledText(wrapper, Messages.getString("org.nightlabs.jfire.base.admin.ui.configgroup.AddConfigGroupWizard.groupID.caption")); //$NON-NLS-1$
			groupIDText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			groupIDText.getTextControl().addModifyListener(modifyListener);
			groupNameText = new LabeledText(wrapper, Messages.getString("org.nightlabs.jfire.base.admin.ui.configgroup.AddConfigGroupWizard.groupName.caption")); //$NON-NLS-1$
			groupNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			groupNameText.getTextControl().addModifyListener(modifyListener);

			return wrapper;
		}

		@Override
		public boolean isPageComplete() {
			return (!"".equals(groupIDText.getTextControl().getText())) && //$NON-NLS-1$
						 (!"".equals(groupNameText.getTextControl().getText()));  //$NON-NLS-1$
		}

		public LabeledText getGroupIDText() {
			return groupIDText;
		}

		public LabeledText getGroupNameText() {
			return groupNameText;
		}
	}

	public AddConfigGroupWizard() {
		super();
	}

	private EntryPage entryPage;

	@Override
	public IDynamicPathWizardPage createWizardEntryPage() {
		entryPage = new EntryPage(this, Messages.getString("org.nightlabs.jfire.base.admin.ui.configgroup.AddConfigGroupWizard.entryPage.title"));  //$NON-NLS-1$
		return entryPage;
	}

	@Override
	public boolean performFinish() {
		try {
			ConfigManagerRemote configManager = JFireEjb3Factory.getRemoteBean(ConfigManagerRemote.class, Login.getLogin().getInitialContextProperties());
			configManager.addConfigGroup(
				entryPage.getGroupIDText().getTextControl().getText(),
				UserConfigSetup.CONFIG_GROUP_CONFIG_TYPE_USER_CONFIG,
				entryPage.getGroupNameText().getTextControl().getText(),
				false,
				null, 0
			);
			return true;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

}
