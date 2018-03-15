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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 ******************************************************************************/
package org.nightlabs.jfire.base.admin.ui.editor.user;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.id.UserID;

/**
 * Editor input for {@link UserEditor}s.
 * 
 * @version $Revision: 8706 $ - $Date: 2007-09-29 18:30:18 +0200 (Sa, 29 Sep 2007) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class UserEditorInput extends JDOObjectEditorInput<UserID>
{
	/**
	 * Constructor for an existing user.
	 * @param userID The user
	 */
	public UserEditorInput(UserID userID)
	{
		super(userID);
		setName(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserEditorInput.editorInputName")); //$NON-NLS-1$
	}
}
