/**
 * 
 */
package org.nightlabs.clientui.ui.layout;

import java.util.List;

import org.nightlabs.clientui.layout.GridData;
import org.nightlabs.clientui.layout.GridLayout;

/**
 * Interface used by {@link GridLayoutConfigComposite} to edit a layout configuration
 * that is build up from a {@link GridLayout} and several {@link GridData} entries.
 * <p>
 * Implement this interface to let {@link GridLayoutConfigComposite} edit your 
 * version of a grid layout configuration that holds entries/links to your configuration entries.
 * </p>
 * <p>
 * Here is a simple example: Let's assume you want to configure a layout that arranges
 * Sections of a FormPage within a grid. You would then implement {@link IGridLayoutConfig}
 * to serve a {@link GridLayout} and implementations of {@link IGridLayoutConfig} that
 * each references one of the Sections (possibly by the Sections id).  
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public interface IGridLayoutConfig {
	/**
	 * Get the {@link GridLayout} of this layout configuration.
	 * @return The {@link GridLayout} of this layout configuration.
	 */
	GridLayout getGridLayout();
	/**
	 * Get all {@link IGridDataEntry}s within this layout configurations.
	 * <p>
	 * Results of this method are not backed and therefore not
	 * affected by calls to {@link #addGridDataEntry()}, {@link #removeGridDataEntry(IGridDataEntry)}
	 * or one of the move methods. After calling these methods {@link #getGridDataEntries()}
	 * should be called again to get the changed set.
	 * </p>
	 * <p>
	 * An {@link IGridDataEntry} basically wraps a {@link GridData}.
	 * </p>
	 * @return All {@link IGridDataEntry}s within this layout configurations.
	 */
	List<IGridDataEntry> getGridDataEntries();
	/**
	 * Remove the given {@link IGridDataEntry} from this layout configuration.
	 * <p> 
	 * After calling this method a new call to {@link #getGridDataEntries()}
	 * should return the modified entry list.
	 * </p>
	 * @param gridDataEntry The entry to remove.
	 */
	void removeGridDataEntry(IGridDataEntry gridDataEntry);
	/**
	 * Add a new {@link IGridDataEntry} to this layout.
	 * The implementing class may query user input for
	 * fulfilling this request.
	 * <p>
	 * The entry should be ready configured with a non <code>null</code>
	 * {@link GridData}.
	 * </p>
	 * <p>
	 * After calling this method a new call to {@link #getGridDataEntries()}
	 * should return the modified entry list.
	 * </p> 
	 * 
	 * @return The newly created {@link IGridDataEntry}.
	 */
	IGridDataEntry addGridDataEntry();
	/**
	 * Move the given entry one place up in the list of entries of this layout config.
	 * <p>
	 * After calling this method a new call to {@link #getGridDataEntries()}
	 * should return the modified entry list.
	 * </p>
	 * @param gridDataEntry The entry to move up.
	 * @return Whether the operation succeeded, i.e. the entry could be moved up.
	 */
	boolean moveEntryDown(IGridDataEntry gridDataEntry);
	/**
	 * Move the given entry one place down in the list of entries of this layout config.
	 * <p>
	 * After calling this method a new call to {@link #getGridDataEntries()}
	 * should return the modified entry list.
	 * </p>
	 * @param gridDataEntry The entry to move down.
	 * @return Whether the operation succeeded, i.e. the entry could be moved down.
	 */
	boolean moveEntryUp(IGridDataEntry gridDataEntry);
}
