package org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup;

import javax.jdo.FetchPlan;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.dao.ConfigDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.progress.ProgressMonitor;

public class ConfigGroupPreferencesController extends ActiveEntityEditorPageController<ConfigGroup> {
	
	private static final String[] FETCH_GROUPS = new String[] { FetchPlan.DEFAULT, ConfigGroup.FETCH_GROUP_NAME };
	
	private ConfigID configGroupID;

	public ConfigGroupPreferencesController(EntityEditor editor) {
		super(editor);
		this.configGroupID = ((JDOObjectEditorInput<ConfigID>)editor.getEditorInput()).getJDOObjectID();
	}
	
	@Override
	protected String[] getEntityFetchGroups() {
		return FETCH_GROUPS;
	}

	@Override
	protected ConfigGroup retrieveEntity(ProgressMonitor monitor) {
		return (ConfigGroup) ConfigDAO.sharedInstance().getConfig(configGroupID, getEntityFetchGroups(), -1, monitor);
	}

	@Override
	protected ConfigGroup storeEntity(ConfigGroup controllerObject, ProgressMonitor monitor) {
		return (ConfigGroup) ConfigDAO.sharedInstance().storeConfig(controllerObject, true, getEntityFetchGroups(), -1, monitor);
	}
}
