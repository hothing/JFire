package org.nightlabs.jfire.base.ui.search;

import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryProvider;

/**
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public interface QueryFilter<Q extends AbstractSearchQuery>
{
	/**
	 * Sets the implementing filter <code>active</code>.
	 * @param active <code>true</code> if the implementing filter should set the aspects it's
	 * 	controlling to the query, otherwise these aspects are nulled in the query.
	 */
	void setActive(boolean active);

	/**
	 * The QueryProvider from which the query needed by this filter can be retrieved.
	 * @param queryProvider from which the query needed by this filter can be retrieved.
	 */
	void setQueryProvider(QueryProvider<? super Q> queryProvider);
	
	/**
	 * The ActiveStateManager that controls the checked state of the button located in the section
	 * I am embedded in.
	 * 
	 * @param sectionButtonManager the ActiveStateManager of the section I am embedded in.
	 */
	void setSectionButtonActiveStateManager(ActiveStateManager sectionButtonManager);
}
