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
package org.nightlabs.base.ui.search;

import java.util.ArrayList;
import java.util.Collection;

import org.nightlabs.base.ui.notification.SelectionManager;
import org.nightlabs.notification.NotificationEvent;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class AbstractSelectionZoneActionHandler
extends AbstractSearchResultActionHandler
{
	public void run()
	{
		Collection<?> selectedObjects = getSearchResultProvider().getSelectedObjects();
		Collection<Class<?>> subjectClassesToClear = new ArrayList<Class<?>>();
		subjectClassesToClear.add(getSearchResultProvider().getFactory().getResultTypeClass());
		if (selectedObjects != null) {
			SelectionManager.sharedInstance().notify(new NotificationEvent(
					AbstractSelectionZoneActionHandler.this,
					getSelectionZone(),
					selectedObjects,
					subjectClassesToClear));
		}
	}

	public abstract String getSelectionZone();
}
