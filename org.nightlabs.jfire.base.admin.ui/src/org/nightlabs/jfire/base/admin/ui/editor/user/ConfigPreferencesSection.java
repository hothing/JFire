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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.editor.RestorableSectionPart;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorController;
import org.nightlabs.base.ui.entity.editor.EntityEditorUtil;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.config.ConfigPreferencesEditComposite2;
import org.nightlabs.jfire.base.ui.config.IConfigModuleChangedListener;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.id.ConfigID;

/**
 * An editor page section to display config preferences.
 * @version $Revision: 11140 $ - $Date: 2008-06-27 18:09:13 +0200 (Fr, 27 Jun 2008) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ConfigPreferencesSection
extends RestorableSectionPart
implements IConfigModuleChangedListener
{
	/**
	 * The editor userID.
	 */
	private ConfigID configID;

	/**
	 * The config editor composite.
	 */
	ConfigPreferencesEditComposite2 configPreferencesEditComposite;
	
	EntityEditorController controller;
	
	FormPage page;

	/**
	 * Create an instance of ConfigPreferencesSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public ConfigPreferencesSection(FormPage page, Composite parent, ConfigID configID)
	{
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		this.page = page;
		controller = ((EntityEditor)page.getEditor()).getController();
		this.configID = configID;
		createClient(getSection(), page.getEditor().getToolkit());
	}

	/**
	 * Create the content for this section.
	 * @param section The section to fill
	 * @param toolkit The toolkit to use
	 */
	protected void createClient(Section section, FormToolkit toolkit)
	{
		section.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.ConfigPreferencesSection.sectionTitle")); //$NON-NLS-1$
		section.setExpanded(true);
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 1);

		configPreferencesEditComposite = new ConfigPreferencesEditComposite2(container, SWT.NONE, true);
		configPreferencesEditComposite.addConfigModuleChangedListener(this);
		configPreferencesEditComposite.setCurrentConfigID(configID);
		configPreferencesEditComposite.updatePreferencesComposite();
	}

	@Override
	public void commit(boolean onSave) {
		ConfigPreferencesController prefController = (ConfigPreferencesController) controller.getPageController(page);
		if (prefController != null) {
			prefController.setPagesToStore(configPreferencesEditComposite.getInvolvedPages());
//			.setDirtyConfigModules(configPreferencesEditComposite.getDirtyConfigModules());
		}
		super.commit(onSave);
		configPreferencesEditComposite.clear();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#dispose()
	 */
	@Override
	public void dispose()
	{
		if(configPreferencesEditComposite != null)
			configPreferencesEditComposite.removeConfigModuleChangedListener(this);
		super.dispose();
	}

	public void configModuleChanged(ConfigModule configModule) {
		// we need to call now the markDirty() in the page controller as well
		// as the upper level will check this in isDirty().
		((EntityEditor)page.getEditor()).getController().getPageController(page).markDirty();
		// call the markDirty of the RestorableSectionPart super-class, 
		// which is not part of the EntityEditor framework but notifies the Editor ;-)
		markDirty(); 
	}
}
