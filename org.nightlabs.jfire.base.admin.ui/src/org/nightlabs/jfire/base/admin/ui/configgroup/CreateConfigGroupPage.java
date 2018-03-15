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
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class CreateConfigGroupPage extends DynamicPathWizardPage implements FormularChangeListener
{
//	private Text configGroupKey;
	private Text configGroupName;

	/**
	 * The configType of the Config
	 */
	private String configGroupType;

	public CreateConfigGroupPage(String title, String configGroupType) {
		super(CreateConfigGroupPage.class.getName(), title,
				SharedImages.getWizardPageImageDescriptor(BaseAdminPlugin.getDefault(),
						CreateConfigGroupPage.class));
		setDescription(Messages.getString("org.nightlabs.jfire.base.admin.ui.configgroup.CreateConfigGroupPage.configGroupName.infoText")); //$NON-NLS-1$
		this.configGroupType = configGroupType;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.wizard.DynamicPathWizardPage#createPageContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createPageContents(Composite parent)
	{
		Formular f = new Formular(parent, SWT.NONE, this);
		configGroupName = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.configgroup.CreateConfigGroupPage.configGroupName.labelText"), null); //$NON-NLS-1$
//		configGroupKey = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.configgroup.CreateConfigGroupPage.configGroupKey.labelText"), null); //$NON-NLS-1$
//		configGroupKey.setEditable(false);
//		configGroupKey.setEnabled(false);
//		configGroupKey.setVisible(false);
		return f;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.wizard.DynamicPathWizardPage#onShow()
	 */
	@Override
	public void onShow()
	{
		super.onShow();
//		verifyInput();
	}

	protected void verifyInput()
	{
		if("".equals(getConfigGroupName())) //$NON-NLS-1$
			updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.configgroup.CreateConfigGroupPage.errorConfigGroupNameMissing")); //$NON-NLS-1$
		else
			updateStatus(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
	 */
	@Override
	public boolean isPageComplete()
	{
		return !"".equals(getConfigGroupName()); //$NON-NLS-1$
	}

	public String getConfigGroupKey()
	{
		return ObjectIDUtil.makeValidIDString(configGroupName.getText(), true);
	}

	public String getConfigGroupType()
	{
		return configGroupType;
	}

	public String getConfigGroupName()
	{
		return configGroupName.getText();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.composite.FormularChangeListener#formularChanged(org.nightlabs.base.ui.composite.FormularChangedEvent)
	 */
	public void formularChanged(FormularChangedEvent event)
	{
		if(event.getSource() == configGroupName) {
//			String newStr = configGroupName.getText().replaceAll("[^a-zA-Z0-9\\-]", "_"); //$NON-NLS-1$
			String newStr = ObjectIDUtil.makeValidIDString(configGroupName.getText());
//			configGroupKey.setText(newStr);
		}
		verifyInput();
	}
}
