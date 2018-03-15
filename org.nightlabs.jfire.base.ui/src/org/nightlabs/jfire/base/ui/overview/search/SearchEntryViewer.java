package org.nightlabs.jfire.base.ui.overview.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.message.IErrorMessageDisplayer;
import org.nightlabs.base.ui.message.MessageType;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.toolkit.IToolkit;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.datastructure.Pair;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.DefaultQueryProvider;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jdo.query.QueryProvider;
import org.nightlabs.jfire.base.ui.overview.AbstractEntryViewer;
import org.nightlabs.jfire.base.ui.overview.Entry;
import org.nightlabs.jfire.base.ui.overview.EntryViewer;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.search.AbstractQueryFilterComposite;
import org.nightlabs.jfire.base.ui.search.ActiveStateButtonManager;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Base class for creating {@link EntryViewer}s which are responsible for searching
 *
 * Subclasses must implement 4 Methods:
 *
 * <ul>
 * 	<li>{@link #createResultComposite(Composite)} which creates a Composite which displays the result
 * 	of the search</li>
 * 	<li>{@link #doSearch(QueryCollection, ProgressMonitor)} that performs the actual search, i.e.
 * 	passes the QueryCollection to the appropriate DAO to retrieve the result to display.
 * 	<li>{@link #displaySearchResult(Object)} must display the search result passed to this method</li>
 * 	<li>{@link #getTargetType()} that represents the result of the queries and is used to initialise
 * 	the {@link QueryProvider}.</li>
 * 	<li>{@link #getQuickSearchRegistryID()} that return the key for finding all registered
 * 	QuickSearchEntries.</li>
 * </ul>
 * @param <R> the type of objects shown by my table.
 * @param <Q> the type of query I am insisting on. This tries to ensure some type safety, so that no
 * 	one can register an additional section from another plugin and add queries not related to a viewer
 * 	to bypass the safety barrier and view elements not intended to be seen by logged-in user.
 *
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class SearchEntryViewer<R, Q extends AbstractSearchQuery>
	extends AbstractEntryViewer
{
	public static final Logger logger = Logger.getLogger(SearchEntryViewer.class);

	public SearchEntryViewer(Entry entry) {
		super(entry);
		queryProvider = new DefaultQueryProvider<Q>(getTargetType());
	}

	/**
	 * The element creating and providing the queries required by the UI.
	 */
	protected QueryProvider<Q> queryProvider;

	private ScrolledComposite scrollableSearchWrapper;
	private XComposite toolbarAndAdvancedSearchWrapper;
	private SashForm sashform;
	private ToolItem searchItem;
	private ToolItem resetItem;
	private ToolBar searchTextToolBar;
	private IErrorMessageDisplayer errMsgDisplayer;

	public Composite createComposite(Composite parent)
	{
		sashform = new SashForm(parent, SWT.VERTICAL);
		IToolkit toolkit = XComposite.retrieveToolkit(parent);

		scrollableSearchWrapper = new ScrolledComposite(sashform, SWT.V_SCROLL);
		scrollableSearchWrapper.setExpandHorizontal(true);
		scrollableSearchWrapper.setExpandVertical(true);

		toolbarAndAdvancedSearchWrapper = new XComposite(scrollableSearchWrapper, SWT.NONE,
			LayoutMode.ORDINARY_WRAPPER);

		Control toolbar = createToolBar(toolbarAndAdvancedSearchWrapper, toolkit);
		toolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createQuickSearchEntries(searchItem);
		createAdvancedSearchSections(toolbarAndAdvancedSearchWrapper, toolkit);
		scrollableSearchWrapper.setContent(toolbarAndAdvancedSearchWrapper);
		// TODO: Even though the min size is set for the width and the height, the scrollableSearchWrapper only shows scrollbars when not able to show the full height... why??? (marius)
		// When we have found a way to make the horizontal scrollbar visible, then we should add a resize listener to the scrollableSearchWrapper and set the new MinSize as long as it changes.
		scrollableSearchWrapper.setMinHeight(toolbarAndAdvancedSearchWrapper.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		scrollableSearchWrapper.setMinWidth(500);
		resultComposite = createResultComposite(sashform);

		// Form Look & Feel
		toolbarAndAdvancedSearchWrapper.setToolkit(toolkit);
		toolbarAndAdvancedSearchWrapper.adaptToToolkit();
		if (resultComposite instanceof XComposite)
		{
			final XComposite resultXComposite = (XComposite) resultComposite;
			resultXComposite.setToolkit(toolkit);
			resultXComposite.adaptToToolkit();

			if (resultXComposite instanceof AbstractTableComposite<?>)
			{
				final AbstractTableComposite<?> tableComp = (AbstractTableComposite<?>) resultXComposite;
				final GridData tableData = (GridData) tableComp.getTableViewer().getTable().getLayoutData();
				tableData.minimumWidth = 500;
				tableData.minimumHeight = 200;
			}
		}
		else if (toolkit != null)
		{
			toolkit.adapt(resultComposite);
		}

		// Context Menu
		menuManager = new MenuManager();
		Menu contextMenu = menuManager.createContextMenu(parent);
		resultComposite.setMenu(contextMenu);

		sashform.setWeights(calculateSashWeights(null));

		return sashform;
	}

	/**
	 * List of all sections containing advanced search information.
	 */
	private List<Section> advancedSearchSections;

	/**
	 * @param parent the Composite to create the sections into.
	 * @param toolkit the toolkit to use for section creation, etc.
	 */
	protected void createAdvancedSearchSections(Composite parent, IToolkit toolkit)
	{
		SortedSet<QueryFilterFactory> queryFilterFactories = getQueryFilterFactories();

		if (queryFilterFactories == null || queryFilterFactories.isEmpty())
		{
			advancedSearchSections = Collections.emptyList();
			return;
		}

		advancedSearchSections = new ArrayList<Section>(queryFilterFactories.size());

		Section advancedSearchSection;
		Button advancedSectionActiveButton;
		final int sectionStyle = ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
			| ExpandableComposite.CLIENT_INDENT;

		for (QueryFilterFactory factory : queryFilterFactories)
		{
			if (toolkit != null)
			{
				advancedSearchSection = toolkit.createSection(parent, sectionStyle);
			}
			else
			{
				advancedSearchSection = new Section(parent, sectionStyle);
			}
			advancedSearchSection.setLayout(new GridLayout());
			advancedSearchSection.setText(factory.getTitle());
			advancedSearchSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			advancedSearchSection.addExpansionListener(expansionListener);

			advancedSectionActiveButton = new Button(advancedSearchSection, SWT.CHECK);
			advancedSectionActiveButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.AbstractQueryFilterComposite.activeButton.text")); //$NON-NLS-1$
			advancedSectionActiveButton.setSelection(false);

			advancedSearchSection.setTextClient(advancedSectionActiveButton);

			AbstractQueryFilterComposite<? extends Q> filterComposite =
				factory.createQueryFilter(
					advancedSearchSection, SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER,
					LayoutDataMode.GRID_DATA_HORIZONTAL, queryProvider
					);
			filterComposite.setSectionButtonActiveStateManager(
				new ActiveStateButtonManager(advancedSectionActiveButton) );
			advancedSearchSection.setClient(filterComposite);

			advancedSectionActiveButton.addSelectionListener(
				new ActiveButtonSelectionListener(advancedSearchSection, filterComposite) );
			advancedSearchSections.add(advancedSearchSection);
		}
	}

	/**
	 * Returns the SortedSet of QueryFilterFactories that will be used to create the advanced search
	 * sections.
	 *
	 * @return the SortedSet of QueryFilterFactories that will be used to create the advanced search
	 * sections.
	 */
	protected SortedSet<QueryFilterFactory> getQueryFilterFactories()
	{
		return QueryFilterFactoryRegistry.sharedInstance().getQueryFilterCompositesFor(
			getScope(), getTargetType()
			);
	}

	/**
	 * @return The sashform containing the search part (toolbar & advanced search sections) and the
	 * 	result composite (displaying the found elements).
	 */
	public Composite getComposite()
	{
		if (sashform == null)
			throw new IllegalStateException("createComposite() was not called before getComposite()!"); //$NON-NLS-1$

		return sashform;
	}

	private ToolBarManager toolBarManager = null;

	/**
	 * @return the ToolBarManager used for the search tool bar with the quick search entries.
	 */
	public ToolBarManager getToolBarManager()
	{
		return toolBarManager;
	}

	protected Text searchText = null;

	private Spinner limit;

	/**
	 * creates the top toolbar including the search text, the search item where
	 * all the quickSearchEntries will be displayed, the limit spinner and
	 * an additional toolbar where custom actions can be added
	 *
	 * @param searchComposite the parent Composite where the toolbar will be located in
	 * @param toolkit the toolkit to use
	 * @return the control representing the toolbar in the header of this viewer.
	 */
	protected Control createToolBar(final XComposite searchComposite, IToolkit toolkit)
	{
		XComposite toolBarWrapper = new XComposite(searchComposite, SWT.NONE,
				LayoutMode.TIGHT_WRAPPER, LayoutDataMode.NONE, 5);

		int toolbarStyle = SWT.WRAP | SWT.HORIZONTAL;
		if (toolkit != null)
		{
			toolbarStyle |= SWT.FLAT;
		}
		int borderStyle = searchComposite.getBorderStyle();

		XComposite quickSeachTextComp = new XComposite(toolBarWrapper, SWT.NONE,
				LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.NONE, 2);
		GridLayout gridLayout = quickSeachTextComp.getGridLayout();
		gridLayout.marginLeft = gridLayout.marginWidth;
		gridLayout.marginWidth = 0;
		gridLayout.marginRight = 0;
		Label searchLabel = new Label(quickSeachTextComp, SWT.NONE);
		searchLabel.setText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer.searchLabel.text")); //$NON-NLS-1$
		searchText = new Text(quickSeachTextComp, borderStyle);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.minimumWidth = 200;
		searchText.setLayoutData(gridData);
		searchText.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				search();
			}
		});
		searchText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent event)
			{
				if (activeMenuItem == null)
					return;

				QuickSearchEntry<?> activeQuickSearchEntry = (QuickSearchEntry<?>) activeMenuItem.getData();
				final String newSearchValue = searchText.getText();
				final Pair<MessageType, String> valResult = activeQuickSearchEntry.validateSearchCondionValue(newSearchValue);
				if (valResult == null)
				{
					errMsgDisplayer.setMessage(null, MessageType.NONE);
					activeQuickSearchEntry.setSearchConditionValue(newSearchValue);
				}
				else
					errMsgDisplayer.setMessage(valResult.getSecond(), valResult.getFirst());
			}
		});

		searchTextToolBar = new ToolBar(toolBarWrapper, toolbarStyle);

		searchItem = new ToolItem(searchTextToolBar, SWT.DROP_DOWN);
		searchItem.setImage(SharedImages.SEARCH_16x16.createImage());
		searchItem.addSelectionListener(searchItemListener);

		resetItem = new ToolItem(searchTextToolBar, SWT.PUSH);
		resetItem.setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer.item.clearAllCriteria.tooltip")); //$NON-NLS-1$
		resetItem.setImage(SharedImages.RESET_16x16.createImage());
		resetItem.addSelectionListener(resetItemListener);

		XComposite rangeWrapper = new XComposite(toolBarWrapper, SWT.NONE,
				LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.NONE, 2);
		Label limitLabel = new Label(rangeWrapper, SWT.NONE);
		limitLabel.setText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer.limitLabel.text")); //$NON-NLS-1$
		limitLabel.setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer.limitLabel.tooltip")); //$NON-NLS-1$

		limit = new Spinner(rangeWrapper, borderStyle);
		limit.setMinimum(0);
		limit.setMaximum(Integer.MAX_VALUE);
		limit.setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer.limitLabel.tooltip")); //$NON-NLS-1$
		limit.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				queryProvider.setToExclude( limit.getSelection() );
			}
		});
		limit.setSelection(25);
		queryProvider.setToExclude(25);

		Label spacerLabel = new Label(toolBarWrapper, SWT.NONE);
		spacerLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ToolBar toolBar = new ToolBar(toolBarWrapper, toolbarStyle);
		GridData actionToolBarData = new GridData(SWT.END, SWT.FILL, false, true);
		actionToolBarData.minimumWidth = 200;
		toolBar.setLayoutData(actionToolBarData);
		toolBarManager = new ToolBarManager(toolBar);
		if (toolkit != null)
		{
			toolBarWrapper.setToolkit(toolkit);
			toolBarWrapper.adaptToToolkit();
			quickSeachTextComp.setToolkit(toolkit);
			quickSeachTextComp.adaptToToolkit();
			rangeWrapper.setToolkit(toolkit);
			rangeWrapper.adaptToToolkit();
			toolkit.adapt(toolBar);
		}

		return toolBarWrapper;
	}

	/**
	 * Implement this method for displaying the result of a search
	 *
	 * @param parent the parent {@link Composite}
	 * @return a Composite which displays the result of a search
	 */
	public abstract Composite createResultComposite(Composite parent);

	/**
	 * @return the global scope, i.e. the scope where a GUI reflecting all fields of the Query is
	 * registered for every Query.
	 */
	protected String getScope()
	{
		return QueryFilterFactory.GLOBAL_SCOPE;
	}

	/**
	 * Returns the class representing the result of all the queries. It is used to initialise the
	 * {@link DefaultQueryProvider} used throughout this viewer.
	 *
	 * @return the class representing the result of all the queries. It is used to initialise the
	 * {@link DefaultQueryProvider} used throughout this viewer.
	 */
	public abstract Class<R> getTargetType();

	private Composite resultComposite;
	protected Composite getResultComposite() {
		return resultComposite;
	}

	/**
	 * Performs a search with the current criteria.
	 *
	 * Furthermore the selected result ranges are set
	 * and after the search is done {@link #displaySearchResult(Object)} is called
	 */
	public void search()
	{
		if (Display.getCurrent() == null)
			throw new IllegalStateException("Thread mismatch! This method must be called on the UI thread!");

		final Display display = sashform.getDisplay();
		final QueryCollection<Q> c = queryProvider.getManagedQueries();
		Job searchJob = new Job(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer.searchJob.name"))	//$NON-NLS-1$
		{
			@Override
			protected IStatus run(final ProgressMonitor monitor)
			{
				final Job thisJob = this;
				if (currentSearchJob != thisJob)
					return Status.CANCEL_STATUS;

				final Collection<R> result = doSearch(c, monitor);
				if (currentSearchJob != thisJob)
					return Status.CANCEL_STATUS;

				display.asyncExec(new Runnable()
				{
					public void run()
					{
						if (sashform.isDisposed())
							return;

						if (currentSearchJob != thisJob)
							return;
						else
							currentSearchJob = null;

						displaySearchResult(result);
					}
				});
				return Status.OK_STATUS;
			}
		};
		searchJob.setUser(true);
		Job oldJob = this.currentSearchJob;
		if (oldJob != null)
			oldJob.cancel();
		this.currentSearchJob = searchJob;
		searchJob.schedule();
	}

	private transient volatile Job currentSearchJob;

	/**
	 * Performs a reseting with all criteria.
	 */
	public void doReset()
	{
		for (AbstractSearchQuery searchQuery : getQueryProvider())
		{
			searchQuery.clearQuery();
		}

		// clear the parts not listening on query changes...
		searchText.setText(""); //$NON-NLS-1$
		for (Section advancedSearchSection : advancedSearchSections) {
			advancedSearchSection.setExpanded(false);
		}

		sashform.setWeights(calculateSashWeights(null));
	}

	/**
	 * The actual search should be done here. It is advised to do it via the DAOs.
	 *
	 * @param queryMap the {@link QueryCollection} containing all queries managed by this viewer.
	 * @param monitor the monitor to show the progress.
	 * @return a collection of all elements matching the cascaded queries of the <code>queryMap</code>.
	 */
	protected abstract Collection<R> doSearch(QueryCollection<? extends Q> queryMap, ProgressMonitor monitor);

	/**
	 * will be called after the search of the current {@link QuickSearchEntry}
	 * {@link #searchEntryType} is done and the result should be displayed
	 * in the Composite returned by {@link #createResultComposite(Composite)}
	 *
	 * @param result the search result to display
	 */
	protected abstract void displaySearchResult(Object result);

	private MenuManager menuManager;
	public MenuManager getMenuManager() {
		return menuManager;
	}

	public ISelectionProvider getSelectionProvider()
	{
		if (resultComposite instanceof AbstractTableComposite)
		{
			AbstractTableComposite<?> tableComposite = (AbstractTableComposite<?>) resultComposite;
			return tableComposite.getTableViewer();
		}
		return null;
	}

	private SelectionListener searchItemListener = new SelectionAdapter()
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			if (e.detail != SWT.ARROW)
				search();
		}
	};

	private SelectionListener resetItemListener = new SelectionAdapter()
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			doReset();
		}
	};

	/**
	 * Listener used to adapt the sash weights to the new size of the search composite and
	 * sets the minimum size of the search composite so that the scrollbars will be shown correctly.
	 */
	protected IExpansionListener expansionListener = new ExpansionAdapter()
	{
		@Override
		public void expansionStateChanged(ExpansionEvent e)
		{
			final Section section = (Section) e.getSource();
			final int[] sashWeights = calculateSashWeights(section);
			sashform.setWeights(sashWeights);
			// This is not enough since the returned size is not the actual one if the searchWrapper is
			// completely visible (e.g. when it grows too big for the sash itself, then the weights are
			// not changed and the searchWrapper's returned size is only its visible part)
//			scrollableSearchWrapper.setMinHeight(scrollableSearchWrapper.getSize().y);

			// We need the -1 otherwise scrollbars are sometimes visible. Another magical number...
			// sashweights[0] == calculateSearchAreaHeight(), so we save some runtime here.
			scrollableSearchWrapper.setMinHeight(sashWeights[0]-1);
		}
	};

	/**
	 * The maximum number of retries for the {@link #setInitialSashWeightsRunnable} to get the correct
	 * bounds of the SearchEntryViewer's parent.
	 */
	private static final int maxTries = 10;
	private int parentSizeRetrievalTries = 0;

	/**
	 * This runnable tries to retrieve the correct parent's height for {@link #maxTries} times.
	 * When retrieved it calculates the correct sash weights and sets them.
	 */
	private Runnable setInitialSashWeightsRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			int completeHeight = sashform.getParent().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
			if (completeHeight > 0)
			{
				final int[] sashWeights = calculateSashWeights(null);
				sashform.setWeights(sashWeights);
				scrollableSearchWrapper.setMinHeight(sashWeights[0]-1);
				return;
			}

			if (parentSizeRetrievalTries < maxTries)
			{
				parentSizeRetrievalTries++;
				Display.getDefault().asyncExec(setInitialSashWeightsRunnable);
			}
		}
	};

	/**
	 * @param expandedStateChangedSection the section that has changed its expanded state.
	 * @return the two weights for the search and the result composite.
	 */
	protected int[] calculateSashWeights(Section expandedStateChangedSection)
	{
		// Calculate the total space available for both composites (search comp and result comp)
		int completeHeight;
		int searchHeight;
		if (expandedStateChangedSection == null)
		{
			// we need to initialise size values because these composites haven't been layed out correctly.
//			searchHeight = scrollableSearchWrapper.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
			searchHeight = calculateSearchAreaHeight();
			if (searchHeight < 0)
			{
				searchHeight = 150;
			}
			// NOTE: we cannot compute the size of the sashform, since in the parent tree, the bounds/ available size are/is not known yet.
//			completeHeight = sashform.getParent().computeSize(SWT.DEFAULT, SWT.DEFAULT).y - sashform.SASH_WIDTH;
			completeHeight = sashform.getParent().getSize().y - sashform.getParent().getBorderWidth(); // - sashform.SASH_WIDTH;
			if (completeHeight <= 0)
			{
				completeHeight = 4 * searchHeight;
				Display.getDefault().asyncExec(setInitialSashWeightsRunnable);
			}
		}
		else
		{
//			scrollableSearchWrapper.layout(true, true);
			completeHeight = sashform.getClientArea().height - sashform.SASH_WIDTH;
			searchHeight = calculateSearchAreaHeight();
//			searchHeight = scrollableSearchWrapper.getSize().y; // computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		}


		// only move the sash if after the adjustment of the sash, the result composite
		// still has some height left (at least 50 pixels).
		if (searchHeight + 50 > completeHeight)
		{
			return sashform.getWeights();
		}

		int resultHeight = completeHeight - searchHeight;
		return new int[] { searchHeight, resultHeight };
	}

	/**
	 * @return the complete height of the search area.
	 */
	private int calculateSearchAreaHeight()
	{
		int searchHeight = toolBarManager.getControl().getSize().y;

		// spacing used as a buffer for minimum area heights
		final int verticalSpacing;
		if (toolbarAndAdvancedSearchWrapper.getLayout() instanceof GridLayout)
		{
			GridLayout gridLayout = (GridLayout) toolbarAndAdvancedSearchWrapper.getLayout();
			verticalSpacing = gridLayout.verticalSpacing;
		}
		else
		{
			verticalSpacing = 10;
		}

		for (Section section : advancedSearchSections)
		{
			if (section.getSize().y > 0)
				searchHeight += section.getSize().y;
			else
				searchHeight += section.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		}

		// add the spacing in between the sections
		searchHeight += advancedSearchSections.size() * verticalSpacing;
		searchHeight += advancedSearchSections.size() * 2; //magical spacing needed due to section spacings.
		return searchHeight;
	}

	/**
	 * Reference to the menu containing all quick search items.
	 */
	protected Menu quickSearchMenu;

	/**
	 * Pointer to the currently active MenuItem
	 */
	private MenuItem activeMenuItem = null;

	/**
	 * Gets all registered {@link QuickSearchEntryFactory}s and adds the entries created by these
	 * factories to the {@link #quickSearchMenu}.
	 *
	 * @param searchItem the ToolBar item that acts as the button to trigger the drop down list of
	 * 	quick search entries.
	 */
	protected void createQuickSearchEntries(final ToolItem searchItem)
	{
		if (getQuickSearchEntryFactories() == null || getQuickSearchEntryFactories().isEmpty())
			return;

		Collection<QuickSearchEntryFactory> quickSearchEntryFactories = getQuickSearchEntryFactories();
		quickSearchMenu = new Menu(RCPUtil.getActiveShell(), SWT.POP_UP);

		for (final QuickSearchEntryFactory quickSearchEntryFactory : quickSearchEntryFactories) {
			final MenuItem menuItem = new MenuItem(quickSearchMenu, SWT.CHECK);
			menuItem.setText(quickSearchEntryFactory.getName());
			menuItem.setImage(quickSearchEntryFactory.getImage());
			final QuickSearchEntry<? extends Q> quickSearchEntry = quickSearchEntryFactory.createQuickSearchEntry();
			quickSearchEntry.setQueryProvider(queryProvider);
			menuItem.setData(quickSearchEntry);

			menuItem.addSelectionListener(dropDownMenuSelectionAdapter);

			// initialise the first query and set empty condition
			if (quickSearchEntryFactory.isDefault())
			{
				if (activeMenuItem != null) // there is already an item declared as default.
				{
					logger.warn("There is already a quick search entry marked as default! This entry with id="+ //$NON-NLS-1$
						quickSearchEntryFactory.getId() + " and name=" +quickSearchEntryFactory.getName() + //$NON-NLS-1$
					" is also declared as 'default'! This declaration is ignored."); //$NON-NLS-1$
				}
				else
				{
					quickSearchEntry.setSearchConditionValue(""); //$NON-NLS-1$
					activeMenuItem = menuItem;
					activeMenuItem.setSelection(true);
				}
			}
		}

		if (activeMenuItem == null && quickSearchMenu.getItems().length > 0)
		{
			activeMenuItem = quickSearchMenu.getItem(0);
			activeMenuItem.setSelection(true);
			((QuickSearchEntry)activeMenuItem.getData()).setSearchConditionValue(""); //$NON-NLS-1$
		}

		searchItem.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event event)
			{
				if (event.detail == SWT.ARROW)
				{
					Rectangle rect = searchItem.getBounds();
					Point p = new Point(rect.x, rect.y + rect.height);
					p = searchItem.getParent().toDisplay(p);
					quickSearchMenu.setLocation(p.x, p.y);
					quickSearchMenu.setVisible(true);
				}
			}
		});
	}

	protected SelectionListener dropDownMenuSelectionAdapter = new SelectionAdapter()
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			final MenuItem newSelectedItem = (MenuItem) e.getSource();
			// unset search condition of old selected element
			if (activeMenuItem != null)
			{
				QuickSearchEntry<?> entry = (QuickSearchEntry<?>) activeMenuItem.getData();
				entry.unsetSearchCondition();
			}

			// erase old selection
			for (MenuItem mi : newSelectedItem.getParent().getItems())
			{
				mi.setSelection(false);
			}

			// update selected search entry with possibly changed search text
			newSelectedItem.setSelection(true);
			// update last element pointer
			activeMenuItem = newSelectedItem;

			final QuickSearchEntry<?> entry = (QuickSearchEntry<?>) newSelectedItem.getData();
			final String searchValue = searchText.getText();
			final Pair<MessageType, String> valResult = entry.validateSearchCondionValue(searchValue);
			if (valResult != null)
			{
				errMsgDisplayer.setMessage(valResult.getSecond(), valResult.getFirst());
				return;
			}

			// no error occurred -> set value
			errMsgDisplayer.setMessage(null, MessageType.NONE);
			entry.setSearchConditionValue(searchValue);

			// start a new search
			search();
		}
	};

	/**
	 * Subclasses can return here their implementations of {@link QuickSearchEntryFactory}
	 * which can be used for searching
	 *
	 * @return a List of {@link QuickSearchEntryFactory}s will can be used for quick searching
	 */
	protected SortedSet<QuickSearchEntryFactory> getQuickSearchEntryFactories()
	{
		return QuickSearchEntryRegistry.sharedInstance().getFactories(getQuickSearchRegistryID());
	}

	/**
	 * Returns the ID used to retrieve all registered {@link QuickSearchEntryFactory}s.
	 * @return the ID used to retrieve all registered {@link QuickSearchEntryFactory}s.
	 */
	protected abstract String getQuickSearchRegistryID();

	protected class ActiveButtonSelectionListener
		extends SelectionAdapter
	{
		private Section correspondingSection;
		private AbstractQueryFilterComposite<? extends Q> filterComposite;

		/**
		 * @param correspondingSection The section that corresponds to the button this listener is
		 * 	added to.
		 * @param filterComposite
		 */
		public ActiveButtonSelectionListener(Section correspondingSection,
			AbstractQueryFilterComposite<? extends Q> filterComposite)
		{
			assert correspondingSection != null;
			assert filterComposite != null;
			this.correspondingSection = correspondingSection;
			this.filterComposite = filterComposite;
		}

		@Override
		public void widgetSelected(SelectionEvent e)
		{
			final Button b = (Button) e.getSource();
			final boolean active = b.getSelection();

			filterComposite.setActive(active);

			if (active != correspondingSection.isExpanded())
			{
				correspondingSection.setExpanded(active);

				// This needs to be triggered manually, since the setExpanded(boolean) won't trigger the
				// notification of its listeners.
				final ExpansionEvent event = new ExpansionEvent(correspondingSection, active);
				expansionListener.expansionStateChanged(event);
			}
		}
	}

	/**
	 * Returns the collection of queries managed by this viewer.
	 * @return the collection of queries managed by this viewer.
	 */
	public QueryCollection<Q> getManagedQueries()
	{
		return queryProvider.getManagedQueries();
	}

	/**
	 * @return the queryProvider
	 */
	public QueryProvider<Q> getQueryProvider()
	{
		return queryProvider;
	}

	@Override
	public IErrorMessageDisplayer getErrorMessageDisplayer()
	{
		return errMsgDisplayer;
	}

	@Override
	public void setErrorMessageDisplayer(IErrorMessageDisplayer displayer)
	{
		this.errMsgDisplayer = displayer;
	}
}