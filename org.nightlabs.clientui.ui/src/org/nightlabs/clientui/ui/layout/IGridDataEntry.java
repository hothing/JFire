/**
 * 
 */
package org.nightlabs.clientui.ui.layout;

import org.nightlabs.clientui.layout.GridData;
import org.nightlabs.i18n.I18nText;

/**
 * {@link IGridDataEntry} basically wraps a {@link GridData} for a 
 * cell within {@link IGridLayoutConfig}. It additionally has a
 * name property to be able to display the cell in the edit ui.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public interface IGridDataEntry {

	/**
	 * Get the name of this entry (cell within a {@link IGridLayoutConfig}).
	 * 
	 * @return The name of this entry (cell within a {@link IGridLayoutConfig}).
	 */
	I18nText getName();
	
	/**
	 * Get the {@link GridData} for this entry. 
	 * @return The {@link GridData} for this entry.
	 */
	GridData getGridData();
}
