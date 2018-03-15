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
package org.nightlabs.jfire.base.admin.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.base.admin.ui.asyncinvoke.AsyncInvokeProblemView;
import org.nightlabs.jfire.base.admin.ui.editor.SysAdminEntityTreeView;
import org.nightlabs.jfire.base.admin.ui.organisation.registration.PendingOrganisationRegistrationsView;

/**
 * Administration perspective for Users, UserGroups,
 * RoleGroups, Workstations etc.
 * 
 * @version $Revision: 16219 $ - $Date: 2009-11-27 19:41:57 +0100 (Fr, 27 Nov 2009) $
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class BaseAdminPerspective implements IPerspectiveFactory
{
	/**
	 * The perspective id.
	 */
	public static final String ID_PERSPECTIVE = BaseAdminPerspective.class.getName();

	/**
	 * Create the initial perspective layout.
	 * @param layout The initial page layout
	 */
	public void createInitialLayout(IPageLayout layout)
	{
		layout.setEditorAreaVisible(true);
		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.25f,	IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
		left.addView(SysAdminEntityTreeView.ID_VIEW);

		IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.7f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
		bottom.addView(PendingOrganisationRegistrationsView.ID_VIEW);
		bottom.addView(AsyncInvokeProblemView.ID_VIEW);
		
		layout.addShowViewShortcut(SysAdminEntityTreeView.ID_VIEW);
		layout.addShowViewShortcut(PendingOrganisationRegistrationsView.ID_VIEW);
		layout.addShowViewShortcut(AsyncInvokeProblemView.ID_VIEW);
		
		RCPUtil.addAllPerspectiveShortcuts(layout);
	}
}
