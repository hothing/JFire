/**
 * 
 */
package org.nightlabs.jdo.query.ui.search;

import org.eclipse.swt.events.ModifyListener;

/**
 * {@link SearchFilterProvider} that accepts a
 * String to do an early search with the user
 * being required to trigger it.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public interface EarlySearchFilterProvider extends SearchFilterProvider {

	void setEarlySearchText(String earlySearchText);
	
	/**
	 * Returns the current search string. 
	 * @return the current search string
	 */
	public String getSearchText();
	
	public void addSearchTextModifyListener(ModifyListener listener);
	
	public void removeSearchTextModifyListener(ModifyListener listener);
}
