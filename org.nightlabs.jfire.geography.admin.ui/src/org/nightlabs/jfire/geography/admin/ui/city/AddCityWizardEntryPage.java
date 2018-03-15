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

package org.nightlabs.jfire.geography.admin.ui.city;

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
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.TimerText;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.base.ui.wizard.DynamicPathWizardPage;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.id.RegionID;
import org.nightlabs.util.NLLocale;

public class AddCityWizardEntryPage extends DynamicPathWizardPage
{
	private TimerText searchCityNameText;
	private TimerText searchZipText;
//	private Button searchButton;
	private List<City> cities = new ArrayList<City>();
	private org.eclipse.swt.widgets.List cityList;
	private RegionID regionID;

	private City selectedCity = null;

	public AddCityWizardEntryPage()
	{
		super(AddCityWizardEntryPage.class.getName(), Messages.getString("org.nightlabs.jfire.geography.admin.ui.city.AddCityWizardEntryPage.pageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("org.nightlabs.jfire.geography.admin.ui.city.AddCityWizardEntryPage.pageDescription")); //$NON-NLS-1$
	}

//	public static final long SEARCH_DELAY_MSEC = 500;
//
//	protected static class AutoSearchThread extends Thread
//	{
//		private AddCityWizardEntryPage addCityWizardEntryPage;
//
//		public AutoSearchThread(AddCityWizardEntryPage addCityWizardEntryPage)
//		{
//			this.addCityWizardEntryPage = addCityWizardEntryPage;
//			this.start();
//		}
//
//		private long lastKeyStrokeDT = System.currentTimeMillis();
//		public long getLastKeyStrokeDT()
//		{
//			return lastKeyStrokeDT;
//		}
//		public void setLastKeyStrokeDT()
//		{
//			this.lastKeyStrokeDT = System.currentTimeMillis();
//		}
//
//		public void run()
//		{
//			while (true) {
//				try {
//					sleep(Math.min(100L, SEARCH_DELAY_MSEC));
//				} catch (InterruptedException e) {
//					// ignore
//				}
//
//				long now = System.currentTimeMillis();
//				if (now - lastKeyStrokeDT > SEARCH_DELAY_MSEC) {
//					synchronized (addCityWizardEntryPage.autoSearchThreadMutex) {
//						if (now - lastKeyStrokeDT > SEARCH_DELAY_MSEC) { // in case a key was hit before entering the synchronized block
//							Display.getDefault().asyncExec(new Runnable() {
//								public void run()
//								{
//									addCityWizardEntryPage.searchCitiesAsync();
//								}
//							});
//							addCityWizardEntryPage.autoSearchThread = null;
//							return;
//						}
//					}
//				}
//			} // while (true) {
//		}
//	}

//	private Object autoSearchThreadMutex = new Object();
//	private AutoSearchThread autoSearchThread = null;

//	/**
//	 * This method must be called whenever the search criterias change (at each keystroke).
//	 * It spawns a new {@link AutoSearchThread} (if not yet existing) and updates its
//	 * lastKeyStrokeDT if it already exists (and therefore delays the search).
//	 */
//	protected void autoSearchDelayed()
//	{
//		synchronized (autoSearchThreadMutex) {
//			if (autoSearchThread == null)
//				autoSearchThread = new AutoSearchThread(this);
//			else
//				autoSearchThread.setLastKeyStrokeDT();
//		}
//	}

	@Override
	public Control createPageContents(Composite parent)
	{
		XComposite page = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);

		XComposite searchC = new XComposite(page, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		searchC.getGridLayout().numColumns = 2;
		
		Label cityNameLabel = new Label(searchC, SWT.NONE);
		cityNameLabel.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.city.AddCityWizardEntryPage.label.city")); //$NON-NLS-1$
		
		Label zipLabel = new Label(searchC, SWT.NONE);
		zipLabel.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.city.AddCityWizardEntryPage.label.zip")); //$NON-NLS-1$

		searchCityNameText = new TimerText(searchC, SWT.BORDER);
//		searchCityNameText.setLayoutData(new GridData(GridData.FILL_BOTH));
		searchCityNameText.addDelayedModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e)
			{
				Display.getDefault().asyncExec(new Runnable() {
					public void run()
					{
						if (!"".equals(searchCityNameText.getText())) //$NON-NLS-1$
							searchZipText.setText(""); //$NON-NLS-1$

						searchCitiesAsync();
					}
				});
			}
		});

		searchZipText = new TimerText(searchC, SWT.BORDER, searchCityNameText);
		searchZipText.addDelayedModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e)
			{
				Display.getDefault().asyncExec(new Runnable() {
					public void run()
					{
						if (!"".equals(searchZipText.getText())) //$NON-NLS-1$
							searchCityNameText.setText(""); //$NON-NLS-1$

						searchCitiesAsync();
					}
				});
			}
		});

//		searchCityNameText.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e)
//			{
//				autoSearchDelayed();
//			}
//		});
//		searchButton = new Button(searchC, SWT.PUSH);
//		searchButton.setText("Search!");
//		searchButton.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e)
//			{mplateDistrictData(District district){
//				searchCities();
//			}
//		});

		AddCityWizard wizard = (AddCityWizard) getWizard();
		ICitySelector citySelector = wizard.getCitySelector();

	//	String languageID = NLLocale.getDefault().getLanguage();
		Region region = citySelector.getRegion();
		if (region == null)
			throw new IllegalStateException("No region selected!"); //$NON-NLS-1$

		regionID = (RegionID) JDOHelper.getObjectId(region);
//		cities = new ArrayList(GeographySystem.sharedInstance().getCitiesSorted(regionID, languageID));
		cityList = new org.eclipse.swt.widgets.List(page, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = Display.getDefault().getBounds().height / 3;
		gd.widthHint = Display.getDefault().getBounds().width / 3;
		cityList.setLayoutData(gd);

		cityList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				int idx = cityList.getSelectionIndex();
				if (idx < 0 || cities.isEmpty())
					selectedCity = null;
				else
					selectedCity = cities.get(idx);

				((DynamicPathWizard)getWizard()).updateDialog();
			}
		});
		cityList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e)
			{
				((DynamicPathWizard)getWizard()).finish();
			}
		});
		cityList.add(Messages.getString("org.nightlabs.jfire.geography.admin.ui.city.AddCityWizardEntryPage.loadingDataListEntry")); //$NON-NLS-1$

		searchCitiesAsync();

		return page;
	}

	/**
	 * This method must be called on the GUI thread, because it reads the search
	 * criteria from the text fields. It spawns a worker thread and calls
	 * {@link #searchCities(String)} on it.
	 */
	protected void searchCitiesAsync()
	{
		final String searchCityNameStr = searchCityNameText.getText();
		final String searchZipStr = searchZipText.getText();
		new Thread() {
			@Override
			public void run()
			{
				searchCities(searchCityNameStr, searchZipStr);
			}
		}.start();
	}

	protected void searchCities(String searchCityNameStr, String searchZipStr)
	{
		final String languageID = NLLocale.getDefault().getLanguage();

		Geography geo = Geography.sharedInstance();
		if ("".equals(searchZipStr)) //$NON-NLS-1$
			cities = new ArrayList<City>(
				geo.findCitiesByCityNameSorted(regionID, searchCityNameStr, NLLocale.getDefault(), Geography.FIND_MODE_CONTAINS)
			);
		else
			cities = new ArrayList<City>(
					geo.findCitiesByZipSorted(regionID, searchZipStr, NLLocale.getDefault(), Geography.FIND_MODE_BEGINS_WITH)
			);

		Display.getDefault().asyncExec(new Runnable() {
			public void run()
			{
				AddCityWizard wizard = (AddCityWizard) getWizard();

				selectedCity = null;
				cityList.removeAll();
				for (Iterator<City> it = cities.iterator(); it.hasNext(); ) {
					City city = it.next();
					if (wizard.getCitySelector().containsCity(city))
						it.remove();
					else
						cityList.add(city.getName().getText(languageID));
				}

				wizard.updateDialog();
			}
		});
	}

	public City getSelectedCity()
	{
		return selectedCity;
	}

	@Override
	public boolean isPageComplete()
	{
		return selectedCity != null;
	}
}
