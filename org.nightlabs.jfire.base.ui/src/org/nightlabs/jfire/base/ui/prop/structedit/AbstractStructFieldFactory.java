package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.nightlabs.base.ui.wizard.DynamicPathWizardPage;


public abstract class AbstractStructFieldFactory implements StructFieldFactory
{
	private String structFieldClass;

	public void setInitializationData(IConfigurationElement element, String propertyName, Object data) throws CoreException
	{
		structFieldClass = element.getAttribute("class"); //$NON-NLS-1$
	}
	
	/**
	 * Extendors should overwrite this method if the creation of the new struct field requires additional user input.
	 * The creation wizard displays this page after the selection of the struct field types.
	 * Return null if no additional information is required.
	 */
	public DynamicPathWizardPage createWizardPage() {
		return null;
	}

	public String getStructFieldClass() {
		return structFieldClass;
	}
}
