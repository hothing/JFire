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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

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
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.region.IRegionSelector;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.dao.RegionDAO;
import org.nightlabs.jfire.geography.id.RegionID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.NLLocale;

public class CitySelectorList extends XComposite implements
		ICitySelector
{
	private IWorkbenchPartSite site;
	private IRegionSelector regionSelector;

	private List<City> cities = new ArrayList<City>();
	/**
	 * @see City#getPrimaryKey()
	 */
	private Set<String> cityPKs = new HashSet<String>();
	private org.eclipse.swt.widgets.List cityList;

	private Region region = null;
	private City selectedCity = null;

	public CitySelectorList(IWorkbenchPartSite site, Composite parent, int style)
	{
		this(site, parent, style, null);
	}
	public CitySelectorList(IWorkbenchPartSite site, Composite parent, int style, IRegionSelector regionSelector)
	{
		super(parent, style, LayoutMode.TIGHT_WRAPPER);
		this.site = site;
		setRegionSelector(regionSelector);

		cityList = new org.eclipse.swt.widgets.List(this, getBorderStyle() | SWT.H_SCROLL | SWT.V_SCROLL);
		cityList.setLayoutData(new GridData(GridData.FILL_BOTH));
		cityList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				fireSelectionChangedEvent();
			}
		});

		hookContextMenu();

		Region region = regionSelector == null ? null : regionSelector.getSelectedRegion();
		setRegionID((RegionID) (region == null ? null : JDOHelper.getObjectId(region)));
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
		int idx = cityList.getSelectionIndex();
		if (idx < 0)
			selectedCity = null;
		else
			selectedCity = cities.get(idx);
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		for (Object o : listenerList.getListeners())
			((ISelectionChangedListener)o).selectionChanged(event);
	}

	public ISelection getSelection()
	{
		return new StructuredSelection(
				selectedCity == null ? Collections.emptyList() : Collections.singletonList(selectedCity));
	}

	/**
	 * This method is called when a country is selected and spawns
	 * a new thread on which it calls {@link #loadRegions()}.
	 */
	private void loadCitiesAsync(final RegionID regionID)
	{
		Job job = new Job(Messages.getString("org.nightlabs.jfire.geography.admin.ui.city.CitySelectorList.loadCities.monitor.task.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception
			{
				loadCities(regionID, monitor);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void loadCities(RegionID regionID, ProgressMonitor monitor)
	{
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.geography.admin.ui.city.CitySelectorList.loadCities.monitor.task.name"), 100); //$NON-NLS-1$
		try {
//			GeographyManager gm = GeographyManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			final Region regionNoCities = regionID == null ? null : RegionDAO.sharedInstance().getRegion(regionID, new String[] {FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new SubProgressMonitor(monitor, 45));
			final Region regionWithCities = regionID == null ? null : RegionDAO.sharedInstance().getRegion(regionID, FETCH_GROUPS_REGION_CITIES, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new SubProgressMonitor(monitor, 45));
			final List<City> l = new ArrayList<City>(regionID == null ? 0 : regionWithCities.getCities().size());
			final Set<String> cPKs = new HashSet<String>(regionID == null ? 0 : regionWithCities.getCities().size());

			if (regionID != null) {
				for (City city : regionWithCities.getCities()) {
					cPKs.add(city.getPrimaryKey());
					l.add(city);
				}
			}

			final String languageID = NLLocale.getDefault().getLanguage();
			Collections.sort(l, new Comparator<City>() {
				public int compare(City c0, City c1)
				{
					return c0.getName().getText(languageID).compareTo(c1.getName().getText(languageID));
				}
			});

			monitor.worked(10);

			Runnable setCityList = new Runnable() {
				public void run()
				{
					if (cityList.isDisposed())
						return;

					region = regionNoCities;
					cities = l;
					cityPKs = cPKs;
					cityList.removeAll();
					for (City city : cities) {
						cityList.add(city.getName().getText(languageID));
						if (lazySelection != null && lazySelection.equals(city)) {
							int index = cityList.indexOf(city.getName().getText(languageID));
							cityList.select(index);
							lazySelection = null;
						}
					}
					fireSelectionChangedEvent();
				}
			};

			if (Display.getDefault().getThread() == Thread.currentThread())
				setCityList.run();
			else
				Display.getDefault().asyncExec(setCityList);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.done();
		}
	}

	public IAction getAddCityAction() {
		addCityAction.setEnabled(getRegion() != null);
		return addCityAction;
	}

	private Action addCityAction = new Action() {
		@Override
		public String getText()
		{
			return Messages.getString("org.nightlabs.jfire.geography.admin.ui.city.CitySelectorList.addCityActionText"); //$NON-NLS-1$
		}

		@Override
		public void run()
		{
			AddCityWizard wizard = new AddCityWizard(CitySelectorList.this);
			AddCityWizardDialog dialog = new AddCityWizardDialog(getShell(), wizard, CitySelectorList.this);
			dialog.open();
		}
	};

	private void fillContextMenu(IMenuManager manager) {
		manager.add(getAddCityAction());

		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				CitySelectorList.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(this.cityList);
		this.cityList.setMenu(menu);
		if (site != null)
			site.registerContextMenu(menuMgr, this);
	}


	private ISelectionChangedListener regionChangedListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			IRegionSelector regionSelector = (IRegionSelector) event.getSource();
			RegionID regionID = null;
			Region region = regionSelector.getSelectedRegion();
			if (region != null)
				regionID = (RegionID)JDOHelper.getObjectId(region);

			setRegionID(regionID);
		}
	};

	public void setRegionSelector(IRegionSelector regionSelector)
	{
		if (this.regionSelector == regionSelector)
			return;

		if (this.regionSelector != null) {
			this.regionSelector.removeSelectionChangedListener(regionChangedListener);
			this.regionSelector = null;
		}

		this.regionSelector = regionSelector;
		if (regionSelector != null)
			regionSelector.addSelectionChangedListener(regionChangedListener);
	}

	public IRegionSelector getRegionSelector() {
		return regionSelector;
	}
	
	public void setRegionID(RegionID regionID)
	{
		loadCitiesAsync(regionID);
	}

	public Region getRegion()
	{
		return region;
	}

	public City getSelectedCity()
	{
		return selectedCity;
	}

	/**
	 * @see org.nightlabs.jfire.geography.admin.ui.city.ICitySelector#containsCity(org.nightlabs.jfire.geography.City)
	 */
	public boolean containsCity(City city)
	{
		return cityPKs.contains(city.getPrimaryKey());
	}
	public void addCity(City city)
	{
		cities.add(city);
		cityPKs.add(city.getPrimaryKey());
		cityList.add(city.getName().getText(NLLocale.getDefault().getLanguage()));
//		cityList.select(cities.size() - 1);
		cityList.setSelection(cities.size() - 1);
		fireSelectionChangedEvent();
	}

	private City lazySelection;
	public void setSelection(ISelection selection)
	{
		if (!selection.isEmpty()) {
			if (selection instanceof StructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				Object firstElement = structuredSelection.getFirstElement();
				if (firstElement instanceof City) {
					int index = cities.indexOf(firstElement);
					if (index != -1)
//						cityList.select(index);
						cityList.setSelection(index);
					else
						lazySelection = (City) firstElement;

					fireSelectionChangedEvent();
				}
			}
		}
//		throw new UnsupportedOperationException("NYI");
	}

	public List<City> getCities() {
		return cities;
	}
}
