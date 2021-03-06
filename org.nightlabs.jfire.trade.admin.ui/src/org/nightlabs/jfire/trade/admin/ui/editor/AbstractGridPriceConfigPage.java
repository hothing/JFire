package org.nightlabs.jfire.trade.admin.ui.editor;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageControllerModifyEvent;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.admin.ui.resource.Messages;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class AbstractGridPriceConfigPage
extends EntityEditorPageWithProgress
{
	/**
	 * @param editor
	 * @param id
	 * @param name
	 */
	public AbstractGridPriceConfigPage(FormEditor editor, String id, String name) {
		super(editor, id, name);
	}

	private AbstractGridPriceConfigSection priceConfigSection = null;
	public AbstractGridPriceConfigSection getPriceConfigSection() {
		return priceConfigSection;
	}
	protected abstract AbstractGridPriceConfigSection createGridPriceConfigSection(Composite parent);
	
	@Override
	protected void addSections(Composite parent) {
		priceConfigSection = createGridPriceConfigSection(parent);
		getManagedForm().addPart(priceConfigSection);
		if (getPageController().isLoaded())
			refreshPriceConfigSection();
	}
	
	public ProductTypePriceConfigPageController getPageController() {
		return (ProductTypePriceConfigPageController) super.getPageController();
	}
	
	protected void refreshPriceConfigSection() {
		if (priceConfigSection == null
				|| priceConfigSection.getSection() == null
				|| priceConfigSection.getSection().isDisposed())
			return;
		ProductTypePriceConfigPageController controller = (ProductTypePriceConfigPageController) getPageController();
		final ProductType productType = controller.getProductType();
//		priceConfigSection.getPriceConfigComposite().setInitaliseState(true);
//		try {
		priceConfigSection.setPackageProductType(productType);
//		} finally {
//			priceConfigSection.getPriceConfigComposite().setInitaliseState(false);
//		}
		if (productType.getInnerPriceConfig() != null) {
			priceConfigSection.getSection().setText(
					productType.getInnerPriceConfig().getName().getText());
		}
		if (productType.isClosed()) {
			priceConfigSection.setMessage(
					Messages.getString("org.nightlabs.jfire.trade.admin.ui.editor.AbstractGridPriceConfigPage.priceConfigSection.message_productTypeClosed"), //$NON-NLS-1$
					IMessageProvider.INFORMATION);
			RCPUtil.setControlEnabledRecursive(priceConfigSection.getPriceConfigComposite(), false);
		}
		switchToContent();
	}
	
	@Override
	protected void handleControllerObjectModified(EntityEditorPageControllerModifyEvent modifyEvent) {
//		if (event.getPackagePriceConfig() != null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					refreshPriceConfigSection();
				}
			});
//		}
	}

	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.trade.admin.ui.editor.AbstractGridPriceConfigPage.pageFormTitle"); //$NON-NLS-1$
	}

}
