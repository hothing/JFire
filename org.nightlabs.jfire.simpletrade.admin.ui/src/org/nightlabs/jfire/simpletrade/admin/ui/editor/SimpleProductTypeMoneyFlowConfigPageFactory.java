/**
 * 
 */
package org.nightlabs.jfire.simpletrade.admin.ui.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.jfire.base.jdo.IJDOObjectDAO;
import org.nightlabs.jfire.simpletrade.dao.SimpleProductTypeDAO;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.trade.admin.ui.editor.ProductTypeMoneyFlowConfigPage;
import org.nightlabs.jfire.trade.admin.ui.editor.ProductTypeMoneyFlowConfigPageController;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class SimpleProductTypeMoneyFlowConfigPageFactory implements
		IEntityEditorPageFactory {

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory#createPage(org.eclipse.ui.forms.editor.FormEditor)
	 */
	public IFormPage createPage(FormEditor formEditor) {
		return new ProductTypeMoneyFlowConfigPage(formEditor);
	}

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory#createPageController(org.nightlabs.base.ui.entity.editor.EntityEditor)
	 */
	public IEntityEditorPageController createPageController(EntityEditor editor) {
		return new ProductTypeMoneyFlowConfigPageController<SimpleProductType, IJDOObjectDAO<SimpleProductType>>(editor) {
			
			@Override
			protected IJDOObjectDAO<SimpleProductType> getProductTypeDAO() {
				return SimpleProductTypeDAO.sharedInstance();
			}
		};
	}

}
