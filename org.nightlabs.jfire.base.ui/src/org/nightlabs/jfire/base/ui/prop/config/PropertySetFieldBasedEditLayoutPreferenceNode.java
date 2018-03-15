/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.config;

import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.ui.PlatformUI;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutUseCase;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class PropertySetFieldBasedEditLayoutPreferenceNode extends PreferenceNode {

	private PropertySetFieldBasedEditLayoutUseCase useCase;
	private PropertySetFieldBasedEditLayoutPreferencePage page;
	
	/**
	 * 
	 */
	public PropertySetFieldBasedEditLayoutPreferenceNode(PropertySetFieldBasedEditLayoutUseCase useCase) {
		super(PropertySetFieldBasedEditLayoutPreferenceNode.class.getName() + "#" + useCase.getUseCaseID()); //$NON-NLS-1$
		this.useCase = useCase;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceNode#createPage()
	 */
	@Override
	public void createPage() {
		page = new PropertySetFieldBasedEditLayoutPreferencePage(useCase);
		page.init(PlatformUI.getWorkbench());
		page.setTitle(getLabelText());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceNode#disposeResources()
	 */
	@Override
	public void disposeResources() {
		if (page != null) {
			page.dispose();
			page = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceNode#getPage()
	 */
	@Override
	public IPreferencePage getPage() {
		return page;
	}
	
	@Override
	public String getLabelText() {
		return useCase.getName().getText();
	}
}
