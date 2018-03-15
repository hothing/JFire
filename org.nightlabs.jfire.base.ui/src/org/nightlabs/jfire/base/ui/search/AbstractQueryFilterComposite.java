package org.nightlabs.jfire.base.ui.search;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryEvent;
import org.nightlabs.jdo.query.QueryProvider;
import org.nightlabs.jdo.query.AbstractSearchQuery.FieldChangeCarrier;

/**
 * Abstract base class for a Composite that uses the given {@link QueryProvider} to retrieve the
 * kind of query needed and set / modify a certain aspect of that query.
 *
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class AbstractQueryFilterComposite<Q extends AbstractSearchQuery>
	extends XComposite
	implements QueryFilter<Q>
{
	/**
	 * The active state manager that will handle the presentation of my active state.
	 */
	private ActiveStateManager sectionButtonActiveStateManager;

	/**
	 * Creates a new {@link AbstractQueryFilterComposite}.
	 * <p><b>Note</b>: The caller has to call {@link #createComposite()} to create the UI! <br />
	 * 	This is not done in this constructor to omit problems with fields that are not only declared,
	 * 	but also initialised. If these fields are used inside {@link #createComposite()}
	 * 	or new values are assigned to them, one of the following two things may happen:
	 *  <ul>
	 *  	<li>The value assigned to that field is overridden by the initialisation value that is
	 *  			assigned after this constructor is finished</li>
	 *  	<li>The referenced value is not yet properly initialised, because the initialisation is
	 *  			done after the constructor finishes, and hence results in an unexpected exception.</li>
	 *  </ul>
	 * </p>
	 * @param parent
	 *          The parent to instantiate this filter into.
	 * @param style
	 *          The style to apply.
	 * @param layoutMode
	 *          The layout mode to use. See {@link XComposite.LayoutMode}.
	 * @param layoutDataMode
	 *          The layout data mode to use. See {@link XComposite.LayoutDataMode}.
	 * @param queryProvider
	 *          The queryProvider to use. It may be <code>null</code>, but the caller has to
	 *          ensure, that it is set before {@link #getQuery()} is called!
	 */
	public AbstractQueryFilterComposite(Composite parent, int style, LayoutMode layoutMode,
		LayoutDataMode layoutDataMode, QueryProvider<? super Q> queryProvider)
	{
		super(parent, style, layoutMode, layoutDataMode);
		this.queryProvider = queryProvider;
		if (queryProvider != null)
		{
			queryProvider.addModifyListener(getQueryClass(), queryChangeListener);
		}
		// remove listener from QueryProvider on disposal
		addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				if (getQueryProvider() != null)
				{
					getQueryProvider().removeModifyListener(getQueryClass(), queryChangeListener);
				}
			}
		});
	}

	/**
	 * Creates a new {@link AbstractQueryFilterComposite} with default layout mode and layout data
	 * mode. Delegate to
	 * {@link AbstractQueryFilterComposite#AbstractQueryFilterComposite(Composite, int, LayoutMode, LayoutDataMode, QueryProvider)}.
	 *
	 * @param parent
	 *          The parent to instantiate this filter into.
	 * @param style
	 *          The style to apply.
	 * @param queryProvider
	 *          The queryProvider to use. It may be <code>null</code>, but the caller has to
	 *          ensure, that it is set before {@link #getQuery()} is called!
	 */
	public AbstractQueryFilterComposite(Composite parent, int style,
		QueryProvider<? super Q> queryProvider)
	{
		this(parent, style, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA, queryProvider);
	}

	/**
	 * Sets the active state of the given <code>button</code> to <code>active</code> if necessary and
	 * propagates the changes to the ActiveStateManager assigned to the active button in the section
	 * this composite is contained in. <br />
	 * Use this method instead {@link #setSearchSectionActive(boolean)}, where possible.
	 *
	 * @param button
	 * 					The button whose active state might have changed, and shall be set to
	 * 					<code>active</code>
	 * @param active
	 *          whether one of the filters contained in has gone <code>active</code>.
	 */
	protected void setSearchSectionActive(Button button, boolean active)
	{
		if (button.getSelection() == active)
			return;

		button.setSelection(active);
		getSectionButtonActiveStateManager().setActive(active);
	}

	/**
	 * Sets the active state of the given {@link #sectionButtonActiveStateManager} to
	 * <code>active</code>. It is related to {@link #setSearchSectionActive(Button, boolean)} and
	 * whenever possible you should use {@link #setSearchSectionActive(Button, boolean)}. <br />
	 * Nonetheless, this method is needed in case you don't have direct access to the button
	 * representing the active state of some UI element.
	 * <p><b>Important:</b> You have to make sure that you only call this if the state of the UI
	 * 	element differs from you're computed one. <br />
	 * 	AbstractArticleContainerFilterComposite#updateUI is a nice example.
	 * </p>
	 *
	 * @param active
	 * 					Whether the state of some UI element changed to <code>active</code>.
	 */
	protected void setSearchSectionActive(boolean active)
	{
		getSectionButtonActiveStateManager().setActive(active);
	}

	protected void resetSearchSectionActiveState()
	{
		while (sectionButtonActiveStateManager.isActive())
		{
			sectionButtonActiveStateManager.setActive(false);
		}
	}

	/**
	 * This is has to be called by the constructor of the subclass.
	 */
	protected abstract void createComposite();

//	/**
//	 * Resets the value in the query
//	 */
//	public abstract void resetQueryData();
//	{
//		for (String fieldName : getFieldNames()) {
//			getQuery().setFieldEnabled(fieldName, false);
//		}
//	}
	
	@Override
	public void setActive(boolean active)
	{
		if (active)
		{ // user clicked on the active checkbox of the section -> reset to previously stored configuration
			getQuery().restoreBackupOfGroup(getGroupID());
		}
		else
		{ // user deactivated the "active" checkbox of the section -> create backup of config & set all
			// active states of the properties displayed to false.
			getQuery().storeBackupOfGroupFieldsEnabledState(getGroupID(), getFieldNames());
			for (String fieldName : getFieldNames())
			{
				getQuery().setFieldEnabled(fieldName, Boolean.FALSE);
			}
		}
	}

	protected abstract String getGroupID();
	protected abstract Set<String> getFieldNames();

	/**
	 * Convenience method to get the name of the field storing the enable state of the given
	 * <code>originalFieldName</code>.
	 *
	 * @param originalFieldName The name of the field for which to return the name of the corresponding
	 * 	enable field.
	 * @return The name of the field storing the enable state of the given <code>originalFieldName</code>.
	 */
	protected String getEnableFieldName(String originalFieldName)
	{
		return AbstractSearchQuery.getEnabledFieldName(originalFieldName);
	}

	/**
	 * Returns the {@link Class} of the type of object which should queried.
	 *
	 * @return the {@link Class} of the type of object which should queried.
	 */
	public abstract Class<Q> getQueryClass();

	private QueryProvider<? super Q> queryProvider;
	private PropertyChangeListener queryChangeListener = new PropertyChangeListener()
	{
		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			final QueryEvent event = (QueryEvent) evt;

			List<FieldChangeCarrier> changedFields;
			if (event.getChangedQuery() == null)
			{
				changedFields = getQuery().getChangedFields(AbstractSearchQuery.PROPERTY_WHOLE_QUERY);
			}
			else
			{ // there is a new Query -> the changedFieldList is not null!
				changedFields = event.getChangedFields();
			}

			updateUI((QueryEvent) event, changedFields);
		}
	};

	/**
	 * This is the main method that nearly all UI changes are done in. There are several occasions
	 * resulting in a call of this method.
	 * <ul>
	 * 	<li> By changing the UI, the UI should change the query -> the QueryProvider will get notified
	 * 			 and thus a listener in the QueryProvider will trigger this method.</li>
	 *  <li> If some other UI triggers a call of {@link #setActive(boolean)}, then we
	 *  		 un/re-set the query's field values and hence trigger an update.</li>
	 *  <li> The assigned QueryProvider loads a set of other queries
	 *  		 {@link QueryProvider#loadQueries(org.nightlabs.jdo.query.QueryCollection)}, hence we'll
	 *  		 get notified an this method is called.</li>
	 * </ul>
	 *
	 * <p><b>Notes for implementors:</b>
	 * 	<ul>
	 * 		<li>When iterating over all {@link QueryEvent#getChangedFields()} you have to consider that
	 * 				you have to also check all combinations of
	 * 				{@link AbstractSearchQuery#getEnabledFieldName(String)} with the known FieldNames of the
	 * 				type of query you are dealing with.</li>
	 * 		<li>Please look at AbstractArticleContainerFilterComposite to see an example.</li>
	 *  </ul>
	 * </p>
	 *
	 * @param event The event containing the old and the new value of the changed property.
	 * @param changedFields The already fetched list of changes.
	 */
	protected abstract void updateUI(QueryEvent event, List<FieldChangeCarrier> changedFields);

	/**
	 * @return the Query needed by this query filter and hence all JDOQueryComposites as well.
	 */
	public Q getQuery()
	{
		assert queryProvider != null : "No query provider has been set before!"; //$NON-NLS-1$
		return queryProvider.getQueryOfType(getQueryClass());
	}

	/**
	 * @param queryProvider
	 *          the queryProvider to set that must not be <code>null</code>!
	 */
	@Override
	public void setQueryProvider(QueryProvider<? super Q> queryProvider)
	{
		assert queryProvider != null;
		if (this.queryProvider != null)
		{
			this.queryProvider.removeModifyListener(getQueryClass(), queryChangeListener);
		}
		this.queryProvider = queryProvider;
		this.queryProvider.addModifyListener(getQueryClass(), queryChangeListener);
	}

	/**
	 * Returns the query provider
	 *
	 * @return the query provider
	 */
	public QueryProvider<? super Q> getQueryProvider()
	{
		return queryProvider;
	}

	/**
	 * @return the sectionButtonActiveStateManager
	 */
	public ActiveStateManager getSectionButtonActiveStateManager()
	{
		return sectionButtonActiveStateManager;
	}

	/**
	 * @param sectionButtonActiveStateManager
	 *          the sectionButtonActiveStateManager to set
	 */
	public void setSectionButtonActiveStateManager(ActiveStateManager sectionButtonActiveStateManager)
	{
		assert sectionButtonActiveStateManager != null;
		this.sectionButtonActiveStateManager = sectionButtonActiveStateManager;
	}

	/**
	 * Helper class that tries to set the ActiveStateManager if it wasn't available at creation time.
	 * Needed since my ActiveStateManager will be set after the UI has been constructed.
	 *
	 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
	 */
	protected abstract class ButtonSelectionListener
		extends ButtonSelectionStateAdapter
	{
		public ButtonSelectionListener()
		{
			super(getSectionButtonActiveStateManager());
		}

		@Override
		public void widgetSelected(SelectionEvent e)
		{
			if (activeStateManager == null)
			{
				activeStateManager = getSectionButtonActiveStateManager();
			}

			super.widgetSelected(e);
		}
	}
}
