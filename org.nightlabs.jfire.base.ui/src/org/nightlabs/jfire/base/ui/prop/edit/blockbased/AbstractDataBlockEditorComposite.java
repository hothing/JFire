/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedEvent;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedListener;
import org.nightlabs.jfire.base.ui.prop.edit.IValidationResultHandler;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * Base class for implementations of {@link IDataBlockEditorComposite}.
 * It has no abstract methods, but is still abstract as it is intended
 * to be subclassed and configured/filled by creating {@link DataFieldEditor}s
 * for the {@link StructField}s of the edited block.
 * <p>
 * Implementations can rely on the data already being set for the {@link DataBlockEditor}
 * that the Composite is created with, so it is usually a good practice to create
 * the {@link DataFieldEditor}s in the constructor. All {@link StructField}s of the 
 * edited {@link DataBlock} can be obtained using {@link #getOrderedStructFields()}.
 * </p>
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractDataBlockEditorComposite extends Composite implements IDataBlockEditorComposite {

	private DataBlockEditor dataBlockEditor;
	private IStruct struct;
	private DataBlock dataBlock;
	
	/**
	 * Create a new {@link AbstractBlockBasedEditor}. Use this super constructor from the subclass.
	 * <p>
	 * Note that this class relies on the {@link DataBlock} already has been set to the given
	 * {@link DataBlockEditor}.
	 * </p>
	 * 
	 * @param dataBlockEditor The {@link DataBlockEditor} this is created for.
	 * @param parent The parent {@link Composite} to use.
	 * @param style The style to apply to the new editor composite.
	 */
	protected AbstractDataBlockEditorComposite(DataBlockEditor dataBlockEditor, Composite parent, int style) {
		super(parent,style);
		this.dataBlockEditor = dataBlockEditor;
		this.struct = dataBlockEditor.getStruct();
		this.dataBlock = dataBlockEditor.getDataBlock();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Implemented by iterating the {@link DataFieldEditor}s that were added by {@link #addFieldEditor(DataField, DataFieldEditor)}
	 * and calling their {@link DataFieldEditor#setData(IStruct, DataField)} 
	 * </p>
	 */
	@Override
	public final void refresh(IStruct struct, DataBlock dataBlock) {
		this.struct = struct;
		this.dataBlock = dataBlock;
		for (DataFieldEditor<DataField> fieldEditor : getFieldEditors().values()) {
			try {
				fieldEditor.setData(struct, dataBlock.getDataField(fieldEditor.getStructField().getStructFieldIDObj()));
				fieldEditor.refresh();
			} catch (DataFieldNotFoundException e) {
				throw new RuntimeException("Could not find correct DataField: ", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Added to all {@link DataFieldEditor}s added with {@link #addFieldEditor(DataField, DataFieldEditor)}.
	 */
	private DataFieldEditorChangedListener fieldEditorChangeListener = new DataFieldEditorChangedListener() {
		@Override
		public void dataFieldEditorChanged(DataFieldEditorChangedEvent changedEvent) {
			notifyChangeListeners(changedEvent.getDataFieldEditor());

			List<ValidationResult> validationResults = getDataBlock().validate(getStruct());
			if (getValidationResultManager() != null)
				getValidationResultManager().handleValidationResults(validationResults);
		}
	};
	
	/**
	 * key: StructField the StructField the {@link DataFieldEditor} is for.
	 * value: DataFieldEditor fieldEditor
	 */
	private Map<StructFieldID, DataFieldEditor<DataField>> fieldEditors = new HashMap<StructFieldID, DataFieldEditor<DataField>>();

	/**
	 * Adds the given {@link DataFieldEditor} for the given {@link StructField} to the editors
	 * known to this {@link Composite}. The {@link DataFieldEditor}s added here will be used
	 * to implement the {@link #refresh(IStruct, DataBlock)} and {@link #updatePropertySet()}
	 * methods of the {@link IDataBlockEditorComposite} interface.
	 * 
	 * @param structFieldID The id of the {@link StructField} the given {@link DataFieldEditor} is for.
	 * @param fieldEditor The {@link DataFieldEditor} to add.
	 */
	protected void addFieldEditor(StructFieldID structFieldID, DataFieldEditor<DataField> fieldEditor) {
		addFieldEditor(structFieldID, fieldEditor, true);
	}

	/**
	 * Adds the given {@link DataFieldEditor} for the given {@link DataField} to the editors
	 * known to this {@link Composite}. The {@link DataFieldEditor}s added here will be used
	 * to implement the {@link #refresh(IStruct, DataBlock)} and {@link #updatePropertySet()}
	 * methods of the {@link IDataBlockEditorComposite} interface.
	 * 
	 * @param structFieldID The id of the {@link StructField} the given {@link DataFieldEditor} is for.
	 * @param fieldEditor The {@link DataFieldEditor} to add.
	 * @param addListener Whether to add a {@link DataFieldEditorChangedListener} to the given
	 *                    {@link DataFieldEditor} that will cause all changes in the {@link DataFieldEditor}
	 *                    to be notified to listeners to this Composite.
	 */
	protected void addFieldEditor(StructFieldID structFieldID, DataFieldEditor<DataField> fieldEditor, boolean addListener) {
		fieldEditors.put(structFieldID, fieldEditor);
		if (addListener)
			fieldEditor.addDataFieldEditorChangedListener(fieldEditorChangeListener);
	}

	/**
	 * Returns all {@link DataFieldEditor}s registered to this Composite.
	 * If none were registered yet they will be created using {@link #createFieldEditors()}.
	 * 
	 * @return All {@link DataFieldEditor}s registered to this Composite.
	 */
	protected Map<StructFieldID, DataFieldEditor<DataField>> getFieldEditors() {
		if (fieldEditors.size() == 0) {
//			createFieldEditors();
		}
		return fieldEditors;
	}
	
	/**
	 * Get the {@link DataFieldEditor} that was registered with {@link #addFieldEditor(DataField, DataFieldEditor)}
	 * to be editing the given {@link DataField}.
	 * 
	 * @param dataField The {@link DataField} to search the {@link DataFieldEditor} for.
	 * @return The {@link DataFieldEditor} editing the given {@link DataField} or 
	 *         <code>null</code> if none could be found.
	 */
	protected DataFieldEditor<DataField> getFieldEditor(DataField dataField) {
		return getFieldEditors().get(dataField.getPropRelativePK());
	}
	
	/**
	 * Checks whether a {@link DataFieldEditor} was registerd with {@link #addFieldEditor(DataField, DataFieldEditor)}
	 * to be editing data for the the given {@link StructField}.
	 * 
	 * @param structFieldID The id of the {@link StructField} to search the {@link DataFieldEditor} for.
	 * @return Whether a {@link DataFieldEditor} was registered for the given {@link StrcutField}.
	 */
	protected boolean hasFieldEditorFor(StructFieldID structFieldID) {
		return getFieldEditors().containsKey(structFieldID);
	}

	private ListenerList fieldEditorChangeListeners = new ListenerList();

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.IDataBlockEditorComposite#addDataFieldEditorChangeListener(org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedListener)
	 */
	@Override
	public void addDataFieldEditorChangeListener(DataFieldEditorChangedListener listener) {
		fieldEditorChangeListeners.add(listener);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.IDataBlockEditorComposite#removeDataFieldEditorChangeListener(org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedListener)
	 */
	@Override
	public void removeDataFieldEditorChangeListener(DataFieldEditorChangedListener listener) {
		fieldEditorChangeListeners.remove(listener);
	}
	
	/**
	 * Notifies the listener to this {@link Composite} of a change in the given {@link DataFieldEditor}.
	 * @param dataFieldEditor The {@link DataFieldEditor} whose data changed.
	 */
	protected synchronized void notifyChangeListeners(DataFieldEditor<? extends DataField> dataFieldEditor) {
		Object[] listeners = fieldEditorChangeListeners.getListeners();
		DataFieldEditorChangedEvent evt = new DataFieldEditorChangedEvent(dataFieldEditor);
		for (Object listener : listeners) {
			if (listener instanceof DataFieldEditorChangedListener)
				((DataFieldEditorChangedListener) listener).dataFieldEditorChanged(evt);
		}
	}

	
	public List<StructField<? extends DataField>> getOrderedStructFields() {
		// TODO re-enable this
		//return AbstractPropStructOrderConfigModule.sharedInstance().structFieldDisplayOrder();
		List<StructField<? extends DataField>> fields = getStruct().getStructBlock(getDataBlock().getDataBlockGroup()).getStructFields();
		return fields;
	}

	protected IStruct getStruct() {
		return struct;
	}

	@Override
	public void dispose() {
		for (DataFieldEditor<? extends DataField> editor : fieldEditors.values()) {
			editor.removeDataFieldEditorChangedListener(fieldEditorChangeListener);
		}
		fieldEditors.clear();
		super.dispose();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Implemented by iterating the {@link DataFieldEditor}s registered with {@link #addFieldEditor(DataField, DataFieldEditor)}
	 * and calling their {@link DataFieldEditor#updatePropertySet()} method.
	 * </p>
	 */
	public void updatePropertySet() {
		for (DataFieldEditor<? extends DataField> editor : getFieldEditors().values()) {
			editor.updatePropertySet();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditor#getDataBlock()
	 */
	public DataBlock getDataBlock() {
		return dataBlock;
	}
	
	public DataBlockEditor getDataBlockEditor() {
		return dataBlockEditor;
	}

	private IValidationResultHandler validationResultHandler;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditor#setValidationResultManager(org.nightlabs.jfire.base.ui.prop.edit.blockbased.IValidationResultManager)
	 */
	public void setValidationResultHandler(IValidationResultHandler validationResultHandler) {
		this.validationResultHandler = validationResultHandler;
	}

	public IValidationResultHandler getValidationResultManager() {
		return validationResultHandler;
	}
}