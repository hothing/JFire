/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.validation;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.prop.validation.ValidationResultType;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public interface IExpressionValidatorEditor 
{
	/**
	 * Sets the {@link IExpression} which is modified by this {@link IExpressionValidatorEditor}.
	 * @param expression the IExpression to set
	 */
	public void setExpression(IExpression expression);
	
	/**
	 * Returns the (modified) {@link IExpression}.
	 * @return the {@link IExpression}. 
	 */
	public IExpression getExpression(); 
	
	/**
	 * Returns the currently selected {@link IExpression}.
	 * @return the selected {@link IExpression}.
	 */
	public IExpression getSelectedExpression();
	
	/**
	 * Sets the message.
	 * @param message the {@link I18nText}to set.
	 */
	public void setMessage(I18nText message);

	/**
	 * Returns the (modified) I18nText message;
	 * @return the message.
	 */
	public I18nText getMessage();
	
	/**
	 * Sets the {@link ValidationResultType}.
	 * @param type the ValidationResultType to set.
	 */
	public void setValidationResultType(ValidationResultType type);
	
	/**
	 * Returns the (modified) {@link ValidationResultType}.
	 * @return the ValidationResultType.
	 */
	public ValidationResultType getValidationResultType();
	
	/**
	 * Refreshes the IExpressionValidatorEditor.
	 */
	public void refresh(); 
}
