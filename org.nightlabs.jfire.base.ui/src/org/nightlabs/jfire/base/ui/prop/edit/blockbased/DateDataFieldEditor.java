package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.composite.DateTimeControl;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractInlineDataFieldComposite;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.datafield.DateDataField;
import org.nightlabs.jfire.prop.structfield.DateStructField;

/**
 * A data field editor for dates.
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DateDataFieldEditor extends AbstractDataFieldEditor<DateDataField> {
	
	public DateDataFieldEditor(IStruct struct, DateDataField data) {
		super(struct, data);
	}

	public static class Factory extends AbstractDataFieldEditorFactory<DateDataField> {

		/* (non-Javadoc)
		 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory#getEditorTypes()
		 */
		@Override
		public String[] getEditorTypes() {
			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
		}

		@Override
		public DataFieldEditor<DateDataField> createPropDataFieldEditor(IStruct struct, DateDataField data) {
			return new DateDataFieldEditor(struct, data);
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory#getPropDataFieldType()
		 */
		@Override
		public Class<DateDataField> getPropDataFieldType() {
			return DateDataField.class;
		}
	}
	
	private DateDataFieldComposite comp;
		
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(Composite parent) {
		if (comp == null)
			comp = new DateDataFieldComposite(parent, this);
		
		return comp;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor#doRefresh()
	 */
	@Override
	public void doRefresh() {
		if (comp != null)
			comp.refresh();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#getControl()
	 */
	public Control getControl() {
		return comp;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#updateProp()
	 */
	public void updatePropertySet() {
		if (!isChanged())
			return;
		
		getDataField().setDate(comp.getDate());
	}
}

class DateDataFieldComposite extends AbstractInlineDataFieldComposite<DateDataFieldEditor> {

	private DateTimeControl dateTimeControl;
	
	public DateDataFieldComposite(Composite parent, DateDataFieldEditor editor) {
		super(parent, SWT.NONE, editor);
	}

	@Override
	public void _refresh() {
		DateStructField dateStructField = (DateStructField) getEditor().getStructField();
		if (dateTimeControl != null)
			dateTimeControl.dispose();
		
		dateTimeControl = new DateTimeControl(this, SWT.NONE, dateStructField.getDateTimeEditFlags(), (Date) null);
		dateTimeControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dateTimeControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getEditor().setChanged(true);
			}
		});
		dateTimeControl.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				getEditor().setChanged(true);
			}
		});
		dateTimeControl.setDate(getEditor().getDataField().getDate());
	}
	
	public Date getDate() {
		return dateTimeControl.getDate();
	}
}

