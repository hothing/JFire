package org.nightlabs.jfire.base.ui.prop.edit;

import java.util.List;

import org.nightlabs.jfire.prop.validation.ValidationResult;
import org.nightlabs.jfire.prop.validation.ValidationResultType;

/**
 * Abstract implementation of {@link IValidationResultHandler} that lets
 * sub-classes handle only one (the first) validation result.
 * <p>
 * The validation result passed to implementors in {@link #handleValidationResult(ValidationResult)}
 * is either the first error-result found or otherwise the first one in the list.
 * </p>
 *  
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public abstract class ValidationResultHandler implements IValidationResultHandler
{

	@Override
	public void handleValidationResults(List<ValidationResult> validationResults) {
		if (validationResults == null || validationResults.isEmpty()) {
			handleValidationResult(null);
			return;
		}
		for (ValidationResult validationResult : validationResults) {
			if (validationResult.getType() == ValidationResultType.ERROR) {
				// If we find an error we handle that one
				// and skip all prior results
				handleValidationResult(validationResult);
				return;
			}
		}
		// no error could be found, we handle the first in the list.
		ValidationResult firstResult = validationResults.get(0);
		handleValidationResult(firstResult);
	}

	/**
	 * Called from {@link #handleValidationResults(List)} with the first
	 * result in the list.
	 * 
	 * @param validationResult The first result from {@link #handleValidationResults(List)}.
	 */
	public abstract void handleValidationResult(ValidationResult validationResult);
}
