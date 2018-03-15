package org.nightlabs.jfire.base.ui.overview.search;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jfire.base.ui.overview.Entry;
import org.nightlabs.jfire.base.ui.search.AbstractQueryFilterComposite;

/**
 * Base Implementation of a {@link SearchEntryViewer} which is designed
 * to work with an implementation of {@link AbstractQueryFilterComposite} as
 * Composite returned by {@link #createSearchComposite(org.eclipse.swt.widgets.Composite)}
 * and an implementation of {@link AbstractTableComposite} as Composite returned
 * by {@link #createResultComposite(org.eclipse.swt.widgets.Composite)}
 * 
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class JDOQuerySearchEntryViewer<R, Q extends AbstractSearchQuery>
	extends SearchEntryViewer<R, Q>
{
	public JDOQuerySearchEntryViewer(Entry entry) {
		super(entry);
	}

	/**
	 * takes the given result and calls setInput(Object input)
	 * with it
	 */
	@Override
	protected void displaySearchResult(Object result)
	{
		if (getListComposite() != null) {
			getListComposite().setInput(result);
		}
	}

	@Override
	public Composite createResultComposite(Composite parent) {
		AbstractTableComposite<R> tableComposite = createListComposite(parent);
		addResultTableListeners(tableComposite);
		return tableComposite;
	}

	/**
	 * creates an {@link AbstractTableComposite} which is used as result composite
	 * this Method is called by {@link SearchEntryViewer#createResultComposite(Composite)}
	 * 
	 * @param parent the parent Composite
	 * @return the {@link AbstractTableComposite} which is used as result composite
	 */
	public abstract AbstractTableComposite<R> createListComposite(Composite parent);
	
	/**
	 * This method is called by {@link #createSearchComposite(Composite)} with
	 * the table created by {@link #createListComposite(Composite)}.
	 * <p>
	 * This implementation does nothing, but subclass may add listeners (for doubleclick etc.)
	 * to the table here.
	 * </p>
	 * @param tableComposite
	 */
	protected void addResultTableListeners(AbstractTableComposite<R>  tableComposite) {
	}
	
	/**
	 * returns the AbstractTableComposite created by {@link #createListComposite(Composite)}
	 * @return the AbstractTableComposite created by {@link #createListComposite(Composite)}
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public AbstractTableComposite<R> getListComposite() {
		return (AbstractTableComposite<R>) getResultComposite();
	}

}
