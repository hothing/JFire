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
package org.nightlabs.jfire.base.admin.ui.editor.localorganisation;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageControllerModifyEvent;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.person.edit.blockbased.PersonBlockBasedEditorSection;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.BlockBasedEditorSection;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;

/**
 * An {@link EntityEditorPageWithProgress} that shows an {@link BlockBasedEditorSection}
 * where the user can edit the {@link Person} of an {@link Organisation} inside an
 * {@link LocalOrganisationEditor}.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class LocalOrganisationPropertiesPage extends EntityEditorPageWithProgress
{
	/**
	 * The id of this page.
	 */
	public static final String ID_PAGE = LocalOrganisationPropertiesPage.class.getName();
	/**
	 * The editor section.
	 */
	private BlockBasedEditorSection organisationPropertiesSection;

	/**
	 * The Factory is registered to the extension-point and creates
	 * new instances of {@link LocalOrganisationPropertiesPage}.
	 */
	public static class Factory implements IEntityEditorPageFactory {

		public IFormPage createPage(FormEditor formEditor) {
			return new LocalOrganisationPropertiesPage(formEditor);
		}
		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new LocalOrganisationPropertiesController(editor);
		}
	}

	/**
	 * Create an instance of LocalOrganisationPropertiesPage.
	 * <p>
	 * This constructor is used by the entity editor
	 * page extension system.
	 *
	 * @param editor The editor for which to create this
	 * 		form page.
	 */
	public LocalOrganisationPropertiesPage(FormEditor editor)
	{
		super(editor, ID_PAGE, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.localorganisation.LocalOrganisationPropertiesPage.pageTitle")); //$NON-NLS-1$
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress#addSections(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void addSections(Composite parent) {
		organisationPropertiesSection = new PersonBlockBasedEditorSection(
				this, parent,
				Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.localorganisation.LocalOrganisationPropertiesPage.sectionTitle")); //$NON-NLS-1$
		createDescriptionControl(organisationPropertiesSection.getSection(), getManagedForm().getToolkit());

		getManagedForm().addPart(organisationPropertiesSection);

		if (getPageController().isLoaded()) {
			setControllerObject();
		}
	}

	private void createDescriptionControl(Section section, FormToolkit toolkit)
	{
		FormText text = toolkit.createFormText(section, true);
		text.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.localorganisation.LocalOrganisationPropertiesPage.sectionText"), false, false); //$NON-NLS-1$
		section.setDescriptionControl(text);
	}

	/**
	 * Sets the controllers current object ({@link Organisation}) to the ui.
	 */
	private void setControllerObject() {
		final LocalOrganisationPropertiesController controller = (LocalOrganisationPropertiesController) getPageController();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (organisationPropertiesSection != null && !organisationPropertiesSection.getSection().isDisposed())
				organisationPropertiesSection.setPropertySet(controller.getControllerObject().getPerson());
				switchToContent();
			}
		});
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress#handleControllerObjectModified(org.nightlabs.base.ui.entity.editor.EntityEditorPageControllerModifyEvent)
	 */
	@Override
	protected void handleControllerObjectModified(EntityEditorPageControllerModifyEvent modifyEvent) {
		setControllerObject();
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress#getPageFormTitle()
	 */
	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.localorganisation.LocalOrganisationPropertiesPage.pageFormTitle"); //$NON-NLS-1$
	}
}
