package org.nightlabs.jfire.base.ui.prop.structedit;

import org.nightlabs.jfire.prop.exception.PropertyException;

public class StructFieldEditorFactoryNotFoundException extends PropertyException
{
	private static final long serialVersionUID = 1L;

	public StructFieldEditorFactoryNotFoundException(String message)
	{
		super(message);
	}
}
