package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.datafield.MultiSelectionDataField;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de --> (Original SelectionDataFieldEditor code)
 */
public class MultiSelectionDataFieldEditor extends AbstractDataFieldEditor<MultiSelectionDataField> {

	public MultiSelectionDataFieldEditor(IStruct struct, MultiSelectionDataField data) {
		super(struct, data);
	}

	public static class Factory extends AbstractDataFieldEditorFactory<MultiSelectionDataField> {

		@Override
		public String[] getEditorTypes() {
			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
		}

//		@Override
//		public Class<? extends DataFieldEditor<SelectionDataField>> getDataFieldEditorClass() {
//			return SelectionDataFieldEditor.class;
//		}
	
		@Override
		public DataFieldEditor<MultiSelectionDataField> createPropDataFieldEditor(IStruct struct, MultiSelectionDataField data) {
			return new MultiSelectionDataFieldEditor(struct, data);
		}

		@Override
		public Class<MultiSelectionDataField> getPropDataFieldType() {
			return MultiSelectionDataField.class;
		}

	};

	private MultiSelectionDataFieldComposite composite;

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(Composite parent) {
		composite = new MultiSelectionDataFieldComposite(this, parent, SWT.NONE, getModifyListener());
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
	public void updatePropertySet() 
	{
		getDataField().setSelection(composite.getSelection());
	}
}


