/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.validation;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public interface IExpressionValidatorHandler 
{
	/**
	 * Sets the {@link IExpressionValidatorEditor}.
	 * @param editor the IExpressionValidatorEditor to set.
	 */
	public void setExpressionValidatorEditor(IExpressionValidatorEditor editor);
	
	/**
	 * Returns the IExpressionValidatorEditor.
	 * @return the {@link IExpressionValidatorEditor}
	 */
	public IExpressionValidatorEditor getExpressionValidatorEditor();
	
	/**
	 * This method is called when the user pressed the add expression button,
	 * in the IExpressionValidatorEditor.
	 */
	public void addExpressionPressed();
}
