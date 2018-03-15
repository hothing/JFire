package org.nightlabs.jfire.base.ui.search;

import java.util.SortedSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.DefaultQueryProvider;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jdo.query.QueryProvider;
import org.nightlabs.jfire.base.ui.overview.search.QueryFilterFactory;
import org.nightlabs.jfire.base.ui.overview.search.QueryFilterFactoryRegistry;
import org.nightlabs.jfire.base.ui.resource.Messages;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class QueryFilterDialog
extends ResizableTitleAreaDialog
{
	private String scope;
	private QueryCollection<? extends AbstractSearchQuery> queryCollection;
	private QueryProvider queryProvider = null;
	private Class<?> targetType = null;

	/**
	 * @param parentShell
	 */
	public QueryFilterDialog(Shell parentShell, String scope,
			QueryCollection<? extends AbstractSearchQuery> queryCollection)
	{
		super(parentShell, null);
		if (scope == null)
			throw new IllegalArgumentException("scope must not be null!"); //$NON-NLS-1$

		if (queryCollection == null)
			throw new IllegalArgumentException("queryCollection must not be null!"); //$NON-NLS-1$

		this.scope = scope;
		this.queryCollection = queryCollection;
		this.targetType = queryCollection.getResultClass();
		this.queryProvider = new DefaultQueryProvider(targetType);
		queryProvider.loadQueries(queryCollection);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite wrapper = new XComposite(parent, SWT.NONE);
		SortedSet<QueryFilterFactory> factories = QueryFilterFactoryRegistry.sharedInstance().getQueryFilterCompositesFor(
				scope, targetType);
		if (factories != null) {
			for (QueryFilterFactory factory : factories) {
				AbstractQueryFilterComposite filterComp = factory.createQueryFilter(wrapper, SWT.NONE,
						LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA, queryProvider);
//				filterComp.getQueryProvider().loadQueries(queryCollection);
//				filterComp.setQueryProvider(queryProvider);
//				filterComp.getQueryProvider().loadQueries(queryCollection);
			}
		}
		return wrapper;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	@Override
	public void create() {
		super.create();
		getShell().setText(Messages.getString("org.nightlabs.jfire.base.ui.search.QueryFilterDialog.window.title"));		 //$NON-NLS-1$
		setTitle(Messages.getString("org.nightlabs.jfire.base.ui.search.QueryFilterDialog.title")); //$NON-NLS-1$
		setMessage(Messages.getString("org.nightlabs.jfire.base.ui.search.QueryFilterDialog.message")); //$NON-NLS-1$
	}

	public QueryCollection<?> getQueryCollection() {
		return queryProvider.getManagedQueries();
//		return queryCollection;
	}
}
