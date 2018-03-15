/* *****************************************************************************
 * NightLabs Editor2D - Graphical editor framework                             *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/
package org.nightlabs.editor2d.ui.decorators;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.nightlabs.editor2d.DrawComponent;
import org.nightlabs.editor2d.ui.edit.tree.DrawComponentTreeEditPart;
import org.nightlabs.editor2d.ui.resource.Messages;

/**
 * <p> Author: Daniel.Mazurek[AT]NightLabs[DOT]de </p>
 */
public class DrawComponentTreeLabelDecorator
implements ILabelDecorator
{
	public Image decorateImage(Image image, Object element)
	{
		if (element instanceof DrawComponentTreeEditPart) {
			DrawComponentTreeEditPart dctep = (DrawComponentTreeEditPart) element;
			DrawComponent dc = dctep.getDrawComponent();
			if (!dc.isVisible()) {
				return new VisibleCompositeImage(image).createImage();
			}
		}
		return null;
	}

	public String decorateText(String text, Object element)
	{
		if (element instanceof DrawComponentTreeEditPart) {
			DrawComponentTreeEditPart dctep = (DrawComponentTreeEditPart) element;
			DrawComponent dc = dctep.getDrawComponent();
			StringBuffer sb = new StringBuffer();
			sb.append(text);
			if (!dc.isVisible()) {
				sb.append(" ["+Messages.getString("org.nightlabs.editor2d.ui.decorators.DrawComponentTreeLabelDecorator.textAddition.invisible")+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			if (dc.isTemplate()) {
				sb.append(" ["+Messages.getString("org.nightlabs.editor2d.ui.decorators.DrawComponentTreeLabelDecorator.template")+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			if (!dc.isEditable()) {
				sb.append( "["+Messages.getString("org.nightlabs.editor2d.ui.decorators.DrawComponentTreeLabelDecorator.locked")+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			return sb.toString();
		}
		return null;
	}

	public void addListener(ILabelProviderListener listener) {

	}

	public void dispose() {

	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {

	}
}
