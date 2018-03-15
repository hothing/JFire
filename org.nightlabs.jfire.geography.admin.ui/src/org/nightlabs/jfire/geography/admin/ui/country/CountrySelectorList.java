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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.wizard.DynamicPathWizardDialog;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.dao.CountryDAO;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.NLLocale;

public class CountrySelectorList
extends XComposite
implements ICountrySelector
{
	private IWorkbenchPartSite site;

	/**
	 * Contains instances of <tt>String</tt> representing <tt>Country.countryID</tt>.
	 */
	private Set<String> countryIDs = new HashSet<String>();
	private List<Country> countries = new ArrayList<Country>();
	private org.eclipse.swt.widgets.List countryList;

	private Country selectedCountry = null;

	public CountrySelectorList(IWorkbenchPartSite site, Composite parent, int style)
	{
		super(parent, style, LayoutMode.TIGHT_WRAPPER);
		this.site = site;

		countryList = new org.eclipse.swt.widgets.List(this, getBorderStyle() | SWT.H_SCROLL | SWT.V_SCROLL);
		countryList.setLayoutData(new GridData(GridData.FILL_BOTH));
		countryList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				fireSelectionChangedEvent();
			}
		});

		hookContextMenu();

		loadCountriesAsync();
	}

	/**
	 * The listener list.
	 */
	private ListenerList listenerList = new ListenerList();

	/**
	 * Add a listener to the listener list.
	 * @param listener The listener to add
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener)
	{
		listenerList.add(listener);
	}

	/**
	 * Remove a listener from the listener list.
	 * @param listener The listener to remove
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener)
	{
		listenerList.remove(listener);
	}

	/**
	 * Fire a selection changed event.
	 */
	protected void fireSelectionChangedEvent()
	{
		int idx = countryList.getSelectionIndex();
		if (idx < 0)
			selectedCountry = null;
		else
			selectedCountry = countries.get(idx);
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		for (Object o : listenerList.getListeners())
			((ISelectionChangedListener)o).selectionChanged(event);
	}

	public ISelection getSelection()
	{
		return new StructuredSelection(
				selectedCountry == null ? Collections.emptyList() : Collections.singletonList(selectedCountry));
	}

	public IAction getAddCountryAction() {
		return addCountryAction;
	}

	private Action addCountryAction = new Action() {
		@Override
		public String getText()
		{
			return Messages.getString("org.nightlabs.jfire.geography.admin.ui.country.CountrySelectorList.addCountryActionText"); //$NON-NLS-1$
		}

		@Override
		public void run()
		{
			AddCountryWizard wizard = new AddCountryWizard(CountrySelectorList.this);
			DynamicPathWizardDialog dialog = new DynamicPathWizardDialog(wizard);
			dialog.open();
		}
	};

	private void fillContextMenu(IMenuManager manager) {
		manager.add(addCountryAction);
		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				CountrySelectorList.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(this.countryList);
		this.countryList.setMenu(menu);
		if (site != null)
			site.registerContextMenu(menuMgr, this);
	}

	/**
	 * This method is called by {@link #CountrySelectorList(Composite, int)} and spawns
	 * a new thread on which it calls {@link #loadCountries()}.
	 */
	private void loadCountriesAsync()
	{
		Job job = new Job(Messages.getString("org.nightlabs.jfire.geography.admin.ui.CountrySelectorList.job.countryloadCountriesAsync")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception
			{
				loadCountries(monitor);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void loadCountries(ProgressMonitor monitor)
	{
		try {
//			GeographyManager gm = GeographyManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();

			Collection<Country> countryCollection = CountryDAO.sharedInstance().getCountries(FETCH_GROUPS_COUNTRY, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
			final List<Country> c = new ArrayList<Country>(countryCollection.size());
			final Set<String> cIDs = new HashSet<String>(countryCollection.size());

			for (Iterator<Country> it = countryCollection.iterator(); it.hasNext(); ) {
				Country country = it.next();
				c.add(country);
				cIDs.add(country.getCountryID());
			}

			final String languageID = NLLocale.getDefault().getLanguage();
			Collections.sort(c, new Comparator<Country>() {
				public int compare(Country c0, Country c1)
				{
					return c0.getName().getText(languageID).compareTo(c1.getName().getText(languageID));
				}
			});

			Runnable setCountryList = new Runnable() {
				public void run()
				{
					if (countryList.isDisposed())
						return;

					countries = c;
					countryIDs = cIDs;
					countryList.removeAll();
					for (Iterator<Country> it = countries.iterator(); it.hasNext(); ) {
						Country country = it.next();
						countryList.add(country.getName().getText(languageID));
						if (lazySelection != null && country.equals(lazySelection)) {
							int index = countryList.indexOf(country.getName().getText(languageID));
							countryList.select(index);
							lazySelection = null;
						}
					}
					fireSelectionChangedEvent();
				}
			};

			if (Display.getDefault().getThread() == Thread.currentThread())
				setCountryList.run();
			else
				Display.getDefault().asyncExec(setCountryList);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.nightlabs.jfire.geography.admin.ui.country.ICountrySelector#getSelectedCountry()
	 */
	public Country getSelectedCountry()
	{
		return selectedCountry;
	}

//	private List<ISelectionChangedListener> selectionListeners = null;
//
//	public void addSelectionChangedListener(ISelectionChangedListener listener)
//	{
//		if (selectionListeners == null)
//			selectionListeners = new LinkedList<ISelectionChangedListener>();
//
//		selectionListeners.add(listener);
//	}
//
//	public ISelection getSelection()
//	{
//		List<Country> l = new ArrayList<Country>();
//
//		Country country = getSelectedCountry();
//		if (country != null)
//			l.add(country);
//
//		return new StructuredSelection(l);
//	}
//
//	public void removeSelectionChangedListener(ISelectionChangedListener listener)
//	{
//		if (selectionListeners == null)
//			return;
//
//		selectionListeners.remove(listener);
//	}

	private Country lazySelection;

	public void setSelection(ISelection selection)
	{
		if (!selection.isEmpty()) {
			if (selection instanceof StructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				Object firstElement = structuredSelection.getFirstElement();
				if (firstElement instanceof Country) {
					int index = countries.indexOf(firstElement);
					if (index != -1)
						countryList.select(index);
					else
						lazySelection = (Country) firstElement;

					fireSelectionChangedEvent();
				}
			}
		}
//		throw new UnsupportedOperationException("NYI");
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.admin.ui.country.ICountrySelector#getCountries()
	 */
	public List<Country> getCountries()
	{
		return countries;
	}

	public void addCountry(Country country)
	{
		countries.add(country);
		countryIDs.add(country.getCountryID());
		countryList.add(country.getName().getText(NLLocale.getDefault().getLanguage()));
		countryList.select(countries.size() - 1);
		fireSelectionChangedEvent();
	}

	public boolean containsCountry(Country country)
	{
		return countryIDs.contains(country.getCountryID());
	}
}
