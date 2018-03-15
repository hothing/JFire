package org.nightlabs.jfire.base.ui.prop;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.nightlabs.jfire.prop.validation.ValidationResultType;

public class ValidationUtil
{
	public static int getIMessageProviderType(ValidationResultType type) {
		switch (type) {
		case ERROR: return IMessageProvider.ERROR;
		case WARNING: return IMessageProvider.WARNING;
		case INFO: return IMessageProvider.INFORMATION;
		default: throw new IllegalArgumentException("No IMessageProviderType found for the given ValidationResultType."); //$NON-NLS-1$
		}
	}
}
