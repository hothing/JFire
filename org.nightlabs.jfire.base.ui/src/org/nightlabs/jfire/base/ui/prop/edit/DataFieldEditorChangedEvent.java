package org.nightlabs.jfire.base.ui.prop.edit;

import org.nightlabs.jfire.prop.DataField;

/**
 * Event used to notify {@link DataFieldEditorChangedListener}s.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class DataFieldEditorChangedEvent {

	private DataFieldEditor<? extends DataField> dataFieldEditor;

	/**
	 * Create a new {@link DataFieldEditorChangedEvent}.
	 * @param dataFieldEditor The changed {@link DataFieldEditor}.
	 */
	public DataFieldEditorChangedEvent(DataFieldEditor<? extends DataField> dataFieldEditor) {
		this.dataFieldEditor = dataFieldEditor;
	}
	
	/**
	 * @return The changed {@link DataFieldEditor}.
	 */
	public DataFieldEditor<? extends DataField> getDataFieldEditor() {
		return dataFieldEditor;
	}

}
