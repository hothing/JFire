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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.config.id.ConfigID;

/**
 * An editor page to display config preferences.
 * 
 * @version $Revision: 10281 $ - $Date: 2008-04-14 12:41:20 +0200 (Mo, 14 Apr 2008) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ConfigPreferencesPage extends FormPage
{
	/**
	 * The id of this page.
	 */
	public static final String ID_PAGE = ConfigPreferencesPage.class.getName();

	/**
	 * The Factory is registered to the extension-point and creates
	 * new instances of {@link ConfigPreferencesPage}.
	 */
	public static class Factory implements IEntityEditorPageFactory {
		public IFormPage createPage(FormEditor formEditor) {
			return new ConfigPreferencesPage(formEditor);
		}

		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new ConfigPreferencesController(editor);
		}
	}

	/**
	 * Create an instance of ConfigPreferencesPage.
	 * <p>
	 * This constructor is used by the entity editor
	 * page extension system.
	 * 
	 * @param editor The editor for which to create this
	 * 		form page.
	 */
	public ConfigPreferencesPage(FormEditor editor)
	{
		super(editor, ID_PAGE, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.ConfigPreferencesPage.pageTitle")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm)
	{
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		form.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.ConfigPreferencesPage.formTitle"));  //$NON-NLS-1$
		fillBody(managedForm, toolkit);
	}

	/**
	 * Layout this form page.
	 * @param managedForm The managed form
	 * @param toolkit The tookit to use
	 */
	private void fillBody(IManagedForm managedForm, FormToolkit toolkit)
	{
		Composite body = managedForm.getForm().getBody();
		GridLayout layout = new GridLayout();
		layout.marginBottom = 10;
		layout.marginTop = 5;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.numColumns = 1;
		layout.horizontalSpacing = 10;
		body.setLayout(layout);
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		if (! (getEditor() instanceof IConfigSetupEditor))
			throw new IllegalStateException(ConfigPreferencesPage.class.getName() +
					" should only be used with a Editor implementing IConfigSetupEditor"); //$NON-NLS-1$
		ConfigID configID = ((IConfigSetupEditor)getEditor()).getConfigID();
		managedForm.addPart(new ConfigPreferencesSection(this, body, configID));
	}
}
