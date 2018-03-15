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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.datafield.I18nTextDataField;
import org.nightlabs.jfire.prop.datafield.TextDataField;

/**
 * Represents an editor for {@link TextDataField} within a
 * block based ExpandableBlocksEditor.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class I18nTextDataFieldEditor extends AbstractDataFieldEditor<I18nTextDataField> {

	public I18nTextDataFieldEditor(IStruct struct, I18nTextDataField data) {
		super(struct, data);
	}

	public static class Factory extends AbstractDataFieldEditorFactory<I18nTextDataField> {
		@Override
		public Class<I18nTextDataField> getPropDataFieldType() {
			return I18nTextDataField.class;
		}

		@Override
		public String[] getEditorTypes() {
			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
		}
//		/**
//		 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory#getDataFieldEditorClass()
//		 */
//		@Override
//		public Class<I18nTextDataFieldEditor> getDataFieldEditorClass() {
//			return I18nTextDataFieldEditor.class;
//		}
		@Override
		public DataFieldEditor<I18nTextDataField> createPropDataFieldEditor(IStruct struct, I18nTextDataField data) {
			return new I18nTextDataFieldEditor(struct, data);
		}
	}

// This seems not to be used (and the method in the javadoc doesn't exist). Commented it out. Marco.
//	/**
//	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#getEditorType()
//	 */
//	public String getEditorType() {
//		return ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE;
//	}

	private I18nTextDataFieldComposite composite;

	@Override
	public Control createControl(Composite parent) {
		if (composite == null) {
			composite = new I18nTextDataFieldComposite(this, parent, SWT.NONE, getSwtModifyListener());
		}
//		composite.refresh(); // I think the framework calls refresh() anyway, hence it's not necessary to call this method here. Marco.
		return composite;
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void updatePropertySet()
	{
//		Display.getDefault().syncExec(new Runnable(){
//			public void run() {
				composite.updateFieldText(getDataField().getI18nText());
//			}
//		});
	}

// This seems not to be used (and the method in the javadoc doesn't exist). Commented it out. Marco.
//	/**
//	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#disposeControl()
//	 */
//	public void disposeControl() {
//		composite.dispose();
//	}

	@Override
	public void doRefresh() {
		if (composite != null)
			composite.refresh();
	}

}
