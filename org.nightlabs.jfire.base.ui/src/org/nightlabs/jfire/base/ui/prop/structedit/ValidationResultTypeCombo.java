/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

import java.util.Arrays;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.validation.ValidationResultType;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class ValidationResultTypeCombo extends XComboComposite<ValidationResultType> 
{
	class LabelProvider extends org.eclipse.jface.viewers.LabelProvider 
	{
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 */
		@Override
		public Image getImage(Object element) 
		{
			if (element instanceof ValidationResultType) {
				ValidationResultType type = (ValidationResultType) element;
				return getImageForValidationResultType(type);
			}
			return super.getImage(element);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) 
		{
			if (element instanceof ValidationResultType) {
				ValidationResultType type = (ValidationResultType) element;
				switch (type) {
					case ERROR:
						return Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.ValidationResultTypeCombo.error"); //$NON-NLS-1$
					case WARNING:	
						return Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.ValidationResultTypeCombo.warning"); //$NON-NLS-1$
					case INFO:
						return Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.ValidationResultTypeCombo.info"); //$NON-NLS-1$
				}
			}			
			return super.getText(element);
		}
		
		private Image getImageForValidationResultType(ValidationResultType type)
		{
			String key = null;
			switch (type) {
				case ERROR:
					key = ISharedImages.IMG_OBJS_ERROR_TSK;
					break;
				case WARNING:
					key = ISharedImages.IMG_OBJS_WARN_TSK	;
					break;
				case INFO:
					key = ISharedImages.IMG_OBJS_INFO_TSK;
					break;
			}
			ImageDescriptor desc = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(key);
			return desc.createImage();
		}			
	}

	/**
	 * @param parent
	 * @param comboStyle
	 * @param caption
	 */
	public ValidationResultTypeCombo(Composite parent, int comboStyle,
			String caption) {
		super(parent, comboStyle, caption);
		setLabelProvider(new LabelProvider());
		setInput(Arrays.asList(ValidationResultType.values()));
	}

	/**
	 * @param parent
	 * @param comboStyle
	 */
	public ValidationResultTypeCombo(Composite parent, int comboStyle) {
		super(parent, comboStyle);
		setLabelProvider(new LabelProvider());
		setInput(Arrays.asList(ValidationResultType.values()));
	}
	
}
