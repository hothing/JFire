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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.base.ui.prop.edit.IValidationResultHandler;
import org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * A composite that creates a block-based {@link PropertySetEditor} that will cover all
 * blocks not covered in a given {@link EditorStructBlockRegistry}.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class FullDataBlockCoverageComposite extends Composite {

	private int numColumns;
	private EditorStructBlockRegistry structBlockRegistry;
	
	/**
	 * Constructs a new {@link FullDataBlockCoverageComposite}.
	 * 
	 * @param parent The parent Composite for the new instance.
	 * @param style The SWT style for the new instance.
	 * @param propertySet The {@link PropertySet} to edit.
	 * @param structBlockRegistry The registry where the blocks already covered can be obtained from. Might be <code>null</code>.
	 * @param validationResultHandler Handler for validation results.
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public FullDataBlockCoverageComposite(
			Composite parent, int style,
			PropertySet propertySet,
			EditorStructBlockRegistry structBlockRegistry, IValidationResultHandler validationResultHandler
	) {
		super(parent, style);
		this.numColumns = 1;
		if (!(propertySet.getStructure() instanceof StructLocal))
			throw new IllegalArgumentException("The given propertySet was not exploded by a StructLocal"); //$NON-NLS-1$
		this.structBlockRegistry = structBlockRegistry;
		if (structBlockRegistry == null) {
			this.structBlockRegistry = new EditorStructBlockRegistry(propertySet.getStructLocalObjectID());
//					propertySet.getStructure().getOrganisationID(),
//					propertySet.getStructLinkClass(), propertySet.getStructScope(), propertySet.getStructLocalScope());
		}
		StructBlockID[] fullCoverageBlockIDs = this.structBlockRegistry.getUnassignedBlockKeyArray();
		createPropEditors(validationResultHandler);
		List<StructBlockID>[] splitBlockIDs = new List[numColumns];
		for (int i=0; i<numColumns; i++) {
			splitBlockIDs[i] = new ArrayList<StructBlockID>();
		}
		for (int i=0; i<fullCoverageBlockIDs.length; i++){
			splitBlockIDs[i % numColumns].add(fullCoverageBlockIDs[i]);
		}

		GridLayout thisLayout = new GridLayout();
		thisLayout.numColumns = numColumns;
		thisLayout.makeColumnsEqualWidth = true;
		this.setLayout(thisLayout);

		this.setLayoutData(new GridData(GridData.FILL_BOTH));

		for (int i=0; i<numColumns; i++) {
			XComposite wrapper = new XComposite(this,SWT.NONE, XComposite.LayoutMode.TIGHT_WRAPPER);
			BlockBasedEditor propEditor = (BlockBasedEditor)propEditors.get(i);
			propEditor.setPropertySet(propertySet);
//			propEditor.setEditorDomain(editorScope,"#FullDatBlockCoverageComposite"+i);
			propEditor.setEditorStructBlockList(splitBlockIDs[i]);
			Control propEditorControl = propEditor.createControl(wrapper, true);
			GridData editorControlGD = new GridData(GridData.FILL_BOTH);
			propEditorControl.setLayoutData(editorControlGD);
		}
	}

	private List<PropertySetEditor> propEditors = new LinkedList<PropertySetEditor>();

	private void createPropEditors(IValidationResultHandler validationResultHandler) {
		propEditors.clear();
		for (int i=0; i<numColumns; i++) {
			BlockBasedEditor blockBasedEditor = createBlockBasedEditor();
			blockBasedEditor.setValidationResultHandler(validationResultHandler);
			propEditors.add(blockBasedEditor);
			blockBasedEditor.addChangeListener(listenerProxy);
		}
	}
	
	/**
	 * This method is delegated to in order to create the {@link BlockBasedEditor}
	 * that will show all {@link StructBlock}s to fulfill full coverage.
	 * @return A new {@link BlockBasedEditor}.
	 */
	protected BlockBasedEditor createBlockBasedEditor() {
		return new BlockBasedEditor(true);
	}

	/**
	 * Set the values from the editor to the PropertySet it
	 * is associated with.
	 */
	public void updatePropertySet() {
		for (PropertySetEditor editor : propEditors) {
			editor.updatePropertySet();
		}
	}

	/**
	 * Link the Composite to a PropertySet and refresh the Control.
	 *
	 * @param propertySet The PropertySet to link to.
	 */
	public void refresh(PropertySet propertySet) {
		for (PropertySetEditor editor : propEditors) {
			editor.setPropertySet(propertySet, true);
		}
	}

	private ListenerList listenerList = new ListenerList();

	private DataBlockEditorChangedListener listenerProxy = new DataBlockEditorChangedListener() {
		@Override
		public void dataBlockEditorChanged(DataBlockEditorChangedEvent dataBlockEditorChangedEvent) {
			notifyChangeListeners(dataBlockEditorChangedEvent);
		}
	};

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
	
	public void validate() {
		for (PropertySetEditor propEditor : propEditors) {
			propEditor.validate();
		}
	}
}
