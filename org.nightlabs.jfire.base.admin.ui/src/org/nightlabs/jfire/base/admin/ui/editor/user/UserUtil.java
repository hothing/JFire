/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2006 NightLabs - http://NightLabs.org                    *
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
 ******************************************************************************/
package org.nightlabs.jfire.base.admin.ui.editor.user;

import org.nightlabs.base.ui.editor.RestorableSectionPart;
import org.nightlabs.base.ui.notification.IDirtyStateManager;

/**
 * @version $Revision: 14484 $ - $Date: 2009-05-08 15:42:31 +0200 (Fr, 08 Mai 2009) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class UserUtil
{
//	/**
//	 * LOG4J logger used by this class
//	 */
//	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
//			.getLogger(UserUtil.class);
//
//	/**
//	 * Get a display name for a user.
//	 * @param user The user to get the name for
//	 * @return The user's display name.
//	 */
//	public static String getUserDisplayName(User user)
//	{
//		if(user == null)
//			return null;
//		Person person = user.getPerson();
//		String displayName = null;
//		if(person != null) {
//			try {
//				displayName = person.getDisplayName();
//			} catch(JDODetachedFieldAccessException e) {
//				// field is not detached...
//				logger.warn("Field Person.personDisplayName is not detached. Cannot create full display name.", e); //$NON-NLS-1$
//				displayName = null;
//			}
//		}
//		if(displayName != null && !"".equals(displayName)) //$NON-NLS-1$
//			return String.format(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserUtil.userFullDisplayName"), user.getUserID(), displayName); //$NON-NLS-1$
//		else
//			return user.getUserID();
//	}

	/**
	 * Returns an {@link IDirtyStateManager} that delegates markDirty/markUndirty events to the given section.
	 * @param section The section to which the events should be delegated.
	 * @return an {@link IDirtyStateManager} that delegates markDirty/markUndirty events to the given section.
	 */
	public static IDirtyStateManager getSectionDirtyStateManager(final RestorableSectionPart section) {
		return new IDirtyStateManager() {
			public boolean isDirty() {
				throw new RuntimeException("isDirty() must not be called."); //$NON-NLS-1$
			}
			public void markDirty() {
				section.markDirty();
			}
			public void markUndirty() {
				section.markUndirty();
			}
		};
	}
}
