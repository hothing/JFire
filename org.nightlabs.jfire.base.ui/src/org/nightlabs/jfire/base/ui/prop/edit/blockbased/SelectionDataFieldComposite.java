package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.AbstractListComposite;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractInlineDataFieldComposite;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.ModifyListener;
import org.nightlabs.jfire.prop.datafield.SelectionDataField;
import org.nightlabs.jfire.prop.exception.StructFieldValueNotFoundException;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;

public class SelectionDataFieldComposite
extends AbstractInlineDataFieldComposite<SelectionDataFieldEditor>
{
	private XComboComposite<StructFieldValue> fieldValueCombo;

//	private static class FieldValueHolder {
//		StructFieldValue value;
//
//		public FieldValueHolder(StructFieldValue value) {
//			super();
//			this.value = value;
//		}
//	}
//
//	private static FieldValueHolder EMPTY_SELECTION = new FieldValueHolder(null);

	/**
	 * Assumes to have a parent composite with GridLayout and
	 * adds it own GridData.
	 * @param editor the SelectionDataFieldEditor
	 * @param parent the parent composite
	 * @param style the SWT style
	 */
	public SelectionDataFieldComposite(final SelectionDataFieldEditor editor,
			Composite parent, int style, final ModifyListener modifyListener)
	{
		super(parent, style, editor);
		if (!(parent.getLayout() instanceof GridLayout))
			throw new IllegalArgumentException("Parent should have a GridLayout!"); //$NON-NLS-1$

		LabelProvider labelProvider = new LabelProvider() {
			@Override
			public String getText(Object element) {
//				FieldValueHolder valueHolder = (FieldValueHolder) element;
//				if (valueHolder == EMPTY_SELECTION)
//					return "[enpty]";
//				else
//					return valueHolder.value.getValueName().getText();
				StructFieldValue value = (StructFieldValue) element;
				if (value == null)
					return Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.SelectionDataFieldComposite.value.empty"); //$NON-NLS-1$
				else
					return value.getValueName().getText();
			}
		};

		fieldValueCombo = new XComboComposite<StructFieldValue>(
				this,
				AbstractListComposite.getDefaultWidgetStyle(this),
				(String) null,
				labelProvider,
				LayoutMode.TIGHT_WRAPPER
		);

		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		textData.grabExcessHorizontalSpace = true;
		fieldValueCombo.setLayoutData(textData);

		final ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				modifyListener.modifyData();
			}
		};

		fieldValueCombo.addSelectionChangedListener(selectionChangedListener);
		fieldValueCombo.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				fieldValueCombo.removeSelectionChangedListener(selectionChangedListener);
			}
		});
		if (editor.getDataField() != null) {
			refresh();
		}
	}

	@Override
	public void _refresh() {
		SelectionStructField field = (SelectionStructField) getEditor().getStructField();

		List<StructFieldValue> structFieldValues = new LinkedList<StructFieldValue>(field.getStructFieldValues());
		if (field.allowsEmptySelection())
			structFieldValues.add(0, null);

		fieldValueCombo.setInput( structFieldValues );

		SelectionDataField dataField = getEditor().getDataField();
		if (dataField.getStructFieldValueID() != null) {
			try {
				fieldValueCombo.selectElement(field.getStructFieldValue(dataField.getStructFieldValueID()));
			} catch (StructFieldValueNotFoundException e) {
				if (fieldValueCombo.getItemCount() > 0) {
					fieldValueCombo.selectElementByIndex(0);
				}
				else {
					fieldValueCombo.selectElementByIndex(-1);
				}
				throw new RuntimeException("Could not find the referenced structFieldValue with id "+dataField.getStructFieldValueID()); //$NON-NLS-1$
			}
		} else {
			fieldValueCombo.selectElement(null);
//			if (field.getDefaultValue() != null)
//				fieldValueCombo.selectElement(field.getDefaultValue());
//			else
//				fieldValueCombo.selectElementByIndex(-1);
		}

		fieldValueCombo.setEnabled(getEditor().getDataField().getManagedBy() == null);
	}

	public XComboComposite<StructFieldValue> getFieldValueCombo() {
		return fieldValueCombo;
	}
}
