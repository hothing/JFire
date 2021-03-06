package org.nightlabs.jfire.trade.admin.ui.editor;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageControllerModifyEvent;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.admin.ui.resource.Messages;

/**
 * Abstract Base class which implements the {@link IProductTypeDetailPage} and
 * should be used for all detail pages for {@link ProductType}s.
 *
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 */
public abstract class AbstractProductTypeDetailPage
extends EntityEditorPageWithProgress
implements IProductTypeDetailPage
{
	private static final Logger logger = Logger.getLogger(AbstractProductTypeDetailPage.class);

	/**
	 * @param editor the FormEditor the page belongs to
	 * @param id the ID of the page
	 * @param name the name of the page
	 */
	public AbstractProductTypeDetailPage(FormEditor editor, String id, String name) {
		super(editor, id, name);
	}

	private int sectionStyle = ExpandableComposite.TITLE_BAR;
	/**
	 * returns the sectionStyle.
	 * @return the sectionStyle
	 */
	public int getSectionStyle() {
		return sectionStyle;
	}
	/**
	 * sets the sectionStyle
	 * @param sectionStyle the sectionStyle to set
	 */
	public void setSectionStyle(int sectionStyle) {
		this.sectionStyle = sectionStyle;
	}

	private IProductTypeSectionPart nameSection = null;
	/**
	 * Returns the {@link IProductTypeSectionPart} which is responsible for displaying the name
	 * of the {@link ProductType}.
	 * Returns the {@link IProductTypeSectionPart} created by {@link #createNameSection(Composite)}.
	 *
	 * @return the IProductTypeSectionPart which is responsible for displaying the name
	 * of the {@link ProductType}.
	 */
	public IProductTypeSectionPart getNameSection() {
		return nameSection;
	}
	/**
	 * Creates the {@link IProductTypeSectionPart} which is responsible for displaying the name
	 * of the {@link ProductType}.
	 * @param parent the parent composite
	 * @return the {@link IProductTypeSectionPart} which is responsible for displaying the name
	 * of the {@link ProductType}.
	 */
	protected abstract IProductTypeSectionPart createNameSection(Composite parent);

	private IProductTypeSectionPart vendorSection = null;
	public IProductTypeSectionPart getVendorSection() {
		return vendorSection;
	}
	/**
	 * Returns the {@link IProductTypeSectionPart} which is responsible for
	 * displaying and editing the vendor of the {@link ProductType}.
	 *
	 * @param parent the parent composite
	 * @return the {@link IProductTypeSectionPart} which is responsible for
	 * displaying and editing the vendor of the {@link ProductType}.
	 */
	protected abstract IProductTypeSectionPart createVendorSection(Composite parent);

	private IProductTypeSectionPart ownerSection = null;
	public IProductTypeSectionPart getOwnerSection() {
		return ownerSection;
	}
	/**
	 * Creates and returns the {@link IProductTypeSectionPart} which is responsible for
	 * displaying and editing the owner of the {@link ProductType}.
	 * @param parent the parent composite
	 * @return the {@link IProductTypeSectionPart} which is responsible for
	 * displaying and editing the owner of the {@link ProductType}.
	 */
	protected abstract IProductTypeSectionPart createOwnerSection(Composite parent);

	private IProductTypeSectionPart nestedProductTypeSection = null;
	public IProductTypeSectionPart getNestedProductTypeSection() {
		return nestedProductTypeSection;
	}
	protected abstract IProductTypeSectionPart createNestedProductTypesSection(Composite parent);

	private IProductTypeSectionPart saleAccessControlSection = null;
	public IProductTypeSectionPart getSaleAccessControlSection() {
		return saleAccessControlSection;
	}
	/**
	 * Creates and returns the {@link IProductTypeSectionPart} which is responsible for
	 * displaying and editing sale access states of the product type.
	 * @param parent the parent composite
	 * @return the {@link IProductTypeSectionPart} which is responsible for
	 * displaying and editing sale access states of the product type.
	 */
	protected abstract IProductTypeSectionPart createSaleAccessControlSection(Composite parent);

	@Override
	protected void addSections(Composite parent)
	{
		nameSection = createNameSection(parent);
		if (nameSection != null) {
			nameSection.getSection().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			getManagedForm().addPart(nameSection);
		}

		nestedProductTypeSection = createNestedProductTypesSection(parent);
		if (nestedProductTypeSection != null) {
			nestedProductTypeSection.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
			getManagedForm().addPart(nestedProductTypeSection);
		}

		Composite wrapper = new XComposite(parent, SWT.NONE);
		wrapper.setLayout(new GridLayout(2, true));
		wrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ownerSection = createOwnerSection(wrapper);
		if (ownerSection != null) {
			ownerSection.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
			getManagedForm().addPart(ownerSection);
		}

		vendorSection = createVendorSection(wrapper);
		if (vendorSection != null) {
			vendorSection.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
			getManagedForm().addPart(vendorSection);
		}

		if (ownerSection == null && vendorSection == null)
			wrapper.dispose();

		saleAccessControlSection = createSaleAccessControlSection(parent);
		if (saleAccessControlSection != null) {
			saleAccessControlSection.getSection().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			getManagedForm().addPart(saleAccessControlSection);
		}
	}

	@Override
	public AbstractProductTypePageController getPageController() {
		return (AbstractProductTypePageController) super.getPageController();
	}

	@Override
	protected void handleControllerObjectModified(EntityEditorPageControllerModifyEvent modifyEvent) {
		//final AbstractProductTypePageController controller = (AbstractProductTypePageController) getPageController();
	//	final ProductType productType = controller.getProductType();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (isDisposed())
					return;
				if (logger.isDebugEnabled()) {
					logger.debug("handleControllerObjectModified: Calling setProductTypePageController()"); //$NON-NLS-1$
					logger.debug("ControllerObject: " + getPageController().getControllerObject()); //$NON-NLS-1$
				}
				setProductTypePageController(getPageController());
				switchToContent();
			}
		});
	}

	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.trade.admin.ui.editor.AbstractProductTypeDetailPage.pageFormTitle"); //$NON-NLS-1$
	}

	/**
	 * Sets the {@link AbstractProductTypePageController}. This method is called whenever the ProductType changes - even if the controller is the same.
	 *
	 * @param pageController the controller.
	 */
	protected void setProductTypePageController(AbstractProductTypePageController<ProductType> pageController)
	{
		if (pageController == null) {
			getManagedForm().getForm().getForm().setMessage(Messages.getString("org.nightlabs.jfire.trade.admin.ui.editor.AbstractProductTypeDetailPage.form.message"), IMessageProvider.INFORMATION); //$NON-NLS-1$
			RCPUtil.setControlEnabledRecursive(getManagedForm().getForm(), false);
		}
		else if (pageController.getProductType().isClosed()) {
			getManagedForm().getForm().getForm().setMessage(
					Messages.getString("org.nightlabs.jfire.trade.admin.ui.editor.AbstractProductTypeDetailPage.productTypeClosedMessage"),  //$NON-NLS-1$
					IMessageProvider.INFORMATION);
			RCPUtil.setControlEnabledRecursive(getManagedForm().getForm(), false);
		}
		else {
			getManagedForm().getForm().getForm().setMessage(null, IMessageProvider.INFORMATION);
			RCPUtil.setControlEnabledRecursive(getManagedForm().getForm(), true);
		}

		if (nameSection != null)
			nameSection.setProductTypePageController(pageController);
		if (nestedProductTypeSection != null)
			nestedProductTypeSection.setProductTypePageController(pageController);
		if (saleAccessControlSection != null)
			saleAccessControlSection.setProductTypePageController(pageController);
		if (ownerSection != null)
			ownerSection.setProductTypePageController(pageController);
		if (vendorSection != null)
			vendorSection.setProductTypePageController(pageController);
	}

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.trade.admin.ui.editor.IProductTypeDetailPage#getProductTypeDetailPageController()
	 */
	public IProductTypeDetailPageController getProductTypeDetailPageController() {
		if (getPageController() instanceof IProductTypeDetailPageController)
			return (IProductTypeDetailPageController) getPageController();
		throw new IllegalStateException("AbstractProductTypeDetailPage should be used with a controller implementing " + IProductTypeDetailPageController.class.getName() + ". The controller is " + getPageController().getClass().getName() + "."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

}