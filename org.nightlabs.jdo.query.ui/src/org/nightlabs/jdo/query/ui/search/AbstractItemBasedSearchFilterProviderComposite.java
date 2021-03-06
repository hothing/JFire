/* *****************************************************************************
 * org.nightlabs.jdo.query.ui - NightLabs Eclipse utilities for JDO                     *
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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jdo.query.ui.search;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.jdo.query.ui.resource.Messages;
import org.nightlabs.jdo.search.SearchFilter;

/**
 * A Composite for manipulating a list of SearchFieldItems ({@link org.nightlabs.jdo.query.ui.search.SearchFilterItemList})
 * and choosing their conjunction. <br/>
 * This Composite will accept a {@link org.nightlabs.jdo.query.ui.search.SearchResultFetcher} to
 * actually perform a search.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractItemBasedSearchFilterProviderComposite extends Composite implements SelectionListener{
	
	private Composite controlsComposite;
	private Button radioMatchAll;
	private Button radioMatchAny;
	private Composite buttonsComposite;
	private SearchFilterItemList itemList;
	private Button buttonMore;
	private Button buttonSearch;
	
	private SearchFilterItemListMutator listMutator;
	private SearchResultFetcher resultFetcher;
	private SearchFilterProvider searchFilterProvider;

	/**
	 * Creates a new ItemBasedSearchFilterProviderComposite,
	 * wich has a list of SearchFilterItems and provides
	 * selection of the conjunction of these items.
	 * This Composites delegates to its {@link SearchFilterItemList}
	 * for a request of a list of SearchFilterItems.
	 * 
	 * @param parent The widgets parent.
	 * @param style	The widgets style.
	 * @param searchFilterProvider The SearchFilterProvider that is the owner of this Composite.
	 * @param listMutator A mutator for the SearchFilterItemList.
	 * @param resultFetcher A fetcher to actually perform the search.
	 * @param login parameter to the fetchers searchTriggered method.
	 */
	public AbstractItemBasedSearchFilterProviderComposite(
		Composite parent,
		int style,
		SearchFilterProvider searchFilterProvider,
		SearchFilterItemListMutator listMutator,
		SearchResultFetcher resultFetcher
	) {
		super(parent, style);
		this.listMutator = listMutator;
		this.resultFetcher = resultFetcher;
		this.searchFilterProvider = searchFilterProvider;
		
		this.setLayout(new GridLayout());
		
		this.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		controlsComposite = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		((GridLayout)controlsComposite.getLayout()).numColumns = 3;
		((GridLayout)controlsComposite.getLayout()).horizontalSpacing = 10;
		((GridLayout)controlsComposite.getLayout()).makeColumnsEqualWidth = false;
		
		GridData controlsCompositeLData = new GridData();
		controlsCompositeLData.horizontalAlignment = GridData.FILL;
		controlsCompositeLData.grabExcessHorizontalSpace = true;
		controlsComposite.setLayoutData(controlsCompositeLData);

		
		
		radioMatchAll = new Button(controlsComposite, SWT.RADIO | SWT.LEFT);
		radioMatchAll.setText(Messages.getString("search.AbstractItemBasedSearchFilterProviderComposite.radioMatchAll.text")); //$NON-NLS-1$
		GridData radioMatchAllLData = new GridData();
		radioMatchAllLData.grabExcessHorizontalSpace = false;
		radioMatchAllLData.horizontalAlignment = GridData.FILL;
		radioMatchAll.setLayoutData(radioMatchAllLData);

		radioMatchAny = new Button(controlsComposite, SWT.RADIO | SWT.LEFT);
		radioMatchAny.setText(Messages.getString("search.AbstractItemBasedSearchFilterProviderComposite.radioMatchAny.text")); //$NON-NLS-1$
		GridData radioMatchAnyLData = new GridData();
		radioMatchAnyLData.grabExcessHorizontalSpace = false;
		radioMatchAnyLData.horizontalAlignment = GridData.FILL;
		radioMatchAny.setLayoutData(radioMatchAnyLData);
		radioMatchAny.setSelection(true);

		buttonsComposite = new Composite(controlsComposite, SWT.NONE);
		GridLayout buttonsCompositeLayout = new GridLayout();
		buttonsCompositeLayout.numColumns = 2;
		GridData buttonsCompositeLData = new GridData();
		buttonsCompositeLData.grabExcessHorizontalSpace = true;
		buttonsCompositeLData.horizontalAlignment = GridData.FILL;
		buttonsComposite.setLayoutData(buttonsCompositeLData);
		buttonsCompositeLayout.makeColumnsEqualWidth = true;
		buttonsComposite.setLayout(buttonsCompositeLayout);

		buttonMore = new Button(buttonsComposite, SWT.PUSH | SWT.CENTER);
		buttonMore.setText(Messages.getString("search.AbstractItemBasedSearchFilterProviderComposite.buttonMore.text")); //$NON-NLS-1$
		GridData buttonMoreLData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		buttonMoreLData.grabExcessHorizontalSpace = true;
		buttonMore.setLayoutData(buttonMoreLData);
		buttonMore.addSelectionListener(this);

		buttonSearch = new Button(buttonsComposite, SWT.PUSH | SWT.CENTER);
		buttonSearch.setText(Messages.getString("search.AbstractItemBasedSearchFilterProviderComposite.buttonSearch.text")); //$NON-NLS-1$
		GridData buttonSearchLData = new GridData(GridData.HORIZONTAL_ALIGN_END);
//		buttonSearchLData.widthHint = 58;
//		buttonSearchLData.heightHint = 32;
		buttonSearch.setLayoutData(buttonSearchLData);
		buttonSearch.addSelectionListener(this);

		// item list
		itemList = createSearchFilterItemList(this, SWT.NONE);
		GridData itemListCompositeLData = new GridData();
		itemListCompositeLData.horizontalAlignment = GridData.FILL;
		itemListCompositeLData.verticalAlignment = GridData.FILL;
		itemListCompositeLData.grabExcessVerticalSpace = true;
		itemListCompositeLData.grabExcessHorizontalSpace = true;
		itemList.setLayoutData(itemListCompositeLData);
		
		createDefaultItems();
		
		this.layout();
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent evt) {
		if (evt.getSource().equals(buttonMore)) {
			if (listMutator != null)
				listMutator.addItemEditor(itemList);
		}
		if (evt.getSource().equals(buttonSearch)) {
			if (listMutator != null)
				resultFetcher.searchTriggered(searchFilterProvider);
		}
	}
	
	public void clearItemList() {
		itemList.clear();
	}
	
	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent arg0) {
	}

	public abstract SearchFilterItemList createSearchFilterItemList(Composite parent, int style);
	
	public SearchFilterItemList getItemList() {
		return itemList;
	}

	protected void createDefaultItems() {
		listMutator.addItemEditor(itemList);
	}
	
	public int getConjuction() {
		if (radioMatchAll.getSelection())
			return SearchFilter.CONJUNCTION_AND;
		else if (radioMatchAny.getSelection())
			return SearchFilter.CONJUNCTION_OR;
		else
			return SearchFilter.CONJUNCTION_DEFAULT;
	}
}
