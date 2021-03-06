package org.nightlabs.jfire.trade.admin.ui.tariffuserset;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.entityuserset.ui.AbstractEntitySection;
import org.nightlabs.jfire.entityuserset.ui.AbstractEntityUserSetPage;
import org.nightlabs.jfire.entityuserset.ui.EntityUserSetPageControllerHelper;
import org.nightlabs.jfire.trade.admin.ui.resource.Messages;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public abstract class AbstractTariffUserSetPage 
extends AbstractEntityUserSetPage<Tariff> 
{		
	/**
	 * @param editor
	 * @param id
	 * @param name
	 */
	public AbstractTariffUserSetPage(FormEditor editor) {
		super(editor, AbstractTariffUserSetPage.class.getName(), Messages.getString("org.nightlabs.jfire.trade.admin.ui.tariffuserset.AbstractTariffUserSetPage.page.name")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.entityuserset.ui.AbstractEntityUserSetPage#createEntitySection(org.eclipse.ui.forms.editor.IFormPage, org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected AbstractEntitySection<Tariff> createEntitySection(IFormPage formPage, Composite parent) 
	{
		return new TariffSection(formPage, parent, Messages.getString("org.nightlabs.jfire.trade.admin.ui.tariffuserset.AbstractTariffUserSetPage.section.tariffs.name")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.entityuserset.ui.AbstractEntityUserSetPage#getEntityUserSetPageControllerHelper()
	 */
	@Override
	protected EntityUserSetPageControllerHelper<Tariff> getEntityUserSetPageControllerHelper() 
	{
		return ((TariffUserSetPageController)getPageController()).getTariffUserSetPageControllerHelper();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress#getPageFormTitle()
	 */
	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.trade.admin.ui.tariffuserset.AbstractTariffUserSetPage.page.title"); //$NON-NLS-1$
	}

}
