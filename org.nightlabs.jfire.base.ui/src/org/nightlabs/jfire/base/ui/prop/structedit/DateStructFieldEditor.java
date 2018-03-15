package org.nightlabs.jfire.base.ui.prop.structedit;


import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.prop.structfield.DateStructField;

public class DateStructFieldEditor extends AbstractStructFieldEditor<DateStructField> {
	public static class DateStructFieldEditorFactory extends AbstractStructFieldEditorFactory {
//		public String getStructFieldEditorClass() {
//			return DateStructFieldEditor.class.getName();
//		}

		@Override
		public StructFieldEditor createStructFieldEditor() {
			return new DateStructFieldEditor();
		}
	}

	private DateStructFieldEditComposite comp;

	@Override
	protected Composite createSpecialComposite(Composite parent, int style) {
		comp = new DateStructFieldEditComposite(parent, style, this);
		return comp;
	}

	@Override
	protected void setSpecialData(DateStructField field) {
		comp.setField(field);
	}
}
