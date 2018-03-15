package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public abstract class AbstractDataBlockEditorFactory implements DataBlockEditorFactory {

	private String bundleName;
	private StructBlockID structBlockID;
	
	public AbstractDataBlockEditorFactory() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditorFactory#getProviderStructBlockID()
	 */
	@Override
	public StructBlockID getProviderStructBlockID() {
		if (structBlockID == null) {
			throw new IllegalStateException("The StructBlockID from could not be read for a registration of " + this.getClass().getName()  + "in " + bundleName); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return structBlockID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		bundleName = config.getDeclaringExtension().getNamespaceIdentifier();
		String structBlockIDStr = config.getAttribute("structBlockID"); //$NON-NLS-1$
		if (structBlockIDStr != null && !"".equals(structBlockIDStr)) { //$NON-NLS-1$
			structBlockID = (StructBlockID) ObjectIDUtil.createObjectID(structBlockIDStr);
		}
	}
}
