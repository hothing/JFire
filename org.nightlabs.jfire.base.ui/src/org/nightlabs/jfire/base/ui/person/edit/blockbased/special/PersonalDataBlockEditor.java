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

package org.nightlabs.jfire.base.ui.person.edit.blockbased.special;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorFactoryRegistry;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorNotFoundException;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditor;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditorComposite;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditorFactory;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditor;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.ExpandableBlocksEditor;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.IDataBlockEditorComposite;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonalDataBlockEditor extends AbstractDataBlockEditor {

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(PersonalDataBlockEditor.class);

	public static class Factory extends AbstractDataBlockEditorFactory {
		/**
		 * @see org.nightlabs.jfire.base.ui.person.edit.blockbased.PersonDataBlockEditorFactory#getProviderStructBlockID()
		 */
		public StructBlockID getProviderStructBlockID() {
			return PersonStruct.PERSONALDATA;
		}

		@Override
		public DataBlockEditor createDataBlockEditor(IStruct struct, DataBlock dataBlock) {
			return new PersonalDataBlockEditor(struct, dataBlock);
		}
	}
	
	private static class PersonalDataBlockEditorComposite extends AbstractDataBlockEditorComposite {

		public PersonalDataBlockEditorComposite(DataBlockEditor blockEditor, Composite parent, int style) {
			super(blockEditor, parent, style);
			try {
				setLayoutData(new GridData(GridData.FILL_BOTH));
				GridLayout thisLayout = new GridLayout();
				thisLayout.horizontalSpacing = 2;
				thisLayout.numColumns = 3;
				thisLayout.verticalSpacing = 2;
				thisLayout.makeColumnsEqualWidth = true;
				thisLayout.marginWidth = 0;
				thisLayout.marginHeight = 0;
				this.setLayout(thisLayout);

				createFieldEditors();

				this.layout();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void createFieldEditors() {
			addDataFieldEditor(PersonStruct.PERSONALDATA_NAME,3);
			addDataFieldEditor(PersonStruct.PERSONALDATA_FIRSTNAME,3);
			addDataFieldEditor(PersonStruct.PERSONALDATA_COMPANY,3);
			addDataFieldEditor(PersonStruct.PERSONALDATA_SALUTATION,1);
			addDataFieldEditor(PersonStruct.PERSONALDATA_TITLE,1);
			addDataFieldEditor(PersonStruct.PERSONALDATA_DATEOFBIRTH,1);
			addDataFieldEditor(PersonStruct.PERSONALDATA_PHOTO,3);
		}

		private void addDataFieldEditor(StructFieldID structFieldID, int horizontalSpan)
		{
			DataField dataField = null;
			try {
				dataField = getDataBlock().getDataField(structFieldID);
			} catch (DataFieldNotFoundException e) {
				logger.error("addDataFieldEditor(StructFieldID fieldID) DataField not found for fieldID continuing: "+structFieldID.toString(),e); //$NON-NLS-1$
			}
			DataFieldEditor<DataField> editor = null;
			if (!hasFieldEditorFor(structFieldID)) {
				try {
					editor = DataFieldEditorFactoryRegistry.sharedInstance().getNewEditorInstance(
							getStruct(), ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE,
							"", // TODO: Context ?!? //$NON-NLS-1$
							dataField
					);
				} catch (DataFieldEditorNotFoundException e1) {
					logger.error("addPersonalDataFieldEditor(PersonStructFieldID fieldID) PersonDataFieldEditor not found for fieldID continuing: "+structFieldID.toString(),e1); //$NON-NLS-1$
				}
				Control editorControl = editor.createControl(this);
				GridData editorLData = new GridData();
				editorLData.horizontalSpan = horizontalSpan;
				editorLData.grabExcessHorizontalSpace = true;
				editorLData.horizontalAlignment = GridData.FILL;
				editorControl.setLayoutData(editorLData);
				addFieldEditor(structFieldID, editor);
			}
		}
	}
	
	protected PersonalDataBlockEditor(IStruct struct, DataBlock dataBlock) {
		super();
	}

	@Override
	protected IDataBlockEditorComposite createEditorComposite(Composite parent) {
		return new PersonalDataBlockEditorComposite(this, parent, SWT.NONE);
	}	
}
