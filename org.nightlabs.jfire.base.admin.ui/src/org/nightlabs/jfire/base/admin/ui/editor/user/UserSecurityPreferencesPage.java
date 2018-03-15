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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageControllerModifyEvent;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;

/**
 * An editor page for security related user stuff, i.e. user
 * groups and role groups.
 * 
 * @version $Revision: 12476 $ - $Date: 2008-10-20 16:48:53 +0200 (Mo, 20 Okt 2008) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Niklas Schiffler <nick@nightlabs.de>
 */
public class UserSecurityPreferencesPage extends EntityEditorPageWithProgress
{
	/**
	 * The id of this page.
	 */
	public static final String ID_PAGE = UserSecurityPreferencesPage.class.getName();
	
	/**
	 * The Factory is registered to the extension-point and creates
	 * new instances of {@link UserSecurityPreferencesPage}.
	 */
	public static class Factory implements IEntityEditorPageFactory {

		public IFormPage createPage(FormEditor formEditor) {
			return new UserSecurityPreferencesPage(formEditor);
		}
		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new UserSecurityPreferencesController(editor);
		}
		
	}
	
	UserSecurityGroupsSection userGroupsSection;
	RoleGroupsSection roleGroupsSection;

	/**
	 * Create an instance of SecurityPreferencesPage.
	 * <p>
	 * This constructor is used by the entity editor
	 * page extension system.
	 * 
	 * @param editor The editor for which to create this
	 * 		form page.
	 */
	public UserSecurityPreferencesPage(FormEditor editor)
	{
		super(editor, ID_PAGE, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.SecurityPreferencesPage.pageTitle")); //$NON-NLS-1$
	}

	@Override
	protected void addSections(Composite parent) {
		userGroupsSection = new UserSecurityGroupsSection(this, parent);
 		getManagedForm().addPart(userGroupsSection);
 		roleGroupsSection = new RoleGroupsSection(this, parent, true);
 		getManagedForm().addPart(roleGroupsSection);
	}

	@Override
	protected void handleControllerObjectModified(EntityEditorPageControllerModifyEvent modifyEvent) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				UserSecurityPreferencesModel userModel = ((UserSecurityPreferencesController)getPageController()).getUserModel();
				RoleGroupSecurityPreferencesModel rgModel = ((UserSecurityPreferencesController)getPageController()).getRoleGroupModel();
				userGroupsSection.setModel(userModel);
				roleGroupsSection.setModel(rgModel);
				switchToContent();
			}
		});
	}
	
	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.SecurityPreferencesPage.pageFormTitle"); //$NON-NLS-1$
	}
}
