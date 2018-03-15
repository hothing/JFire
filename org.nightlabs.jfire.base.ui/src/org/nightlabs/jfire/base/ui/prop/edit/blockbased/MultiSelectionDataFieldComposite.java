package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractInlineDataFieldComposite;
import org.nightlabs.jfire.prop.ModifyListener;
import org.nightlabs.jfire.prop.datafield.MultiSelectionDataField;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructField;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructFieldValue;

/**
 * This shows check-boxes for all available options.
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class MultiSelectionDataFieldComposite
extends AbstractInlineDataFieldComposite<MultiSelectionDataFieldEditor>
{
	private Composite wrapper;
	private final ModifyListener modifyListener;

	public MultiSelectionDataFieldComposite(final MultiSelectionDataFieldEditor editor, Composite parent, int style, final ModifyListener modifyListener)
	{
		super(parent, style, editor);
		this.modifyListener = modifyListener;
		setLayout(new GridLayout());
		refresh();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractInlineDataFieldComposite#_refresh()
	 */
	@Override
	protected void _refresh()
	{
		if(wrapper != null) {
			wrapper.dispose();
			wrapper = null;
		}
		wrapper = new Composite(this, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		wrapper.setLayout(gl);
		wrapper.setLayoutData(new GridData());
		MultiSelectionStructField structField = (MultiSelectionStructField) getEditor().getStructField();
		MultiSelectionDataField dataField = getEditor().getDataField();
		Set<MultiSelectionStructFieldValue> selectedValues = new HashSet<MultiSelectionStructFieldValue>(dataField.getStructFieldValues());
		List<MultiSelectionStructFieldValue> structFieldValues = structField.getStructFieldValues();
		for (MultiSelectionStructFieldValue structFieldValue : structFieldValues) {
			Button b = new Button(wrapper, SWT.CHECK);
			b.setData(structFieldValue);
			b.setText(structFieldValue.getValueName().getText());
			b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			if(selectedValues.contains(structFieldValue))
				b.setSelection(true);
			if(modifyListener != null) {
				b.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						modifyListener.modifyData();
					}
				});
			}
		}
		layout(true, true);
	}

	/**
	 * Get the selected values.
	 * @return the selected values
	 */
	public Collection<MultiSelectionStructFieldValue> getSelection()
	{
		if(wrapper == null)
			return null;
		Control[] children = wrapper.getChildren();
		HashSet<MultiSelectionStructFieldValue> result = new HashSet<MultiSelectionStructFieldValue>(children.length);
		for (Control control : children) {
			if(!(control instanceof Button))
				continue;
			Button b = (Button) control;
			if(!b.getSelection())
				continue;
			Object data = b.getData();
			if(data == null || !(data instanceof MultiSelectionStructFieldValue))
				continue;
			result.add((MultiSelectionStructFieldValue) data);
		}
		return result;
	}
}
