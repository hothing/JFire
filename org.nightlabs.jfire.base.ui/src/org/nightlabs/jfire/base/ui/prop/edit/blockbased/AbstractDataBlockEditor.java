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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedEvent;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedListener;
import org.nightlabs.jfire.base.ui.prop.edit.IValidationResultHandler;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * Base class for implementations of {@link DataBlockEditor} that works with a {@link Composite}
 * that implements the {@link IDataBlockEditorComposite} interface.
 * With a composite implementing this interface the implementation of an editor needs only to
 * create this {@link Composite} in {@link #createEditorComposite(Composite)}.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractDataBlockEditor implements DataBlockEditor {

	private static final Logger logger = Logger.getLogger(AbstractDataBlockEditor.class);
	
	private IStruct struct;
	protected DataBlock dataBlock;
	private ListenerList dataBlockEditorListeners = new ListenerList();
	private IValidationResultHandler validationResultHandler;
	private IDataBlockEditorComposite dataBlockEditorComposite;

	/**
	 * Create a new {@link AbstractBlockBasedEditor}.
	 */
	protected AbstractDataBlockEditor() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditor#addDataBlockEditorChangedListener(org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditorChangedListener)
	 */
	public synchronized void addDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		dataBlockEditorListeners.add(listener);
	}
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditor#removeDataBlockEditorChangedListener(org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditorChangedListener)
	 */
	public synchronized void removeDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		dataBlockEditorListeners.add(listener);
	}
	/**
	 * Call this method to notify listeners to this editor of a change.
	 * 
	 * @param dataFieldEditor The {@link DataFieldEditor} whose data changed.
	 */
	protected synchronized void notifyChangeListeners(DataFieldEditor<? extends DataField> dataFieldEditor) {
		Object[] listeners = dataBlockEditorListeners.getListeners();
		DataBlockEditorChangedEvent changedEvent = new DataBlockEditorChangedEvent(this, dataFieldEditor);
		for (Object listener : listeners) {
			if (listener instanceof DataBlockEditorChangedListener)
				((DataBlockEditorChangedListener) listener).dataBlockEditorChanged(changedEvent);
		}
	}
	
	private DataFieldEditorChangedListener fieldEditorChangeListener = new DataFieldEditorChangedListener() {
		@Override
		public void dataFieldEditorChanged(DataFieldEditorChangedEvent changedEvent) {
			notifyChangeListeners(changedEvent.getDataFieldEditor());
			
			validateDataBlock();
		}
	};	

	/**
	 * Default implementation of updateProp() iterates through all
	 * DataFieldEditor s added by {@link #addFieldEditor(DataField, DataFieldEditor)}
	 * and calls their updateProp method.<br/>
	 * Implementors might override if no registered PropDataFieldEditors are used.
	 */
	@Override
	public void updatePropertySet() {
		getDataBlockEditorComposite().updatePropertySet();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditor#getDataBlock()
	 */
	@Override
	public DataBlock getDataBlock() {
		return dataBlock;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditor#getStruct()
	 */
	@Override
	public IStruct getStruct() {
		return struct;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditor#setValidationResultManager(org.nightlabs.jfire.base.ui.prop.edit.blockbased.IValidationResultManager)
	 */
	@Override
	public void setValidationResultManager(IValidationResultHandler validationResultHandler) {
		this.validationResultHandler = validationResultHandler;
		getDataBlockEditorComposite().setValidationResultHandler(validationResultHandler);
		validateDataBlock();
	}
	
	/**
	 * @return The {@link IValidationResultHandler} set with {@link #setValidationResultManager(IValidationResultHandler)}.
	 */
	public IValidationResultHandler getValidationResultManager() {
		return validationResultHandler;
	}
	
	/**
	 * Validates the data in the {@link DataBlock} associated to this editor.
	 */
	protected void validateDataBlock() {
		if (getDataBlock() != null) {
			long start = System.currentTimeMillis();
			List<ValidationResult> validationResults = getDataBlock().validate(getStruct());
			long duration = System.currentTimeMillis() - start;			
			if (getValidationResultManager() != null) {
				getValidationResultManager().handleValidationResults(validationResults);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Validation of of datablock "+getDataBlock()+" took "+duration+" ms!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} else {
			logger.warn(this.getClass().getName() + ".validateDataBlock() called before setData()");  //$NON-NLS-1$	
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditor#setData(org.nightlabs.jfire.prop.IStruct, org.nightlabs.jfire.prop.DataBlock)
	 */
	@Override
	public void setData(IStruct struct, DataBlock dataBlock) {
		this.struct = struct;
		this.dataBlock = dataBlock;
		if (dataBlockEditorComposite != null)
			dataBlockEditorComposite.refresh(struct, dataBlock);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation will create and return an implemenation
	 * of {@link IDataBlockEditorComposite} {@link #createEditorComposite(Composite)}.
	 * </p>
	 */
	@Override
	public Control createControl(Composite parent) {
		if (dataBlockEditorComposite != null)
			throw new IllegalStateException("The control for this DataBlockEditor was already created"); //$NON-NLS-1$
		dataBlockEditorComposite = createEditorComposite(parent);
		dataBlockEditorComposite.addDataFieldEditorChangeListener(fieldEditorChangeListener);
		dataBlockEditorComposite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent evt) {
				dataBlockEditorComposite.removeDataFieldEditorChangeListener(fieldEditorChangeListener);
			}
		});
		if (!(dataBlockEditorComposite instanceof Control))
			throw new IllegalStateException(this.getClass() + " is not implemented correctly, it did not return a " + Control.class.getName() + " in createEditorComposite()"); //$NON-NLS-1$ //$NON-NLS-2$
		
		return (Control) dataBlockEditorComposite;
	}
	
	private IDataBlockEditorComposite getDataBlockEditorComposite() {
		if (dataBlockEditorComposite == null)
			throw new IllegalStateException("The control of this DataBlockEditor was not created yet, however this implementation relies on it in order to function"); //$NON-NLS-1$
		return dataBlockEditorComposite;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditor#getControl()
	 */
	@Override
	public Control getControl() {
		return (Control) getDataBlockEditorComposite();
	}

	/**
	 * Create a {@link Composite} that implements {@link IDataBlockEditorComposite}.
	 * This will be used by this class to implement the {@link DataBlockEditor} interface.
	 * 
	 * @param parent The parent to use.
	 * @return The newly created {@link IDataBlockEditorComposite}.
	 */
	protected abstract IDataBlockEditorComposite createEditorComposite(Composite parent);
}