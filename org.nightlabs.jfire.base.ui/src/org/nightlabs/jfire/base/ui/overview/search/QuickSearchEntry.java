package org.nightlabs.jfire.base.ui.overview.search;

import org.nightlabs.base.ui.message.MessageType;
import org.nightlabs.datastructure.Pair;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryProvider;

/**
 * Interface which defines an QuickSearch which can be used
 * e.g. in the Overview to perform a quick search with only a search text
 * in a special context defined by this QuickSearchEntry
 *
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public interface QuickSearchEntry<Q extends AbstractSearchQuery>
{
	/**
	 * Validate the given search value for correctness and in case something is not right returns the error type as well
	 * as the error message.
	 *
	 * @param searchValue The search input to validate.
	 * @return In case something is not right returns the error type as well as the error message.
	 */
	Pair<MessageType, String> validateSearchCondionValue(String searchValue);

	/**
	 * Sets the search text
	 * @param searchText the search text to set
	 */
	void setSearchConditionValue(String searchText);

	/**
	 * Implementors have to unset the Query value that was set by this entry.
	 *
	 * @param query the query on which the represented query aspect has to be unset.
	 */
	void unsetSearchCondition();

	/**
	 * Sets the QueryProvider for the implementing Entry.
	 *
	 * @param queryProvider
	 * 					The QueryProvider from which the Query to set the search condition will be fetched.
	 */
	void setQueryProvider(QueryProvider<? super Q> queryProvider);

	/**
	 * Return the factory which created this instance
	 *
	 * @return the factory which created this instance
	 */
	QuickSearchEntryFactory<Q> getFactory();
}
