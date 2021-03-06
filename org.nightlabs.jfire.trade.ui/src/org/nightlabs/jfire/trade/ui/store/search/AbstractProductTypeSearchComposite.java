package org.nightlabs.jfire.trade.ui.store.search;

import java.util.Collection;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.progress.ProgressMonitorWrapper;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.DefaultQueryProvider;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeGroup;
import org.nightlabs.jfire.store.ProductTypeLocal;
import org.nightlabs.jfire.store.dao.ProductTypeDAO;
import org.nightlabs.jfire.store.deliver.DeliveryConfiguration;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.search.AbstractProductTypeQuery;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.ui.TradePlugin;
import org.nightlabs.jfire.trade.ui.resource.Messages;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class AbstractProductTypeSearchComposite
	extends XComposite
{
	public static final String[] PRODUCT_TYPE_SEARCH_FETCH_GROUPS = new String[] {
		FetchPlan.DEFAULT,
		ProductType.FETCH_GROUP_THIS_PRODUCT_TYPE,
		ProductType.FETCH_GROUP_NAME,
		DeliveryConfiguration.FETCH_GROUP_NAME,
		LocalAccountantDelegate.FETCH_GROUP_NAME,
		ProductTypeGroup.FETCH_GROUP_NAME,
		PriceConfig.FETCH_GROUP_NAME,
		LegalEntity.FETCH_GROUP_PERSON,
		// TODO remove the following fetch-group, because most implementations of ProductType don't support nesting
		ProductTypeLocal.FETCH_GROUP_NESTED_PRODUCT_TYPE_LOCALS
	};

	private AbstractTableComposite<? extends ProductType> productTypeTableComposite;
	private Text searchText;
//	private ProductTypeSearchCriteriaComposite searchCriteriaComposite;
	private Composite searchCriteriaComposite;

	/**
	 * @param parent
	 * @param style
	 */
	public AbstractProductTypeSearchComposite(Composite parent, int style) {
		super(parent, style);
		queryProvider = new DefaultQueryProvider<AbstractSearchQuery>(getResultClass());
		AbstractProductTypeQuery productTypeQuery = queryProvider.getQueryOfType(getQueryClass());
		// preset the saleable flag.
		productTypeQuery.setSaleable(true);
		productTypeQuery.setFieldEnabled(AbstractProductTypeQuery.FieldName.saleable, true);

		createComposite(this);
	}

	public AbstractTableComposite<? extends ProductType> getProductTypeTableComposite() {
		return productTypeTableComposite;
	}

//	public ProductTypeSearchCriteriaComposite getSearchCriteriaComposite() {
//		return searchCriteriaComposite;
//	}

	protected void createComposite(Composite parent)
	{
		Composite criteriaComp = createCriteriaComposite(parent);
		ExpandableComposite expandableComposite = createExpandableComposite(parent);
		initProductTypeTable(parent);
	}

	protected void initProductTypeTable(Composite parent) {
		productTypeTableComposite = createProductTypeTable(parent);
		productTypeTableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		productTypeTableComposite.addSelectionChangedListener(productTypeSelectionListener);
	}

	protected Composite createCriteriaComposite(Composite parent) {
		Composite criteriaComp = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		criteriaComp.setLayout(new GridLayout(2, false));
		criteriaComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label searchLabel = new Label(criteriaComp, SWT.NONE);
		searchLabel.setText(Messages.getString("org.nightlabs.jfire.trade.ui.store.search.AbstractProductTypeSearchComposite.searchLabel.text")); //$NON-NLS-1$
		createSearchText(criteriaComp);
		return criteriaComp;
	}

	protected Text createSearchText(Composite parent) {
		searchText = new Text(parent, SWT.BORDER | SWT.SINGLE);
		searchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		searchText.addSelectionListener(searchTextListener); // this listener doesn't seem to work
		searchText.addTraverseListener(new TraverseListener()
		{
			@Override
			public void keyTraversed(TraverseEvent e)
			{
				if (e.keyCode == SWT.CR)
					searchPressed();
			}
		});
		return searchText;
	}

	protected ExpandableComposite createExpandableComposite(Composite parent) {
		ExpandableComposite expandableComposite = new ExpandableComposite(parent, ExpandableComposite.TWISTIE);
		expandableComposite.setText(Messages.getString("org.nightlabs.jfire.trade.ui.store.search.AbstractProductTypeSearchComposite.expandableComposite.text")); //$NON-NLS-1$
		expandableComposite.setLayout(new GridLayout());
//		GridData gridData = new GridData(GridData.FILL_BOTH);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		expandableComposite.setLayoutData(gridData);
		searchCriteriaComposite = createSearchCriteriaComposite(expandableComposite);
		searchCriteriaComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		expandableComposite.setClient(searchCriteriaComposite);
		expandableComposite.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				layout(true, true);
			}
		});
		return expandableComposite;
	}

	private SelectionListener searchTextListener = new SelectionAdapter()
	{
		@Override
		public void widgetDefaultSelected(SelectionEvent e)
		{
			searchPressed();
		}
	};

	private ISelectionChangedListener productTypeSelectionListener = new ISelectionChangedListener()
	{
		public void selectionChanged(SelectionChangedEvent event)
		{
			StructuredSelection sel = (StructuredSelection) event.getSelection();
			selectedProductType = (ProductType) sel.getFirstElement();
		}
	};

	protected String[] getFetchGroups() {
		return PRODUCT_TYPE_SEARCH_FETCH_GROUPS;
	}

	protected abstract Class<? extends ProductType> getResultClass();
	protected abstract Class<? extends AbstractProductTypeQuery> getQueryClass();
	protected abstract AbstractProductTypeQuery createNewQuery();

	public void searchPressed()
	{
		final String searchStr = searchText.getText();
		Job searchJob = new Job(Messages.getString("org.nightlabs.jfire.trade.ui.store.search.AbstractProductTypeSearchComposite.searchJob.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Display.getDefault().syncExec(new Runnable() {
					public void run()
					{
						if (productTypeTableComposite != null && !productTypeTableComposite.isDisposed())
							productTypeTableComposite.setInput(new String[] {Messages.getString("org.nightlabs.jfire.trade.ui.store.search.AbstractProductTypeSearchComposite.productTypeTableComposite.input_searching")}); //$NON-NLS-1$
					}
				});

				if (queryProvider == null)
					throw new RuntimeException("queryProvider must NOT be null at this point of time!"); //$NON-NLS-1$

				AbstractProductTypeQuery productTypeQuery = queryProvider.getQueryOfType(getQueryClass());
//				productTypeQuery.setSaleable(true);
//				productTypeQuery.setFieldEnabled(AbstractProductTypeQuery.FieldName.saleable, true);
//				if (searchStr != null && searchStr.trim().length() > 0)
//				{
//					productTypeQuery.setFullTextSearch(searchStr);
//					productTypeQuery.setFieldEnabled(AbstractProductTypeQuery.FieldName.fullTextSearch, true);
//				}
				configureQuery(productTypeQuery, searchStr);

				Collection<ProductTypeID> productTypeIDs;
				QueryCollection<?> q1 = queryProvider.getManagedQueries();
				QueryCollection<? extends AbstractProductTypeQuery> q = (QueryCollection<? extends AbstractProductTypeQuery>) q1;
				productTypeIDs = TradePlugin.getDefault().getStoreManager().getProductTypeIDs(q);
				final Collection<ProductType> productTypes = ProductTypeDAO.sharedInstance().getProductTypes(
						productTypeIDs,
						getFetchGroups(),
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new ProgressMonitorWrapper(monitor));

				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (productTypeTableComposite != null && !productTypeTableComposite.isDisposed()) {
							productTypeTableComposite.setInput(productTypes);
							if (productTypeTableComposite.getItemCount() == 1) {
								productTypeTableComposite.setSelection(new StructuredSelection(productTypes));
								productTypeTableComposite.setFocus();
							}
						}
					}
				});
				return Status.OK_STATUS;
			}
		};
		searchJob.schedule();
	}

	private ProductType selectedProductType = null;
	public ProductType getSelectedProductType() {
		return selectedProductType;
	}

	public void setSearchText(String searchTextString) {
		if (searchText != null && !searchText.isDisposed()) {
			searchText.setText(searchTextString);
		}
	}

	protected Collection<ProductType> retrieveProductTypes(
		QueryCollection<? extends AbstractProductTypeQuery> queries,
			ProgressMonitor monitor)
	{
		Collection<ProductTypeID> productTypeIDs;
		productTypeIDs = TradePlugin.getDefault().getStoreManager().getProductTypeIDs(queries);
		Collection<ProductType> productTypes = ProductTypeDAO.sharedInstance().getProductTypes(
				productTypeIDs,
				getFetchGroups(),
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				monitor);
		return productTypes;
	}

	private DefaultQueryProvider<AbstractSearchQuery> queryProvider;

//	protected ProductTypeSearchCriteriaComposite createSearchCriteriaComposite(Composite parent)
	protected Composite createSearchCriteriaComposite(Composite parent)
	{
		return new ProductTypeSearchCriteriaComposite(parent, SWT.NONE,	queryProvider, getQueryClass());
	}

	protected AbstractTableComposite<? extends ProductType> createProductTypeTable(Composite parent) {
		return new ProductTypeTableComposite(parent, SWT.NONE);
	}

	/**
	 * @return the queryProvider
	 */
	public DefaultQueryProvider<AbstractSearchQuery> getQueryProvider()
	{
		return queryProvider;
	}

	protected String getSearchText() {
		return searchText.getText();
	}

	protected void configureQuery(AbstractProductTypeQuery productTypeQuery, String searchStr)
	{
//		// ignore user input on the saleable flag, everything else doesn't make sense!
//		// TODO: we should tell the UI to NOT display the saleable flag or make it unmodifiable.
		productTypeQuery.setSaleable(true);
		productTypeQuery.setFieldEnabled(AbstractProductTypeQuery.FieldName.saleable, true);
		if (searchStr != null && searchStr.trim().length() > 0)
		{
			productTypeQuery.setFullTextSearch(searchStr);
			productTypeQuery.setFieldEnabled(AbstractProductTypeQuery.FieldName.fullTextSearch, true);
		}
		// TODO: FIXME: set vendorID so hat only productTypes of the current vendor can be found
//		productTypeQuery.setVendorID()
	}

//	protected AbstractProductTypeQuery configureQuery(AbstractProductTypeQuery query, String searchStr)
//	{
//		query.setFullTextSearch(".*"+searchStr+".*"); // Need to pass regex here //$NON-NLS-1$ //$NON-NLS-2$
//
//		if (getSearchCriteriaComposite().getSelectedSaleAccessState() != null) {
//			SaleAccessStateUtil.applySaleAccessState(
//					getSearchCriteriaComposite().getSelectedSaleAccessState(), query);
//		}
//
//		if (getSearchCriteriaComposite().getSelectedOwnerID() != null)
//			query.setOwnerID(getSearchCriteriaComposite().getSelectedOwnerID());
//
//		if (getSearchCriteriaComposite().getSelectedProductTypeGroupID() != null)
//			query.setProductTypeGroupID(getSearchCriteriaComposite().getSelectedProductTypeGroupID());
//
//		// TODO: FIXME: set vendorID so hat only productTypes of the current vendor can be found
//		return query;
//	}
}
