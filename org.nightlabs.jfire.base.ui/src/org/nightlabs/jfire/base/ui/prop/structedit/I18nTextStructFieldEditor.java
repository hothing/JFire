package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.structfield.I18nTextStructField;

public class I18nTextStructFieldEditor extends AbstractStructFieldEditor<I18nTextStructField> {

	public static class I18nTextStructFieldEditorFactory extends AbstractStructFieldEditorFactory {
//		public String getStructFieldEditorClass() {
//			return I18nTextStructFieldEditor.class.getName();
//		}

		@Override
		public StructFieldEditor createStructFieldEditor() {
			return new I18nTextStructFieldEditor();
		}
	}

	private I18nTextStructField textField;
	private Spinner lineCountSpinner;

	@Override
	protected Composite createSpecialComposite(Composite parent, int style) {
		XComposite comp = new XComposite(parent, style);
		comp.getGridLayout().numColumns = 3;
		new Label(comp, SWT.NONE).setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.I18nTextStructFieldEditor.lineCountLabel.text")); //$NON-NLS-1$
		lineCountSpinner = new Spinner(comp, comp.getBorderStyle());
		lineCountSpinner.setMinimum(1);
		lineCountSpinner.setIncrement(1);
		lineCountSpinner.setSelection(1);

		lineCountSpinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (ignoreModifyEvent)
					return;

				textField.setLineCount(lineCountSpinner.getSelection());
				setChanged();
			}
		});
		return comp;
	}

	private boolean ignoreModifyEvent = false;

	@Override
	protected void setSpecialData(I18nTextStructField field) {
		this.textField = field;
		ignoreModifyEvent = true;
		try {
			lineCountSpinner.setSelection(field.getLineCount());
		} finally {
			ignoreModifyEvent = false;
		}
	}
}
