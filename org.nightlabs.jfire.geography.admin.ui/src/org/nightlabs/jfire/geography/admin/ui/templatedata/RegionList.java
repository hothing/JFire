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
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.util.NLLocale;

public class RegionList extends XComposite implements ISelectionProvider
{
	private TimerText searchRegionNameText;
	private List<Region> regions = new ArrayList<Region>();
	private org.eclipse.swt.widgets.List regionList;

	private CountryID countryID;

	public RegionList(Composite parent) {
		this(parent, null);
	}

	public RegionList(Composite parent, CountryID countryID) {
		super(parent, SWT.NONE);

		searchRegionNameText = new TimerText(this, SWT.BORDER);
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

		regionList = new org.eclipse.swt.widgets.List(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = Display.getDefault().getBounds().height / 3;
		gd.widthHint = Display.getDefault().getBounds().width / 3;
		regionList.setLayoutData(gd);

		regionList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Object[] listeners = selectionChangedListeners.getListeners();
				if (listeners.length < 1)
					return;

				SelectionChangedEvent event = new SelectionChangedEvent(RegionList.this, getSelection());

				for (Object listener : listeners)
					((ISelectionChangedListener)listener).selectionChanged(event);
			}
		});

		setCountryID(countryID);
	}

	public void setCountryID(CountryID countryID) {
		this.countryID = countryID;

		if (countryID == null) {
			regionList.removeAll();
			regions.clear();
			return;
		}

		searchRegionsAsync();
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
		return new StructuredSelection(getSelectedRegions());
	}

	@Override
	public void setSelection(ISelection selection) {
		IStructuredSelection sel = (IStructuredSelection) selection;
		int[] indices = new int[sel.size()];
		int i = -1;
		for (Object o : sel.toArray())
			indices[++i] = regions.indexOf(o);

		regionList.select(indices);
	}

	public List<Region> getSelectedRegions()
	{
		int[] indices = regionList.getSelectionIndices();
		ArrayList<Region> l = new ArrayList<Region>(indices.length);
		for (int i : indices)
			l.add(regions.get(i));

		return l;
	}

	public Region getFirstSelectedRegion()
	{
		List<Region> l = getSelectedRegions();
		if (l.isEmpty())
			return null;
		else
			return l.get(0);
	}

	/**
	 * This method must be called on the GUI thread, because it reads the search
	 * criteria from the text fields. It spawns a worker thread and calls
	 * {@link #searchRegions(String)} on it.
	 */
	protected void searchRegionsAsync()
	{
		regionList.removeAll();
		regions = Collections.emptyList();
		regionList.add(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.RegionList.regionList.text")); //$NON-NLS-1$

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

		regions = Geography.sharedInstance().findRegionsByRegionNameSorted(countryID, searchRegionNameStr, NLLocale.getDefault(), Geography.FIND_MODE_CONTAINS);

		getDisplay().asyncExec(new Runnable() {
			public void run()
			{
				regionList.removeAll();
				for (Iterator<Region> it = regions.iterator(); it.hasNext(); ) {
					Region region = it.next();
					regionList.add(region.getName().getText(languageID));
				}
			}
		});
	}

}
