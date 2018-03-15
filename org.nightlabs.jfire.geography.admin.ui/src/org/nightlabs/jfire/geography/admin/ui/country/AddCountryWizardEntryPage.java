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

package org.nightlabs.jfire.geography.admin.ui.country;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.util.NLLocale;

public class AddCountryWizardEntryPage extends DynamicPathWizardPage
{
	private TimerText searchCountryNameText;

	private List<Country> countries = new ArrayList<Country>();
	private org.eclipse.swt.widgets.List countryList;

	private Country selectedCountry = null;

	public AddCountryWizardEntryPage()
	{
		super(AddCountryWizardEntryPage.class.getName(), Messages.getString("org.nightlabs.jfire.geography.admin.ui.country.AddCountryWizardEntryPage.pageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("org.nightlabs.jfire.geography.admin.ui.country.AddCountryWizardEntryPage.pageDescription")); //$NON-NLS-1$
	}

	@Override
	public Control createPageContents(Composite parent)
	{
		XComposite page = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);

//		AddCountryWizard wizard = (AddCountryWizard) getWizard();

//		String languageID = NLLocale.getDefault().getLanguage();

		searchCountryNameText = new TimerText(page, SWT.BORDER);
		searchCountryNameText.addDelayedModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e)
			{
				Display.getDefault().asyncExec(new Runnable() {
					public void run()
					{
						searchCountriesAsync();
					}
				});
			}
		});

//		countries = new ArrayList(GeographySystem.sharedInstance().getCountriesSorted(NLLocale.getDefault()));
		countryList = new org.eclipse.swt.widgets.List(page, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = Display.getDefault().getBounds().height / 3;
		gd.widthHint = Display.getDefault().getBounds().width / 3;
		countryList.setLayoutData(gd);

		countryList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				int idx = countryList.getSelectionIndex();
				if (idx < 0 || countries.isEmpty())
					selectedCountry = null;
				else
					selectedCountry = countries.get(idx);

				((DynamicPathWizard)getWizard()).updateDialog();
			}
		});
		countryList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e)
			{
				((DynamicPathWizard)getWizard()).finish();
			}
		});
		countryList.add(Messages.getString("org.nightlabs.jfire.geography.admin.ui.country.AddCountryWizardEntryPage.loadingDataListEntry")); //$NON-NLS-1$

		searchCountriesAsync();

		return page;
	}

	/**
	 * This method must be called on the GUI thread, because it reads the search
	 * criteria from the text fields. It spawns a worker thread and calls
	 * {@link #searchCountries(String)} on it.
	 */
	protected void searchCountriesAsync()
	{
		final String searchCountryNameStr = searchCountryNameText.getText();
		new Thread() {
			@Override
			public void run()
			{
				searchCountries(searchCountryNameStr);
			}
		}.start();
	}

	protected void searchCountries(String searchCountryNameStr)
	{
		final String languageID = NLLocale.getDefault().getLanguage();

		countries = new ArrayList<Country>(
			Geography.sharedInstance().findCountriesByCountryNameSorted(searchCountryNameStr, NLLocale.getDefault(), Geography.FIND_MODE_CONTAINS)
		);

		countryList.getDisplay().asyncExec(new Runnable() {
			public void run()
			{
				if (countryList.isDisposed())
					return;

				AddCountryWizard wizard = (AddCountryWizard) getWizard();

				selectedCountry = null;
				countryList.removeAll();
				for (Iterator<Country> it = countries.iterator(); it.hasNext(); ) {
					Country country = it.next();
					if (wizard.getCountrySelector().containsCountry(country))
						it.remove();
					else
						countryList.add(country.getName().getText(languageID));
				}

				wizard.updateDialog();
			}
		});
	}

	public Country getSelectedCountry()
	{
		return selectedCountry;
	}

	@Override
	public boolean isPageComplete()
	{
		return selectedCountry != null;
	}
}
