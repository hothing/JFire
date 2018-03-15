/* *****************************************************************************
 * org.nightlabs.base.ui - NightLabs Eclipse utilities                            *
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

package org.nightlabs.base.ui.celleditor;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Subclass of {@link TextCellEditor} which implements the {@link IReadOnlyCellEditor} interface.  
 * @author Daniel.Mazurek <at> NightLabs <dot> de
 */
public class XTextCellEditor
extends TextCellEditor
implements IReadOnlyCellEditor
{
	private boolean readOnly = false;
	
	public XTextCellEditor() {
		super();
	}

	/**
	 * @param parent
	 */
	public XTextCellEditor(Composite parent) {
		super(parent);
	}

	/**
	 * @param parent
	 * @param style
	 */
	public XTextCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * @param parent
	 * @param style
	 * @param readOnly
	 */
	public XTextCellEditor(Composite parent, int style, boolean readOnly)
	{
		super(parent, getCellEditorStyle(style, readOnly));
		this.readOnly = readOnly;
	}
	
	private static int getCellEditorStyle(int style, boolean readOnly) {
		if (readOnly)
			return style | SWT.READ_ONLY;
		return style;
	}

	/**
	 * The <code>TextCellEditor</code> implementation of
	 * this <code>CellEditor</code> framework method accepts
	 * a text string (type <code>String</code>).
	 *
	 * @param value a text string (type <code>String</code>)
	 */
	@Override
	protected void doSetValue(Object value)
	{
		super.doSetValue(value);
	}

	public boolean isReadOnly() {
		return readOnly;
	}
	
	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
		getControl().setEnabled(readOnly);
	}
}
