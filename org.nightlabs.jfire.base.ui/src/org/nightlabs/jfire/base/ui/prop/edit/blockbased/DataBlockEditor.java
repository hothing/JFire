package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.prop.edit.IValidationResultHandler;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * Implementations of this interface are used to display the {@link DataField}s
 * of a {@link DataBlock} stored in a {@link PropertySet}.
 * <p>
 * Factories creating instances of this interface are registered
 * per {@link StructBlockID} using the extension-point <code>org.nightlabs.jfire.base.ui.specialisedDataBlockEditor</code>.
 * Note that if a {@link DataBlock} should be displayed/edited for 
 * which no {@link DataBlockEditorFactory} was registered, the 
 * framework will automatically use the {@link GenericDataBlockEditor}.
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface DataBlockEditor {

	/**
	 * Create the {@link Control} of this {@link DataBlockEditor}.
	 * <p>
	 * Implementations can rely on that {@link #setData(IStruct, DataBlock)}
	 * was called at least once before this method gets called.
	 * </p>
	 * 
	 * @param parent The parent {@link Composite} for the new {@link Control}.
	 * @return A new {@link Control} with the UI of this {@link DataBlockEditor}.
	 */
	public Control createControl(Composite parent);
	
	/**
	 * @return The {@link Control} created by {@link #createControl(Composite)}.
	 */
	public Control getControl();
	
	/**
	 * Set the {@link DataBlock} for this editor.
	 * <p>
	 * Note, that if the UI of this editor was already created with {@link #createControl(Composite)},
	 * this method should reflect the new data in the UI immediately.
	 * </p>
	 * <p>
	 * Also not that any framework using implementations of this interface should ensure 
	 * that this method was called once before {@link #createControl(Composite)} gets called.
	 * </p>
	 *  
	 * @param struct The {@link IStruct} the {@link DataBlock} was build from. 
	 * @param block The {@link DataBlock} to edit.
	 */
	void setData(IStruct struct, DataBlock block);
	
	/**
	 * @return The {@link IStruct} set with {@link #setData(IStruct, DataBlock)}.
	 */
	IStruct getStruct();
	/**
	 * @return The {@link DataBlock} set with {@link #setData(IStruct, DataBlock)}.
	 */
	DataBlock getDataBlock();
	
	/**
	 * When this method is called a {@link DataBlockEditor} should reflect
	 * the changes made in the UI to the {@link DataBlock} set with {@link #setData(IStruct, DataBlock)}.
	 */
	void updatePropertySet();

	/**
	 * Add a {@link DataBlockEditorChangedListener} to this editor.
	 * It should be notified of any change in the editors UI.
	 * 
	 * @param listener The listener to add. 
	 */
	void addDataBlockEditorChangedListener(DataBlockEditorChangedListener listener);
	/**
	 * Remove the given {@link DataBlockEditorChangedListener}.
	 * 
	 * @param listener The listener to remove. 
	 */
	void removeDataBlockEditorChangedListener(DataBlockEditorChangedListener listener);	
	
	void setValidationResultManager(IValidationResultHandler validationResultHandler);
}