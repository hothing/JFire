package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.swt.events.DisposeListener;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedListener;
import org.nightlabs.jfire.base.ui.prop.edit.IValidationResultHandler;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;

/**
 * This interface is used by {@link AbstractDataBlockEditor} to implement 
 * the {@link DataBlockEditor} interface. It should show the UI to edit
 * the {@link DataField}s of a {@link DataBlock}.
 * <p>
 * This interface is intended to be implemented by subclassing 
 * {@link AbstractDataBlockEditorComposite} rather than directly.
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de --> 
 */
public interface IDataBlockEditorComposite {

	/**
	 * Refresh the UI with the data of the given {@link DataBlock}.
	 * 
	 * @param struct The {@link IStruct} the given {@link DataBlock} was built from.
	 * @param dataBlock The {@link DataBlock} to show.
	 */
	void refresh(IStruct struct, DataBlock dataBlock);
	/**
	 * Here the changes made in the UI should be reflected to the {@link DataBlock}
	 * set with {@link #refresh(IStruct, DataBlock)}.
	 */
	void updatePropertySet();

	/**
	 * 
	 * @param validationResultHandler The {@link IValidationResultHandler} to set.
	 */
	void setValidationResultHandler(IValidationResultHandler validationResultHandler);

	/**
	 * Add the given listener that will be notified of changes in the UI.
	 * @param listener The listener to add.
	 */
	void addDataFieldEditorChangeListener(DataFieldEditorChangedListener listener);
	/**
	 * Remove the given listener.
	 * @param listener The listener to remove.
	 */
	void removeDataFieldEditorChangeListener(DataFieldEditorChangedListener listener);
	/**
	 * Add a {@link DisposeListener} to this {@link IDataBlockEditorComposite}.
	 * 
	 * @param listener The listener to add.
	 */
	void addDisposeListener(DisposeListener listener);
}