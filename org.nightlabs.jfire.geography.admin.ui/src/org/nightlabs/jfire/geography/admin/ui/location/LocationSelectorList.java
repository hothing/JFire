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

package org.nightlabs.jfire.geography.admin.ui.location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
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
import org.nightlabs.jfire.geography.Location;
import org.nightlabs.jfire.geography.admin.ui.city.ICitySelector;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.dao.CityDAO;
import org.nightlabs.jfire.geography.id.CityID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.NLLocale;

public class LocationSelectorList
extends XComposite
implements ILocationSelector
{
	private IWorkbenchPartSite site;
	private ICitySelector citySelector = null;

	private List<Location> locations = new ArrayList<Location>();
	private Set<String> locationPKs = new HashSet<String>();
	private org.eclipse.swt.widgets.List locationList;

	private City city = null;
	private Location selectedLocation = null;

	public LocationSelectorList(IWorkbenchPartSite site, Composite parent, int style)
	{
		this(site, parent, style, null);
	}

	public LocationSelectorList(IWorkbenchPartSite site, Composite parent, int style, ICitySelector citySelector)
	{
		super(parent, style, LayoutMode.TIGHT_WRAPPER);
		this.site = site;

		setCitySelector(citySelector);

		locationList = new org.eclipse.swt.widgets.List(this, getBorderStyle() | SWT.H_SCROLL | SWT.V_SCROLL);
		locationList.setLayoutData(new GridData(GridData.FILL_BOTH));
		locationList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				fireSelectionChangedEvent();
			}
		});

		hookContextMenu();

		City city = citySelector.getSelectedCity();
		setCityID((CityID) (city == null ? null : JDOHelper.getObjectId(city)));
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
		int idx = locationList.getSelectionIndex();
		if (idx < 0)
			selectedLocation = null;
		else
			selectedLocation = locations.get(idx);
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		for (Object o : listenerList.getListeners())
			((ISelectionChangedListener)o).selectionChanged(event);
	}

	public ISelection getSelection()
	{
		return new StructuredSelection(
				selectedLocation == null ? Collections.emptyList() : Collections.singletonList(selectedLocation));
	}

	public ICitySelector getCitySelector()
	{
		return citySelector;
	}

	private ISelectionChangedListener cityChangedListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			ICitySelector citySelector = (ICitySelector) event.getSource();
			CityID cityID = null;
			City city = citySelector.getSelectedCity();
			if (city != null)
				cityID = (CityID)JDOHelper.getObjectId(city);

			setCityID(cityID);
		}
	};

	public void setCitySelector(ICitySelector citySelector)
	{
		if (this.citySelector == citySelector)
			return;

		if (this.citySelector != null) {
			this.citySelector.removeSelectionChangedListener(cityChangedListener);
			this.citySelector = null;
		}

		this.citySelector = citySelector;

		if (citySelector != null)
			citySelector.addSelectionChangedListener(cityChangedListener);
	}

	public Action getAddLocationAction()
	{
		if (addLocationAction != null) {
			addLocationAction.setEnabled(getCity() != null );
		}
		return addLocationAction;
	}
	public void setAddLocationAction(Action addLocationAction)
	{
		this.addLocationAction = addLocationAction;
	}

	private Action addLocationAction = null;

//	private Action addRegionAction = new Action() {
//		public String getText()
//		{
//			return "Add Region...";
//		}
//
//		public void run()
//		{
//			AddVenueWizard wizard = new AddVenueWizard(LocationSelectorList.this);
//			DynamicPathWizardDialog dialog = new DynamicPathWizardDialog(wizard);
//			dialog.open();
//		}
//	};

	private void fillContextMenu(IMenuManager manager) {
		if (addLocationAction != null) {
			manager.add(getAddLocationAction());
		}

		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				LocationSelectorList.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(this.locationList);
		this.locationList.setMenu(menu);
		if (site != null)
			site.registerContextMenu(menuMgr, this);
	}

	/**
	 * This method is called when a city is selected and spawns
	 * a new thread on which it calls {@link #loadRegions()}.
	 */
	private void loadLocationsAsync(final CityID cityID)
	{
		Job job = new Job(Messages.getString("org.nightlabs.jfire.geography.admin.ui.location.LocationSelectorList.job.loadLocationsAsync")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception
			{
				loadLocations(cityID, monitor);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void loadLocations(CityID cityID, ProgressMonitor monitor)
	{
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.geography.admin.ui.location.LocationSelectorList.monitor.loadLocations"), 100); //$NON-NLS-1$
		try {
			final City cityNoLocations = cityID == null ? null : CityDAO.sharedInstance().getCity(cityID, new String[] {FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new SubProgressMonitor(monitor, 45));
			final City cityWithLocations = cityID == null ? null : CityDAO.sharedInstance().getCity(cityID, FETCH_GROUPS_CITY_LOCATIONS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new SubProgressMonitor(monitor, 45));
			final List<Location> l = new ArrayList<Location>(cityID == null ? 0 : cityWithLocations.getLocations().size());
			final Set<String> lPKs = new HashSet<String>(cityID == null ? 0 : cityWithLocations.getLocations().size());

			if (cityID != null) {
				for (Iterator<Location> it = cityWithLocations.getLocations().iterator(); it.hasNext(); ) {
					Location location = it.next();
					lPKs.add(location.getPrimaryKey());
					l.add(location);
				}
			}

			final String languageID = NLLocale.getDefault().getLanguage();
			Collections.sort(l, new Comparator<Location>() {
				public int compare(Location l0, Location l1)
				{
					return l0.getName().getText(languageID).compareTo(l1.getName().getText(languageID));
				}
			});

			monitor.worked(10);

			Runnable setLocationList = new Runnable() {
				public void run()
				{
					if (locationList.isDisposed())
						return;

					city = cityNoLocations;
					locations = l;
					locationPKs = lPKs;
					selectedLocation = null;
					locationList.removeAll();
					for (Iterator<Location> it = locations.iterator(); it.hasNext(); ) {
						Location location = it.next();
						locationList.add(location.getName().getText(languageID));
						if (lazySelection != null && lazySelection.equals(location)) {
							int index = locationList.indexOf(location.getName().getText(languageID));
							locationList.select(index);
							lazySelection = null;
						}
					}
					fireSelectionChangedEvent();
				}
			};

			if (Display.getDefault().getThread() == Thread.currentThread())
				setLocationList.run();
			else
				Display.getDefault().asyncExec(setLocationList);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.admin.ui.location.ILocationSelector#setCityID(org.nightlabs.jfire.geography.ui.id.CityID)
	 */
	public void setCityID(CityID cityID)
	{
		loadLocationsAsync(cityID);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.admin.ui.location.ILocationSelector#getCity()
	 */
	public City getCity()
	{
		return city;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.admin.ui.location.ILocationSelector#getLocations()
	 */
	public Collection<Location> getLocations()
	{
		return locations;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.admin.ui.location.ILocationSelector#getSelectedLocation()
	 */
	public Location getSelectedLocation()
	{
		return selectedLocation;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.admin.ui.location.ILocationSelector#containsLocation(org.nightlabs.jfire.geography.Location)
	 */
	public boolean containsLocation(Location location)
	{
		return locationPKs.contains(location.getPrimaryKey());
	}

	public void addLocation(Location location)
	{
		locations.add(location);
		locationPKs.add(location.getPrimaryKey());
		locationList.add(location.getName().getText(NLLocale.getDefault().getLanguage()));
//		locationList.select(locations.size() - 1);
		locationList.setSelection(locations.size() - 1);
		fireSelectionChangedEvent();
	}

	private Location lazySelection;
	public void setSelection(ISelection selection)
	{
		if (!selection.isEmpty()) {
			if (selection instanceof StructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				Object firstElement = structuredSelection.getFirstElement();
				if (firstElement instanceof Location) {
					int index = locations.indexOf(firstElement);
					if (index != -1)
//						locationList.select(index);
						locationList.setSelection(index);
					else
						lazySelection = (Location) firstElement;

					fireSelectionChangedEvent();
				}
			}
		}
//		throw new UnsupportedOperationException("NYI");
	}
}
