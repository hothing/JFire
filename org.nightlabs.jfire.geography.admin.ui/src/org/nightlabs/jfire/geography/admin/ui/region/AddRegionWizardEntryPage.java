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

package org.nightlabs.jfire.geography.admin.ui.region;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jdo.JDOHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.composite.TimerText;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.base.ui.wizard.DynamicPathWizardPage;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.util.NLLocale;

public class AddRegionWizardEntryPage extends DynamicPathWizardPage
{
	private TimerText searchRegionNameText;
	private List<Region> regions = new ArrayList<Region>();
	private org.eclipse.swt.widgets.List regionList;

	private CountryID countryID;
	private Region selectedRegion;

	public AddRegionWizardEntryPage()
	{
		super(AddRegionWizardEntryPage.class.getName(), Messages.getString("org.nightlabs.jfire.geography.admin.ui.region.AddRegionWizardEntryPage.pageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("org.nightlabs.jfire.geography.admin.ui.region.AddRegionWizardEntryPage.pageDescription")); //$NON-NLS-1$
	}

	@Override
	public Control createPageContents(Composite parent)
	{
		XComposite page = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);

		AddRegionWizard wizard = (AddRegionWizard) getWizard();
		IRegionSelector regionSelector = wizard.getRegionSelector();

//		String languageID = NLLocale.getDefault().getLanguage();
		Country country = regionSelector.getCountry();
		if (country == null)
			throw new IllegalStateException("No country selected!"); //$NON-NLS-1$

		searchRegionNameText = new TimerText(page, SWT.BORDER);
		searchRegionNameText.addDelayedModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e)
			{
				Display.getDefault().asyncExec(new Runnable() {
					public void run()
					{
						searchRegionsAsync();
					}
				});
			}
		});

		countryID = (CountryID) JDOHelper.getObjectId(country);
//		regions = GeographySystem.sharedInstance().getRegionsSorted(countryID, NLLocale.getDefault());
		regionList = new org.eclipse.swt.widgets.List(page, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = Display.getDefault().getBounds().height / 3;
		gd.widthHint = Display.getDefault().getBounds().width / 3;
		regionList.setLayoutData(gd);

		regionList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				int idx = regionList.getSelectionIndex();
				if (idx < 0 || regions.isEmpty())
					selectedRegion = null;
				else
					selectedRegion = regions.get(idx);

				((DynamicPathWizard)getWizard()).updateDialog();
			}
		});
		regionList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e)
			{
				((DynamicPathWizard)getWizard()).finish();
			}
		});
		regionList.add(Messages.getString("org.nightlabs.jfire.geography.admin.ui.region.AddRegionWizardEntryPage.loadingDataListEntry")); //$NON-NLS-1$

		searchRegionsAsync();

		return page;
	}

	/**
	 * This method must be called on the GUI thread, because it reads the search
	 * criteria from the text fields. It spawns a worker thread and calls
	 * {@link #searchRegions(String)} on it.
	 */
	protected void searchRegionsAsync()
	{
		final String searchRegionNameStr = searchRegionNameText.getText();
		new Thread() {
			@Override
			public void run()
			{
				searchRegions(searchRegionNameStr);
			}
		}.start();
	}

	protected void searchRegions(String searchRegionNameStr)
	{
		final String languageID = NLLocale.getDefault().getLanguage();

		regions = new ArrayList<Region>(
			Geography.sharedInstance().findRegionsByRegionNameSorted(countryID, searchRegionNameStr, NLLocale.getDefault(), Geography.FIND_MODE_CONTAINS)
		);

		Display.getDefault().asyncExec(new Runnable() {
			public void run()
			{
				if (regionList.isDisposed())
					return;

				AddRegionWizard wizard = (AddRegionWizard) getWizard();

				selectedRegion = null;
				regionList.removeAll();
				for (Iterator<Region> it = regions.iterator(); it.hasNext(); ) {
					Region region = it.next();
					if (wizard.getRegionSelector().containsRegion(region))
						it.remove();
					else
						regionList.add(region.getName().getText(languageID));
				}

				wizard.updateDialog();
			}
		});
	}

	public Region getSelectedRegion()
	{
		return selectedRegion;
	}

	@Override
	public boolean isPageComplete()
	{
		return selectedRegion != null;
	}
}
