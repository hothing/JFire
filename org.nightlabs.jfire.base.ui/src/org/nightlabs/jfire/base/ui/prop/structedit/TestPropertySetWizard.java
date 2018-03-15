package org.nightlabs.jfire.base.ui.prop.structedit;

import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.EditorStructBlockRegistry;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.FullDataBlockCoverageWizardPage;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.PropertySet;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class TestPropertySetWizard extends DynamicPathWizard 
{
	private PropertySet propertySet;
	private EditorStructBlockRegistry editorStructBlockRegistry;
	private FullDataBlockCoverageWizardPage page;
	
	public TestPropertySetWizard(PropertySet propertySet, EditorStructBlockRegistry editorStructBlockRegistry) 
	{
		this.propertySet = propertySet;
		this.editorStructBlockRegistry = editorStructBlockRegistry;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.wizard.DynamicPathWizard#addPages()
	 */
	@Override
	public void addPages() {
		page = new FullDataBlockCoverageWizardPage(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.TestPropertySetWizard.page.title"),  //$NON-NLS-1$
				Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.TestPropertySetWizard.page.message"),  //$NON-NLS-1$
				propertySet, true, editorStructBlockRegistry);
		addPage(page);
		super.addPages();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		return true;
	}

}
