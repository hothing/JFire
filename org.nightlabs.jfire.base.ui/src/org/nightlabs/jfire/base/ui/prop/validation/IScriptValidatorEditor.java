/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.validation;

import org.nightlabs.jfire.prop.validation.IScriptValidator;

/**
 * Interface for entities which provide an editor functionality for editing {@link IScriptValidator}s.
 * 
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public interface IScriptValidatorEditor 
{
	/**
	 * Returns the edited {@link IScriptValidator}.
	 * @return the edited {@link IScriptValidator}
	 */
	public IScriptValidator<?, ?> getScriptValidator();
	
	/**
	 * Returns the current selected ValidationResultKey of the {@link IScriptValidator}.
	 * @return the current selected ValidationResultKey of the IScriptValidator
	 */
	public String getCurrentKey();
	
	/**
	 * Returns the current edited script as string.
	 * @return the current edited script as string
	 */
	public String getScript();
	
	/**
	 * Sets the script text as string.
	 * @param script the script to set
	 */
	public void setScript(String script);
}
