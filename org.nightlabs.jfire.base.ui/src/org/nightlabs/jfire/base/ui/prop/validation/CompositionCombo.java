package org.nightlabs.jfire.base.ui.prop.validation;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComboComposite;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class CompositionCombo extends XComboComposite<String> 
{
	/**
	 * @param parent
	 * @param comboStyle
	 */
	public CompositionCombo(Composite parent, int comboStyle) {
		super(parent, comboStyle);
		setLabelProvider(new CompositionOperatorLabelProvider());
	}

}
