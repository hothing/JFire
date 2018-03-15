/**
 *
 */
package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.datafield.SelectionDataField;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class SelectionDataFieldEditor extends AbstractDataFieldEditor<SelectionDataField> {

	public SelectionDataFieldEditor(IStruct struct, SelectionDataField data) {
		super(struct, data);
	}

	public static class Factory extends AbstractDataFieldEditorFactory<SelectionDataField> {

		@Override
		public String[] getEditorTypes() {
			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
		}

//		@Override
//		public Class<? extends DataFieldEditor<SelectionDataField>> getDataFieldEditorClass() {
//			return SelectionDataFieldEditor.class;
//		}
	
		@Override
		public DataFieldEditor<SelectionDataField> createPropDataFieldEditor(IStruct struct, SelectionDataField data) {
			return new SelectionDataFieldEditor(struct, data);
		}

		@Override
		public Class<SelectionDataField> getPropDataFieldType() {
			return SelectionDataField.class;
		}

	};

	private SelectionDataFieldComposite composite;

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(Composite parent) {
		composite = new SelectionDataFieldComposite(this, parent, SWT.NONE, getModifyListener());
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor#doRefresh()
	 */
	@Override
	public void doRefresh() {
		if (composite != null)
			composite.refresh();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#getControl()
	 */
	public Control getControl() {
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#updateProp()
	 */
	public void updatePropertySet() {
		getDataField().setSelection(composite.getFieldValueCombo().getSelectedElement());
	}
}


