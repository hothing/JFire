package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructField;

public class MultiSelectionStructFieldFactory extends AbstractStructFieldFactory
{
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.StructFieldFactory#createStructField(org.nightlabs.jfire.prop.StructBlock, org.eclipse.jface.wizard.WizardPage)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public StructField createStructField(StructBlock block, WizardPage wizardPage) 
	{
		return new MultiSelectionStructField(block);
	}
}
