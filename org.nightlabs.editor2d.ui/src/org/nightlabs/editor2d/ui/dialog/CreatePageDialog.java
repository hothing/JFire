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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.eclipse.ui.dialog.ResizableTrayDialog;
import org.nightlabs.editor2d.ui.composite.PageComposite;
import org.nightlabs.editor2d.ui.resource.Messages;

/**
 * <p> Author: Daniel.Mazurek[AT]NightLabs[DOT]de </p>
 */
public class CreatePageDialog
extends ResizableTrayDialog
{
	public CreatePageDialog(Shell parentShell) {
		super(parentShell, Messages.RESOURCE_BUNDLE);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MIN | SWT.MAX);
	}

	protected PageComposite pageComp = null;
	public PageComposite getPageComposite() {
		return pageComp;
	}
	
	@Override
	protected Control createDialogArea(Composite parent)
	{
		return new PageComposite(parent, SWT.NONE);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("org.nightlabs.editor2d.ui.dialog.CreatePageDialog.title")); //$NON-NLS-1$
	}
}
