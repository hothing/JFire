package org.nightlabs.jfire.base.ui.prop.validation;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public interface IScriptValidatorHandler 
{
	/**
	 * Returns the IScriptValidatorEditor.
	 * @return the {@link IScriptValidatorEditor}
	 */
	public IScriptValidatorEditor getScriptValidatorEditor();
	
	/**
	 * Sets the IScriptValidatorEditor.
	 * @param editor the IScriptValidatorEditor to set.
	 */
	public void setScriptValidatorEditor(IScriptValidatorEditor editor);
	
	/**
	 * This method get called when the users presses on the add template button,
	 * in the IScriptValidatorEditor.
	 */
	public void addTemplate();
	
	/**
	 * Validates the given script.
	 * @param script the script as string to validate
	 * @return null if the script is ok or the error message as string.
	 */
	public String validateScript(String script);
}
