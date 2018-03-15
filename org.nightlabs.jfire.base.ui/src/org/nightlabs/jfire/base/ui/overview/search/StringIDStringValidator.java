package org.nightlabs.jfire.base.ui.overview.search;

import org.nightlabs.base.ui.message.MessageType;
import org.nightlabs.base.ui.validation.AbstractValidator;
import org.nightlabs.datastructure.Pair;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.ui.resource.Messages;

/**
 * Validator that checks that a given String only consists of the allowed characters for an ID string. <br>
 * See {@link ObjectIDUtil#isValidIDString(String)}
 *
 * @author Marius Heinzmann <!-- marius[at]nightlabs[dot]de -->
 */
public class StringIDStringValidator
	extends AbstractValidator<String>
{

	public StringIDStringValidator()
	{
		super(String.class);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.validation.AbstractValidator#doValidateInput(java.lang.Object)
	 */
	@Override
	public Pair<MessageType, String> doValidateInput(String input)
	{
		if (input == null || input.trim().length() == 0 || ObjectIDUtil.isValidIDString(input))
			return null;

		return new Pair<MessageType, String>(MessageType.ERROR, Messages.getString("org.nightlabs.jfire.base.ui.overview.search.StringIDStringValidator.errorMessage")); //$NON-NLS-1$
	}

}

