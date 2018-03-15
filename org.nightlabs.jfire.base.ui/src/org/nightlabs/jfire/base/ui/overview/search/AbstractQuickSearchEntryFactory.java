package org.nightlabs.jfire.base.ui.overview.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nightlabs.base.ui.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.ui.validation.InputValidator;
import org.nightlabs.jdo.query.AbstractSearchQuery;

/**
 * Abstract base class for {@link QuickSearchEntryFactory}s
 *
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 */
public abstract class AbstractQuickSearchEntryFactory<Q extends AbstractSearchQuery>
	implements QuickSearchEntryFactory<Q>
{
	private InputValidator<?> validator;

//	private Image composedDecoratorImage = null;
	private Image image = null;
	private Image decoratorImage = null;
	private String name = null;
	private String id = null;
	private boolean isDefault = false;

	public Image getDecoratorImage() {
		return decoratorImage;
	}
	public void setDecoratorImage(Image decoratorImage) {
		this.decoratorImage = decoratorImage;
	}

	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}

//	public Image getComposedDecoratorImage()
//	{
//		if (composedDecoratorImage == null && getDecoratorImage() != null) {
//			composedDecoratorImage = new SearchCompositeImage(getDecoratorImage()).createImage();
//		}
//		return composedDecoratorImage;
//	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isDefault()
	{
		return isDefault;
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
	throws CoreException
	{
		if (config.getName().equals(QuickSearchEntryRegistry.ELEMENT_QUICK_SEARCH_ENTRY_FACTORY)) {
			String decoratorString = config.getAttribute(QuickSearchEntryRegistry.ATTRIBUTE_DECORATOR_IMAGE);
			String iconString = config.getAttribute(QuickSearchEntryRegistry.ATTRIBUTE_IMAGE);
			String name = config.getAttribute(QuickSearchEntryRegistry.ATTRIBUTE_NAME);
			String idString = config.getAttribute(QuickSearchEntryRegistry.ATTRIBUTE_ID);
			String isDefault = config.getAttribute(QuickSearchEntryRegistry.ATTRIBUTE_DEFAULT);

			if (AbstractEPProcessor.checkString(name)) {
				this.name = name;
			}
			if (AbstractEPProcessor.checkString(iconString)) {
				ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
						config.getNamespaceIdentifier(), iconString);
				if (imageDescriptor != null)
					image = imageDescriptor.createImage();
			}
			if (AbstractEPProcessor.checkString(decoratorString)) {
				ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
						config.getNamespaceIdentifier(), decoratorString);
				if (imageDescriptor != null)
					decoratorImage = imageDescriptor.createImage();
			}
			if (AbstractEPProcessor.checkString(idString)) {
				id = idString;
			}
			if (AbstractEPProcessor.checkString(isDefault))
			{
				this.isDefault = Boolean.parseBoolean(isDefault);
			}

			validator = createInputValidator();
		}
	}

	/**
	 * This method should be overridden in order to provide an input validation of the String set to corresponding query.
	 * @return The {@link InputValidator} checking the string that
	 */
	protected InputValidator<?> createInputValidator()
	{
		return null;
	}

	@Override
	public InputValidator<?> getInputValidator()
	{
		return validator;
	}

	@Override
	public int compareTo(QuickSearchEntryFactory<Q> o)
	{
		return getName().compareTo(o.getName());
	}
}
