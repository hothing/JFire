/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.jfire.prop.StructBlock;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class StructBlockCombo extends XComboComposite<StructBlock> 
{
	class LabeLProvider extends LabelProvider {
		@Override
		public String getText(Object element) 
		{
			if (element instanceof StructBlock) {
				StructBlock structBlock = (StructBlock) element;
				return structBlock.getName().getText();
			}
			return super.getText(element);
		}
	}
	
	/**
	 * @param parent
	 * @param comboStyle
	 * @param caption
	 */
	public StructBlockCombo(Composite parent, int comboStyle, String caption) {
		super(parent, comboStyle, caption);
		setLabelProvider(new LabeLProvider());
	}

	/**
	 * @param parent
	 * @param comboStyle
	 */
	public StructBlockCombo(Composite parent, int comboStyle) {
		super(parent, comboStyle);
		setLabelProvider(new LabeLProvider());
	}
}
