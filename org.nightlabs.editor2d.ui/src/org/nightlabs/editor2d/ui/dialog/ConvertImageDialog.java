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
package org.nightlabs.editor2d.ui.dialog;

import java.awt.image.BufferedImage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.eclipse.ui.dialog.ResizableTrayDialog;
import org.nightlabs.editor2d.ui.composite.ConvertImageComposite;
import org.nightlabs.editor2d.ui.resource.Messages;

/**
 * <p> Author: Daniel.Mazurek[AT]NightLabs[DOT]de </p>
 */
public class ConvertImageDialog
extends ResizableTrayDialog
{
	private BufferedImage image = null;
	private ConvertImageComposite convertImageComp = null;

	/**
	 * @param parentShell
	 */
	public ConvertImageDialog(Shell parentShell, BufferedImage originalImage)
	{
		super(parentShell, Messages.RESOURCE_BUNDLE);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.image = originalImage;
	}

	@Override
	public void create()
	{
		super.create();
		getShell().setText(Messages.getString("org.nightlabs.editor2d.ui.dialog.ConvertImageDialog.text")); //$NON-NLS-1$
	}

	public ConvertImageComposite getConvertImageComposite() {
		return convertImageComp;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		convertImageComp = new ConvertImageComposite(parent, SWT.NONE, image);
		return convertImageComp;
	}
}
