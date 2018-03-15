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

import java.util.Set;

import org.apache.log4j.Logger;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Simple controller that will not load anything
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ConfigPreferencesController extends EntityEditorPageController
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ConfigPreferencesController.class);

	/**
	 * The user editor.
	 */
	private EntityEditor editor;
	
	private Set<ConfigModule> dirtyConfigModules;
	
	private Set<AbstractConfigModulePreferencePage> pagesToStore;

	public void setPagesToStore(Set<AbstractConfigModulePreferencePage> pagesToStore) {
		this.pagesToStore = pagesToStore;
	}

	/**
	 * Create an instance of this controller for
	 * an {@link UserEditor} and load the data.
	 */
	public ConfigPreferencesController(EntityEditor editor)
	{
		super(editor);
		this.editor = editor;
	}

	/**
	 * Nothing to do by now
	 * @param monitor The progress monitor to use.
	 */
	public void doLoad(ProgressMonitor monitor)
	{
	}

	/**
	 * Save the user data.
	 * @param monitor The progress monitor to use.
	 */
	@Override
	public boolean doSave(ProgressMonitor monitor)
	{
		if (pagesToStore == null || pagesToStore.isEmpty()) {
			logger.debug("No config modules dirty, saving nothing."); //$NON-NLS-1$
			dirtyConfigModules = null;
			return true;
		}
		
		for (AbstractConfigModulePreferencePage page : pagesToStore) {
			page.storeConfigModule(true);
		}
		return true;
	}
		
	/**
	 * Get the editor.
	 * @return the editor
	 */
	public EntityEditor getEditor()
	{
		return editor;
	}

	public Set<ConfigModule> getDirtyConfigModules() {
		return dirtyConfigModules;
	}

	public void setDirtyConfigModules(Set<ConfigModule> dirtyConfigModules) {
		this.dirtyConfigModules = dirtyConfigModules;
	}
	
}
