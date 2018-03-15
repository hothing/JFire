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
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.country.ICountrySelector;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.dao.CountryDAO;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.NLLocale;

public class RegionSelectorList
extends XComposite
implements IRegionSelector
{
	private IWorkbenchPartSite site;
	private ICountrySelector countrySelector = null;

	private List<Region> regions = new ArrayList<Region>();
	/**
	 * @see Region#getPrimaryKey()
	 */
	private Set<String> regionPKs = new HashSet<String>();
	private org.eclipse.swt.widgets.List regionList;

	private Country country = null;
	private Region selectedRegion = null;

	/**
	 * @param parent
	 * @param style
	 * @param setLayoutData
	 */
	public RegionSelectorList(IWorkbenchPartSite site, Composite parent, int style)
	{
		this(site, parent, style, null);
	}

	/**
	 * @param parent
	 * @param style
	 * @param setLayoutData
	 */
	public RegionSelectorList(IWorkbenchPartSite site, Composite parent, int style, ICountrySelector countrySelector)
	{
		super(parent, style, LayoutMode.TIGHT_WRAPPER);
		this.site = site;

		setCountrySelector(countrySelector);

		regionList = new org.eclipse.swt.widgets.List(this, getBorderStyle() | SWT.H_SCROLL | SWT.V_SCROLL);
		regionList.setLayoutData(new GridData(GridData.FILL_BOTH));
		regionList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				fireSelectionChangedEvent();
			}
		});

		hookContextMenu();

		Country country = countrySelector == null ? null : countrySelector.getSelectedCountry();
		setCountryID((CountryID) (country == null ? null : JDOHelper.getObjectId(country)));
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
		int idx = regionList.getSelectionIndex();
		if (idx < 0)
			selectedRegion = null;
		else
			selectedRegion = regions.get(idx);
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		for (Object o : listenerList.getListeners())
			((ISelectionChangedListener)o).selectionChanged(event);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection()
	{
		return new StructuredSelection(
				selectedRegion == null ? Collections.emptyList() : Collections.singletonList(selectedRegion));
	}

	public ICountrySelector getCountrySelector()
	{
		return countrySelector;
	}

	private ISelectionChangedListener countryChangedListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			ICountrySelector countrySelector = (ICountrySelector) event.getSource();
			CountryID countryID = null;
			Country country = countrySelector.getSelectedCountry();
			if (country != null)
				countryID = (CountryID)JDOHelper.getObjectId(country);

			setCountryID(countryID);
		}
	};

	public void setCountrySelector(ICountrySelector countrySelector)
	{
		if (this.countrySelector == countrySelector)
			return;

		if (this.countrySelector != null) {
			this.countrySelector.removeSelectionChangedListener(countryChangedListener);
			this.countrySelector = null;
		}

		this.countrySelector = countrySelector;

		if (countrySelector != null)
			countrySelector.addSelectionChangedListener(countryChangedListener);
	}

	public IAction getAddRegionAction() {
		addRegionAction.setEnabled(getCountry() != null);
		return addRegionAction;
	}

	private Action addRegionAction = new Action() {
		@Override
		public String getText()
		{
			return Messages.getString("org.nightlabs.jfire.geography.admin.ui.region.RegionSelectorList.addRegionActionText"); //$NON-NLS-1$
		}

		@Override
		public void run()
		{
			AddRegionWizard wizard = new AddRegionWizard(RegionSelectorList.this);
//			DynamicPathWizardDialog dialog = new DynamicPathWizardDialog(wizard);
			AddRegionWizardDialog dialog = new AddRegionWizardDialog(getShell(), wizard, RegionSelectorList.this);
			dialog.open();
		}
	};

	private void fillContextMenu(IMenuManager manager) {
		manager.add(getAddRegionAction());
		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				RegionSelectorList.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(this.regionList);
		this.regionList.setMenu(menu);
		if (site != null)
			site.registerContextMenu(menuMgr, this);
	}

	/**
	 * This method is called when a country is selected and spawns
	 * a new thread on which it calls {@link #loadRegions()}.
	 */
	private void loadRegionsAsync(final CountryID countryID)
	{
		Job job = new Job(Messages.getString("org.nightlabs.jfire.geography.admin.ui.region.RegionSelectorList.job.loadRegionsAsync")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception
			{
				loadRegions(countryID, monitor);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void loadRegions(CountryID countryID, ProgressMonitor monitor)
	{
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.geography.admin.ui.region.RegionSelectorList.mornitor.loadRegions"), 100); //$NON-NLS-1$
		try {
//			GeographyManager gm = GeographyManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			final Country countryNoRegions = countryID == null ? null : CountryDAO.sharedInstance().getCountry(countryID, new String[] { FetchPlan.DEFAULT }, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new SubProgressMonitor(monitor, 45));
			final Country countryWithRegions = countryID == null ? null : CountryDAO.sharedInstance().getCountry(countryID, FETCH_GROUPS_COUNTRY_REGIONS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new SubProgressMonitor(monitor, 45));
			final List<Region> l = new ArrayList<Region>(countryID == null ? 0 : countryWithRegions.getRegions().size());
			final Set<String> rPKs = new HashSet<String>(countryID == null ? 0 : countryWithRegions.getRegions().size());

			if (countryID != null) {
				for (Iterator<Region> it = countryWithRegions.getRegions().iterator(); it.hasNext(); ) {
					Region region = it.next();
					rPKs.add(region.getPrimaryKey());
					l.add(region);
				}
			}

			final String languageID = NLLocale.getDefault().getLanguage();
			Collections.sort(l, new Comparator<Region>() {
				public int compare(Region r0, Region r1)
				{
					return r0.getName().getText(languageID).compareTo(r1.getName().getText(languageID));
				}
			});

			monitor.worked(10);

			Runnable setRegionList = new Runnable() {
				public void run()
				{
					if (regionList.isDisposed())
						return;

					country = countryNoRegions;
					regions = l;
					regionPKs = rPKs;
					selectedRegion = null;
					regionList.removeAll();
					for (Iterator<Region> it = regions.iterator(); it.hasNext(); ) {
						Region region = it.next();
						regionList.add(region.getName().getText(languageID));
						if (lazySelection != null && lazySelection.equals(region)) {
							int index = regionList.indexOf(region.getName().getText(languageID));
							regionList.select(index);
							lazySelection = null;
						}
					}
					fireSelectionChangedEvent();
				}
			};

			if (Display.getDefault().getThread() == Thread.currentThread())
				setRegionList.run();
			else
				Display.getDefault().asyncExec(setRegionList);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.admin.ui.region.IRegionSelector#setCountryID(org.nightlabs.jfire.geography.ui.id.CountryID)
	 */
	public void setCountryID(CountryID countryID)
	{
		loadRegionsAsync(countryID);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.admin.ui.region.IRegionSelector#getCountry()
	 */
	public Country getCountry()
	{
		return country;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.admin.ui.region.IRegionSelector#getRegions()
	 */
	public Collection<Region> getRegions()
	{
		return regions;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.admin.ui.region.IRegionSelector#getSelectedRegion()
	 */
	public Region getSelectedRegion()
	{
		return selectedRegion;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.admin.ui.region.IRegionSelector#containsRegion(org.nightlabs.jfire.geography.Region)
	 */
	public boolean containsRegion(Region region)
	{
		return regionPKs.contains(region.getPrimaryKey());
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.admin.ui.region.IRegionSelector#addRegion(org.nightlabs.jfire.geography.Region)
	 */
	public void addRegion(Region region)
	{
		regions.add(region);
		regionPKs.add(region.getPrimaryKey());
		regionList.add(region.getName().getText(NLLocale.getDefault().getLanguage()));
		regionList.select(regions.size() - 1);
		fireSelectionChangedEvent();
	}

	private Region lazySelection = null;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection)
	{
		if (!selection.isEmpty()) {
			if (selection instanceof StructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				Object firstElement = structuredSelection.getFirstElement();
				if (firstElement instanceof Region) {
					int index = regions.indexOf(firstElement);
					if (index != -1)
						regionList.select(index);
					else
						lazySelection = (Region) firstElement;

					fireSelectionChangedEvent();
				}
			}
		}
//		throw new UnsupportedOperationException("NYI");
	}


}
