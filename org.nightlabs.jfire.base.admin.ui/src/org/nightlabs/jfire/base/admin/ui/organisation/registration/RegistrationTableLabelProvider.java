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

package org.nightlabs.jfire.base.admin.ui.organisation.registration;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.jfire.organisation.RegistrationStatus;
import org.nightlabs.l10n.DateFormatter;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class RegistrationTableLabelProvider
	extends LabelProvider
	implements ITableLabelProvider
{

	public RegistrationTableLabelProvider()
	{
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		try {
			if (element instanceof RegistrationStatus) {
				RegistrationStatus rs = (RegistrationStatus)element;
				switch(columnIndex) {
					case 0:
						return rs.getOrganisationID();
					case 1:
						return rs.getDirection();
					case 2:
						return rs.getStatus();
					case 3:
						return DateFormatter.formatDateShortTimeHMS(rs.getOpenDT(), true);
					case 4:
						if (rs.getOpenUser() == null)
							return ""; //$NON-NLS-1$
						else
              return rs.getOpenUser().getCompleteUserID() + ((rs.getOpenUser().getName() != null)?"(" + rs.getOpenUser().getName() + ")":""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					case 5:
						if (rs.getCloseDT() == null)
							return ""; //$NON-NLS-1$
						else
							return DateFormatter.formatDateShortTimeHMS(rs.getCloseDT(), true);
					case 6:
						if (rs.getCloseUser() == null)
							return ""; //$NON-NLS-1$
						else
							return rs.getCloseUser().getCompleteUserID() + ((rs.getCloseUser().getName() != null)?"(" + rs.getCloseUser().getName() + ")":""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					default:
						return ""; //$NON-NLS-1$
				}
			}

			return element.toString();
		} catch (RuntimeException x) {
			ExceptionHandlerRegistry.asyncHandleException(x);
			throw x;
		} catch (Exception x) {
			ExceptionHandlerRegistry.asyncHandleException(x);
			throw new RuntimeException(x);
		}
	}
}
