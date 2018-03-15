package org.nightlabs.jfire.base.ui.prop.validation;

import org.nightlabs.jfire.base.expression.AndCondition;
import org.nightlabs.jfire.base.expression.OrCondition;
import org.nightlabs.jfire.base.ui.resource.Messages;

public class CompositionOperatorLabelProvider extends org.eclipse.jface.viewers.LabelProvider 
{
	@Override
	public String getText(Object element) 
	{
		if (element.equals(AndCondition.OPERATOR_TEXT))
			return Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.CompositionCombo.AND"); //$NON-NLS-1$
		else if (element.equals(OrCondition.OPERATOR_TEXT))
			return Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.CompositionCombo.OR");			 //$NON-NLS-1$
		
		return super.getText(element);
	}
}