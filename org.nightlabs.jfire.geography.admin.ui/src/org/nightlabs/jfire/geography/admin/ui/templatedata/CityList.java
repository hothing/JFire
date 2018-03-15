package org.nightlabs.jfire.geography.admin.ui.templatedata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.composite.TimerText;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.id.RegionID;
import org.nightlabs.util.NLLocale;

public class CityList extends XComposite implements ISelectionProvider
{
	private TimerText searchCityNameText;
	private List<City> cities = new ArrayList<City>();
	private org.eclipse.swt.widgets.List cityList;

	private RegionID regionID;

	public CityList(Composite parent) {
		this(parent, null);
	}

	public CityList(Composite parent, RegionID regionID) {
		super(parent, SWT.NONE);

		searchCityNameText = new TimerText(this, SWT.BORDER);
		searchCityNameText.addDelayedModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e)
			{
				Display.getDefault().asyncExec(new Runnable() {
					public void run()
					{
						searchCitiesAsync();
					}
				});
			}
		});

		cityList = new org.eclipse.swt.widgets.List(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = Display.getDefault().getBounds().height / 3;
		gd.widthHint = Display.getDefault().getBounds().width / 3;
		cityList.setLayoutData(gd);

		cityList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Object[] listeners = selectionChangedListeners.getListeners();
				if (listeners.length < 1)
					return;

				SelectionChangedEvent event = new SelectionChangedEvent(CityList.this, getSelection());

				for (Object listener : listeners)
					((ISelectionChangedListener)listener).selectionChanged(event);
			}
		});

		setRegionID(regionID);
	}

	public void setRegionID(RegionID regionID) {
		this.regionID = regionID;

		if (regionID == null) {
			cityList.removeAll();
			cities.clear();
			return;
		}

		searchCitiesAsync();
	}

	private ListenerList selectionChangedListeners = new ListenerList();

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	@Override
	public ISelection getSelection() {
		return new StructuredSelection(getSelectedCities());
	}

	public List<City> getSelectedCities()
	{
		int[] indices = cityList.getSelectionIndices();
		ArrayList<City> l = new ArrayList<City>(indices.length);
		for (int i : indices)
			l.add(cities.get(i));

		return l;
	}

	@Override
	public void setSelection(ISelection selection) {
		IStructuredSelection sel = (IStructuredSelection) selection;
		int[] indices = new int[sel.size()];
		int i = -1;
		for (Object o : sel.toArray())
			indices[++i] = cities.indexOf(o);

		cityList.select(indices);
	}

	public City getFirstSelectedCity()
	{
		List<City> l = getSelectedCities();
		if (l.isEmpty())
			return null;
		else
			return l.get(0);
	}

	/**
	 * This method must be called on the GUI thread, because it reads the search
	 * criteria from the text fields. It spawns a worker thread and calls
	 * {@link #searchCities(String)} on it.
	 */
	protected void searchCitiesAsync()
	{
		cityList.removeAll();
		cities = Collections.emptyList();
		cityList.add(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.searchCitiesAsync")); //$NON-NLS-1$

		final String searchCityNameStr = searchCityNameText.getText();
		new Thread() {
			@Override
			public void run()
			{
				searchCities(searchCityNameStr);
			}
		}.start();
	}

	protected void searchCities(String searchCityNameStr)
	{
		final String languageID = NLLocale.getDefault().getLanguage();

		cities = Geography.sharedInstance().findCitiesByCityNameSorted(regionID, searchCityNameStr, NLLocale.getDefault(), Geography.FIND_MODE_CONTAINS);

		getDisplay().asyncExec(new Runnable() {
			public void run()
			{
				cityList.removeAll();
				for (Iterator<City> it = cities.iterator(); it.hasNext(); ) {
					City city = it.next();
					cityList.add(city.getName().getText(languageID));
				}
			}
		});
	}

}
