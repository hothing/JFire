/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractInlineDataFieldComposite;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.datafield.II18nTextDataField;
import org.nightlabs.jfire.prop.structfield.TextStructField;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class TextDataFieldComposite<DataFieldType extends DataField & II18nTextDataField> extends AbstractInlineDataFieldComposite<AbstractDataFieldEditor<DataFieldType>> {

	private Text fieldText;
	private ModifyListener modifyListener;

	public TextDataFieldComposite(AbstractDataFieldEditor<DataFieldType> editor, Composite parent, int style, ModifyListener modListener, GridLayout gl) {
		super(parent, style, editor);
		if (!(parent.getLayout() instanceof GridLayout))
			throw new IllegalArgumentException("Parent should have a GridLayout!"); //$NON-NLS-1$

		fieldText = new Text(this, createTextStyle());
		fieldText.setEnabled(true);
		fieldText.setLayoutData(createTextLayoutData());
		this.modifyListener = modListener;
		fieldText.addModifyListener(modifyListener);

		if (gl != null)
			setLayout(gl);
	}

	/**
	 * Assumes to have a parent composite with GridLaout and
	 * adds it own GridData.
	 */
	public TextDataFieldComposite(AbstractDataFieldEditor<DataFieldType> editor, Composite parent, int style, ModifyListener modListener) {
		this(editor, parent, style, modListener, null);
	}

	protected int createTextStyle() {
		if (isMultiLine())  {
			return getTextBorderStyle() | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL;
		} else {
			 return getTextBorderStyle();
		}

	}

	protected Object createTextLayoutData() {
		if (isMultiLine()) {
			GridData gd = new GridData(GridData.FILL_BOTH);
			gd.minimumHeight = RCPUtil.getFontHeight(this) * getLineCount();
			return gd;
		} else {
			GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			return textData;
		}
	}

	protected int getTextBorderStyle() {
		return getBorderStyle();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractInlineDataFieldComposite#_refresh()
	 */
	@Override
	public void _refresh() {
		if (getEditor().getDataField().getI18nText().getText() == null)
			fieldText.setText(""); //$NON-NLS-1$
		else
			fieldText.setText(getEditor().getDataField().getI18nText().getText());
	}

	@Override
	protected void handleManagedBy(String managedBy) {
//		super.handleManagedBy(managedBy);
		// The super method disables the whole composite thus making it impossible to select (and copy to clipboard) some text.
		// Thus, we make the text read-only.
		fieldText.setEditable(managedBy == null);
		if (managedBy != null)
			setToolTipText(String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.TextDataFieldComposite.managedBy.tooltip"), managedBy)); //$NON-NLS-1$
		else
			setToolTipText(null);
	}

	private boolean isMultiLine() {
		StructField<?> structField = getEditor().getStructField();
		if (structField instanceof TextStructField) {
			return ((TextStructField) structField).getLineCount() > 1;
		}
		return false;
	}

	private int getLineCount() {
		StructField<?> structField = getEditor().getStructField();
		if (structField instanceof TextStructField) {
			return Math.max(((TextStructField) structField).getLineCount(), 1);
		}
		return 1;
	}

	public String getText() {
		return fieldText.getText();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	@Override
	public void dispose() {
		fieldText.removeModifyListener(modifyListener);
		super.dispose();
	}

//	@Override
//	public Point computeSize(int hint, int hint2, boolean changed) {
//		String text = fieldText.getText();
//		try {
//			return super.computeSize(hint, hint2, changed);
//		} finally {
//			fieldText.setText(text);
//		}
//	}
//

}
