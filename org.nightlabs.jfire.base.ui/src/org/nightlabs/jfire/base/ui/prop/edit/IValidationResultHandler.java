package org.nightlabs.jfire.base.ui.prop.edit;

import java.util.List;

import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * Interface used to indirect the handling of the results of the validation
 * of a {@link PropertySet}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public interface IValidationResultHandler {
	
	/**
	 * Called to signal the implementation that a new valiation
	 * has been performed and resulted with the given results.
	 * 
	 * @param validationResults The results to handle.
	 */
	void handleValidationResults(List<ValidationResult> validationResults);
}
