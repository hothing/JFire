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
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.jfire.base.ui.prop.ValidationUtil;
import org.nightlabs.jfire.base.ui.prop.edit.IValidationResultHandler;
import org.nightlabs.jfire.base.ui.prop.edit.ValidationResultHandler;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.prop.exception.DataNotFoundException;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * A WizardPage to define values of PropDataFields for a
 * set of PropDataBlocks.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class CompoundDataBlockWizardPage extends WizardHopPage {

	private PropertySet propSet;
	private Map<StructBlockID, DataBlockGroup> dataBlockGroups = new HashMap<StructBlockID, DataBlockGroup>();
	private Map<StructBlockID, DataBlockGroupEditor> dataBlockGroupEditors = new HashMap<StructBlockID, DataBlockGroupEditor>();
	private StructBlockID[] structBlockIDs;
	private int dataBlockEditorColumnHint = 2;
	private IValidationResultHandler validationResultHandler;

	XComposite wrapperComp;
	private ValidationResult lastValidationResult = null;
	
	/**
	 * This variable is used to retain a possible validation error message until the first input by the user was made.
	 */
	protected boolean pristine = false;

//	public CompoundDataBlockWizardPage (
//			String pageName,
//			String title,
//			PropertySet prop,
//			List<StructBlockID> structBlockIDs
//	) {
//		this(pageName, title, prop, structBlockIDs.toArray(new StructBlockID[structBlockIDs.size()]));
//	}

	/**
	 * Creates a new CompoundDataBlockWizardPage for the
	 * StructBlock identified by the dataBlockID
	 */
	public CompoundDataBlockWizardPage (
			String pageName,
			String title,
			PropertySet propSet,
			StructBlockID[] structBlockIDs
	) {
		super(pageName);
		if (title != null)
			this.setTitle(title);
		if (propSet == null)
			throw new IllegalArgumentException("Parameter propertySet must not be null"); //$NON-NLS-1$

		this.propSet = propSet;
		this.structBlockIDs = structBlockIDs;
		for (int i = 0; i < structBlockIDs.length; i++) {
			try {
				dataBlockGroups.put(structBlockIDs[i],propSet.getDataBlockGroup(structBlockIDs[i]));
			} catch (DataNotFoundException e) {
				ExceptionHandlerRegistry.syncHandleException(e);
				throw new RuntimeException(e);
			}
		}

		validationResultHandler = new ValidationResultHandler() {
			@Override
			public void handleValidationResult(ValidationResult validationResult) {
				if (pristine)
					return;
				
				lastValidationResult = validationResult;
				if (validationResult == null)
					setMessage(null);
				else
					setMessage(validationResult.getMessage(), ValidationUtil.getIMessageProviderType(validationResult.getType()));
				if (getContainer().getCurrentPage() != null)
					getContainer().updateButtons();
			}
		};
		
		// Register a listener, that sets pristine to false and then immediately deregisteres itself again
		final DataBlockEditorChangedListener[] listener = new DataBlockEditorChangedListener[1];
		listener[0] = new DataBlockEditorChangedListener() {
			@Override
			public void dataBlockEditorChanged(DataBlockEditorChangedEvent dataBlockEditorChangedEvent) {
				pristine = false;
				removeChangeListener(listener[0]);
			}
		};
		addChangeListener(listener[0]);
	}

	/**
	 * Creates the wrapper Composite.
	 * Has to be called when {@link #createControl(Composite)} is
	 * overridden.
	 *
	 * @param parent
	 */
	protected void createWrapper(Composite parent) {
		wrapperComp = new XComposite(parent, SWT.NONE, XComposite.LayoutMode.TIGHT_WRAPPER);
		setControl(wrapperComp);
	}

	/**
	 * Creates a composite with the AbstractDataBlockEditor according
	 * to the StructBlockID passed to the constructor.
	 */
	protected void createDataBlockEditors() {
		dataBlockGroupEditors.clear();
		for (int i = 0; i < structBlockIDs.length; i++) {
			DataBlockGroup dataBlockGroup = dataBlockGroups.get(structBlockIDs[i]);
			DataBlockGroupEditor editor = new DataBlockGroupEditor(propSet.getStructure(), dataBlockGroup, wrapperComp, validationResultHandler);
			editor.addDataBlockEditorChangedListener(listenerProxy);
			editor.refresh(propSet.getStructure(), dataBlockGroup);
			dataBlockGroupEditors.put(
					structBlockIDs[i],
					editor
			);
		}
	}

	/**
	 * Returns the propertySet passed in the constructor.
	 *
	 * @return
	 */
	public PropertySet getPropertySet() {
		return propSet;
	}

	/**
	 * Returns one of the DataBlockGroupEditors created by
	 * {@link #createPropDataBlockEditors()}, thus null
	 * before a call to this method. Null can be returned
	 * as well when a StructBlockID is passed here
	 * that was not in the List the WizardPage was constructed with.
	 *
	 * @return
	 */
	public DataBlockGroupEditor getDataBlockGroupEditor(StructBlockID structBlockID) {
		return dataBlockGroupEditors.get(structBlockID);
	}

	/**
	 * Returns the propDataBlockGorup within the given
	 * Property this Page is associated with.
	 *
	 * @return
	 */
	public DataBlockGroup getDataBlockGroup(StructBlockID structBlockID) {
		return dataBlockGroups.get(structBlockID);
	}

	/**
	 * Get the hint for the column count of the
	 * AbstractDataBlockEditor. Default is 2.
	 * @return
	 */
	public int getPropDataBlockEditorColumnHint() {
		return dataBlockEditorColumnHint;
	}
	/**
	 * Set the hint for the column count of the
	 * AbstractDataBlockEditor. Default is 2.
	 *
	 * @param propDataBlockEditorColumnHint
	 */
	public void setPropDataBlockEditorColumnHint(
			int propDataBlockEditorColumnHint) {
		this.dataBlockEditorColumnHint = propDataBlockEditorColumnHint;
	}

	/**
	 * Set all values to the propertySet.
	 */
	public void updatePropertySet() {
		for (DataBlockGroupEditor editor : dataBlockGroupEditors.values()) {
			editor.updatePropertySet();
		}
	}

	public void refresh(PropertySet propertySet) {
		for (Map.Entry<StructBlockID, DataBlockGroupEditor> entry : dataBlockGroupEditors.entrySet()) {
			try {
				DataBlockGroup blockGroup = propertySet.getDataBlockGroup(entry.getKey());
				dataBlockGroups.put(entry.getKey(), blockGroup);
				entry.getValue().refresh(propertySet.getStructure(), blockGroup);
			} catch (DataBlockGroupNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * This implementation of createControl
	 * calls {@link #createWrapper(Composite)} and {@link #createPropDataBlockEditors()}.
	 * Subclasses can override and call this method themselves.
	 *
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createPageContents(Composite parent) {
		createWrapper(parent);
		createDataBlockEditors();
		return wrapperComp;
	}

	public XComposite getWrapperComp() {
		return wrapperComp;
	}

	@Override
	public void onHide() {
		super.onHide();
		updatePropertySet();
	}

	@Override
	public void onShow() {
		super.onShow();
		refresh(propSet);
	}

	public void setValidationResultManager(IValidationResultHandler validationResultHandler) {
		this.validationResultHandler = validationResultHandler;
	}

	public IValidationResultHandler getValidationResultManager() {
		return validationResultHandler;
	}
	
	private DataBlockEditorChangedListener listenerProxy = new DataBlockEditorChangedListener() {
		@Override
		public void dataBlockEditorChanged(DataBlockEditorChangedEvent dataBlockEditorChangedEvent) {
			notifyChangeListeners(dataBlockEditorChangedEvent);
		}
	};
	
	private ListenerList listenerList = new ListenerList();
	
	protected synchronized void notifyChangeListeners(DataBlockEditorChangedEvent event) {
		for (Object obj :  listenerList.getListeners())
			((DataBlockEditorChangedListener) obj).dataBlockEditorChanged(event);
	}
	
	public void addChangeListener(DataBlockEditorChangedListener listener) {
		listenerList.add(listener);
	}

	public void removeChangeListener(DataBlockEditorChangedListener listener) {
		listenerList.remove(listener);
	}
	
	@Override
	public boolean isPageComplete() {
		return !pristine && lastValidationResult == null;
	}
	
	public void markPristine() {
		pristine = true;
	}
}
