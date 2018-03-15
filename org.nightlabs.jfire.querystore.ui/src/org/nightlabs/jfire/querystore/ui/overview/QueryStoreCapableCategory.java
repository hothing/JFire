package org.nightlabs.jfire.querystore.ui.overview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.jfire.base.ui.overview.CategoryFactory;
import org.nightlabs.jfire.base.ui.overview.DefaultCategory;

/**
 * This category provides the default view of the {@link DefaultCategory} and additionally
 * a table showing all stored QueryStores related to the shown Entry.
 * <p><b>Important:</b> In order for me to work properly the Entries have to fulfil the
 * 			prerequisites defined in {@link QueryStoreCapableCategoryComposite}.
 * </p>
 *
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class QueryStoreCapableCategory
	extends DefaultCategory
{
	private QueryStoreCapableCategoryComposite categoryComposite;

	public QueryStoreCapableCategory(CategoryFactory categoryFactory)
	{
		super(categoryFactory);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.querystore.ui.overview.Category#createComposite(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Composite createComposite(Composite composite)
	{
		categoryComposite = new QueryStoreCapableCategoryComposite(composite, SWT.NONE, this);
		return categoryComposite;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.querystore.ui.overview.Category#getComposite()
	 */
	@Override
	public Composite getComposite()
	{
		return categoryComposite;
	}

	@Override
	protected void updateCategoryComposite()
	{
		if (categoryComposite == null)
			return;

		if (Display.getCurrent() == null)
		{
			categoryComposite.getDisplay().asyncExec(new Runnable ()
			{
				@Override
				public void run()
				{
					doUpdateComposite();
				}
			});
		}
		else
		{
			doUpdateComposite();
		}
	}

	protected void doUpdateComposite()
	{
		categoryComposite.setInput(getEntries());
	}
}
