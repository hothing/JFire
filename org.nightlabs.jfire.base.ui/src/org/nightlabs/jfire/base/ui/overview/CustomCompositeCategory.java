/* ********************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org              *
 * Copyright (C) 2004-2007 NightLabs - http://NightLabs.org           *
 *                                                                    *
 * This library is free software; you can redistribute it and/or      *
 * modify it under the terms of the GNU Lesser General Public         *
 * License as published by the Free Software Foundation; either       *
 * version 2.1 of the License, or (at your option) any later version. *
 *                                                                    *
 * This library is distributed in the hope that it will be useful,    *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of     *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  *
 * Lesser General Public License for more details.                    *
 *                                                                    *
 * You should have received a copy of the GNU Lesser General Public   *
 * License along with this library; if not, write to the              *
 *     Free Software Foundation, Inc.,                                *
 *     51 Franklin St, Fifth Floor,                                   *
 *     Boston, MA  02110-1301  USA                                    *
 *                                                                    *
 * Or get it online:                                                  *
 *     http://www.gnu.org/copyleft/lesser.html                        *
 **********************************************************************/
package org.nightlabs.jfire.base.ui.overview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;

/**
 * A category that will not display its items in a table, but rather will
 * ask all its entries to create a Composite for itself.
 * See {@link Entry#createComposite(Composite)}.
 * <p>
 * The custom Composites are placed inside a {@link Composite}.
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class CustomCompositeCategory extends DefaultCategory {

	/**
	 * @param categoryFactory
	 */
	public CustomCompositeCategory(CategoryFactory categoryFactory) {
		super(categoryFactory);
	}
	
	@Override
	public Composite createComposite(Composite composite) {
		XComposite wrapper = new XComposite(composite, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		for (Entry entry : getEntries()) {
			Composite comp = entry.createComposite(wrapper);
			if (comp.getLayoutData() == null) {
				comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}
		}
		return wrapper;
	}

}
