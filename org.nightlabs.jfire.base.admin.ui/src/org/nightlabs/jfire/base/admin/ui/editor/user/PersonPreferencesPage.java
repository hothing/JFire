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
import org.nightlabs.jfire.base.ui.person.edit.blockbased.PersonBlockBasedEditorSection;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.BlockBasedEditorSection;
import org.nightlabs.jfire.security.User;

/**
 * An editor page for person preferences.
 *
 * @version $Revision: 13893 $ - $Date: 2009-04-13 17:58:49 +0200 (Mo, 13 Apr 2009) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class PersonPreferencesPage extends EntityEditorPageWithProgress
{
	/**
	 * The id of this page.
	 */
	public static final String ID_PAGE = PersonPreferencesPage.class.getName();

	private BlockBasedEditorSection userPropertiesSection;

	/**
	 * The Factory is registered to the extension-point and creates
	 * new instances of {@link PersonPreferencesPage}.
	 */
	public static class Factory implements IEntityEditorPageFactory {

		public IFormPage createPage(FormEditor formEditor) {
			return new PersonPreferencesPage(formEditor);
		}
		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new PersonPreferencesController(editor);
		}
	}

	/**
	 * Create an instance of PersonPreferencesPage.
	 * <p>
	 * This constructor is used by the entity editor
	 * page extension system.
	 *
	 * @param editor The editor for which to create this
	 * 		form page.
	 */
	public PersonPreferencesPage(FormEditor editor)
	{
		super(editor, ID_PAGE, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.PersonPreferencesPage.pageTitle")); //$NON-NLS-1$
	}

	@Override
	protected void addSections(Composite parent) {
		userPropertiesSection = new PersonBlockBasedEditorSection(this, parent, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.PersonPreferencesPage.sectionTitle")); //$NON-NLS-1$

		getManagedForm().addPart(userPropertiesSection);
	}

	@Override
	protected void handleControllerObjectModified(EntityEditorPageControllerModifyEvent modifyEvent) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				PersonPreferencesController controller = (PersonPreferencesController)getPageController();
				User user = controller.getUser();
//				if (user.getPerson() == null)
//					user.setPerson(new Person(user.getOrganisationID(), PropertySet.TEMPORARY_PROP_ID));

				userPropertiesSection.setPropertySet(user.getPerson());
				updateGui(controller);
				switchToContent();
			}
		});
	}

	/**
	 * This method is meant for extendors if this class to be able to update their GUI when {@link #asyncCallback()}
	 * is called. The default implementation does nothing.
	 */
	protected void updateGui(PersonPreferencesController controller) {

	}

	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.PersonPreferencesPage.pageFormTitle"); //$NON-NLS-1$
	}

	BlockBasedEditorSection getUserPropertiesSection() {
		return userPropertiesSection;
	}
}
