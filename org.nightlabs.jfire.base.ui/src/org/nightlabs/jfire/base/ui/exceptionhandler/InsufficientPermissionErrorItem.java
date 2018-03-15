package org.nightlabs.jfire.base.ui.exceptionhandler;

import org.nightlabs.base.ui.exceptionhandler.ErrorItem;

public class InsufficientPermissionErrorItem extends ErrorItem
{
	private InsufficientPermissionDialogContext context;

	public InsufficientPermissionErrorItem(
			String message, Throwable thrownException, Throwable triggerException,
			InsufficientPermissionDialogContext context
	)
	{
		super(message, thrownException, triggerException);
		this.context = context;
		if (context == null)
			throw new IllegalArgumentException("context must not be null!"); //$NON-NLS-1$
	}

	public InsufficientPermissionDialogContext getContext() {
		return context;
	}
}
