/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.jfire.prop.StructField;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class StructFieldCombo extends XComboComposite<StructField<?>> 
{
	class LabelProvider extends org.eclipse.jface.viewers.LabelProvider {
		@Override
		public String getText(Object element) 
		{
			if (element instanceof StructField<?>) {
				StructField<?> structField = (StructField<?>) element;
				return structField.getName().getText();
			}
			return super.getText(element);
		}
	}
	
	/**
	 * @param parent
	 * @param comboStyle
	 */
	public StructFieldCombo(Composite parent, int comboStyle) {
		super(parent, comboStyle);
		setLabelProvider(new LabelProvider());
	}

	/**
	 * @param parent
	 * @param comboStyle
	 * @param caption
	 */
	public StructFieldCombo(Composite parent, int comboStyle, String caption) {
		super(parent, comboStyle, caption);
		setLabelProvider(new LabelProvider());
	}

}
