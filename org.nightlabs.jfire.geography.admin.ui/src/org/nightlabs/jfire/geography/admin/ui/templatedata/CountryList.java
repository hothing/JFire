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
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.util.NLLocale;

public class CountryList
extends XComposite
implements ISelectionProvider
{
	private TimerText searchCountryNameText;

	private List<Country> countries = new ArrayList<Country>();
	private org.eclipse.swt.widgets.List countryList;

	public CountryList(Composite parent) {
		super(parent, SWT.NONE);

		searchCountryNameText = new TimerText(this, SWT.BORDER);
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
		countryList = new org.eclipse.swt.widgets.List(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = Display.getDefault().getBounds().height / 3;
		gd.widthHint = Display.getDefault().getBounds().width / 3;
		countryList.setLayoutData(gd);

		countryList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Object[] listeners = selectionChangedListeners.getListeners();
				if (listeners.length < 1)
					return;

				SelectionChangedEvent event = new SelectionChangedEvent(CountryList.this, getSelection());

				for (Object listener : listeners)
					((ISelectionChangedListener)listener).selectionChanged(event);
			}
		});

		searchCountriesAsync();
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
		return new StructuredSelection(getSelectedCountries());
	}

	public List<Country> getSelectedCountries()
	{
		int[] indices = countryList.getSelectionIndices();
		ArrayList<Country> l = new ArrayList<Country>(indices.length);
		for (int i : indices)
			l.add(countries.get(i));

		return l;
	}

	public Country getFirstSelectedCountry()
	{
		List<Country> l = getSelectedCountries();
		if (l.isEmpty())
			return null;
		else
			return l.get(0);
	}

	@Override
	public void setSelection(ISelection selection) {
		IStructuredSelection sel = (IStructuredSelection) selection;
		int[] indices = new int[sel.size()];
		int i = -1;
		for (Object o : sel.toArray())
			indices[++i] = countries.indexOf(o);

		countryList.select(indices);
	}

	/**
	 * This method must be called on the GUI thread, because it reads the search
	 * criteria from the text fields. It spawns a worker thread and calls
	 * {@link #searchCountries(String)} on it.
	 */
	protected void searchCountriesAsync()
	{
		final String searchCountryNameStr = searchCountryNameText.getText();
		countryList.removeAll();
		countries = Collections.emptyList();
		countryList.add(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.CountryList.countryList.text")); //$NON-NLS-1$
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

		countries = Geography.sharedInstance().findCountriesByCountryNameSorted(searchCountryNameStr, NLLocale.getDefault(), Geography.FIND_MODE_CONTAINS);

		getDisplay().asyncExec(new Runnable() {
			public void run()
			{
				if (countryList.isDisposed())
					return;

				countryList.removeAll();
				for (Iterator<Country> it = countries.iterator(); it.hasNext(); ) {
					Country country = it.next();
					countryList.add(country.getName().getText(languageID));
				}
			}
		});
	}
}
