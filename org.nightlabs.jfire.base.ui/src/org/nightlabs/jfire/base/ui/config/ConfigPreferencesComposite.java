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

package org.nightlabs.jfire.base.ui.config;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.base.ui.resource.Messages;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ConfigPreferencesComposite extends XComposite {

	private XComposite wrapper;
	private XComposite noEditGUIComposite;
	
	/**
	 * @param parent
	 * @param style
	 * @param setLayoutData
	 */
	public ConfigPreferencesComposite(Composite parent, int style,
			boolean setLayoutData) {
		super(parent, style,
				LayoutMode.TIGHT_WRAPPER,
				setLayoutData ? LayoutDataMode.GRID_DATA : LayoutDataMode.NONE
			);
		createWrapper();
		createNoEditGUI();
		getStackLayout().topControl = noEditGUIComposite;
	}
	
	protected void createWrapper() {
		wrapper = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		wrapper.setLayout(new StackLayout());
	}
	
	protected void createNoEditGUI() {
		noEditGUIComposite = new XComposite(wrapper, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		Label label = new Label(noEditGUIComposite, SWT.WRAP);
		label.setLayoutData(new GridData());
		label.setText(Messages.getString("org.nightlabs.jfire.base.ui.config.ConfigPreferencesComposite.noPreferencePage")); //$NON-NLS-1$
	}
	
	public void clear() {
		wrapper.dispose();
		wrapper = null;
		createWrapper();
	}
	
	public Composite getWrapper() {
		return wrapper;
	}
	
	public StackLayout getStackLayout() {
		return (StackLayout)wrapper.getLayout();
	}
	
	public void setNoEditGUI() {
		getStackLayout().topControl = noEditGUIComposite;
		wrapper.layout();
	}
	
}
