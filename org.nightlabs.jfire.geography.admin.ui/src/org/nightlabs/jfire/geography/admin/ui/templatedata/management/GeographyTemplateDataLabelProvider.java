package org.nightlabs.jfire.geography.admin.ui.templatedata.management;

import java.util.HashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class GeographyTemplateDataLabelProvider extends LabelProvider {
	public HashMap<ImageDescriptor, Image>imageCache = new HashMap<ImageDescriptor, Image>();
	@Override
	public Image getImage(Object element) {
		Image image = null;
		if(element instanceof GeographyTreeNode){
			GeographyTreeNode node = (GeographyTreeNode)element;
			ImageDescriptor descriptor = node.getImageDescriptor();

			//obtain the cached image corresponding to the descriptor
			image = imageCache.get(descriptor);
			if (image == null && descriptor != null) {
				image = descriptor.createImage();
				imageCache.put(descriptor, image);
			}
		}//if
		return image;
	}

	@Override
	public String getText(Object element) {
		String result = null;
		if(element instanceof GeographyTreeNode){
			GeographyTreeNode node = (GeographyTreeNode)element;
			result = node.getLabel();
		}//if
		else if (element instanceof String)
			result = element.toString();
		
		return result==null?"":result; //$NON-NLS-1$
	}
}