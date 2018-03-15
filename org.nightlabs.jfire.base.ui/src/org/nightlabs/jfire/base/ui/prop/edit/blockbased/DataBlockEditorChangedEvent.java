/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.prop.DataField;

/**
 * Event object used to notify {@link DataBlockEditorChangedListener}s of a changed
 * {@link DataBlockEditor} and its changed {@link DataFieldEditor}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class DataBlockEditorChangedEvent {

	private DataBlockEditor dataBlockEditor;
	private DataFieldEditor<? extends DataField> dataFieldEditor;
	
	/**
	 * Create a new {@link DataBlockEditorChangedEvent}.
	 */
	public DataBlockEditorChangedEvent(DataBlockEditor dataBlockEditor, DataFieldEditor<? extends DataField> dataFieldEditor) {
		this.dataBlockEditor = dataBlockEditor;
		this.dataFieldEditor = dataFieldEditor;
	}
	/**
	 * @return The changed {@link DataBlockEditor}.
	 */
	public DataBlockEditor getDataBlockEditor() {
		return dataBlockEditor;
	}
	/**
	 * @return The changed {@link DataFieldEditor}.
	 */
	public DataFieldEditor<? extends DataField> getDataFieldEditor() {
		return dataFieldEditor;
	}
}
