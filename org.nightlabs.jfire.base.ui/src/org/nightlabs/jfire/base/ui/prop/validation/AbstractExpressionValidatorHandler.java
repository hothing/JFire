/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.validation;

import org.nightlabs.jfire.base.expression.Composition;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.validation.GenericDataFieldNotEmptyExpression;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public abstract class AbstractExpressionValidatorHandler 
implements IExpressionValidatorHandler 
{
	private IExpressionValidatorEditor expressionValidatorEditor;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IAddExpressionValidatorHandler#getExpressionValidatorComposite()
	 */
	@Override
	public IExpressionValidatorEditor getExpressionValidatorEditor() {
		return expressionValidatorEditor;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IAddExpressionValidatorHandler#setExpressionValidatorComposite(org.nightlabs.jfire.base.ui.prop.structedit.ExpressionValidatorComposite)
	 */
	@Override
	public void setExpressionValidatorEditor(IExpressionValidatorEditor editor) {
		this.expressionValidatorEditor = editor;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IAddExpressionValidatorHandler#addExpressionPressed()
	 */
	@Override
	public void addExpressionPressed() 
	{
		StructFieldID structFieldID = getStructFieldID();
		if (structFieldID != null) {
			IExpression newExpression = new GenericDataFieldNotEmptyExpression(structFieldID);
			IExpression selectedExpression = getExpressionValidatorEditor().getSelectedExpression();
			if (selectedExpression != null) {
				if (selectedExpression instanceof Composition) {
					Composition composition = (Composition) selectedExpression;
					composition.addExpression(newExpression);
					getExpressionValidatorEditor().refresh();
				}
			}
			else if (getExpressionValidatorEditor().getExpression() == null) {
				getExpressionValidatorEditor().setExpression(newExpression);
			}
		}
	}
	
	protected abstract StructFieldID getStructFieldID();
}
