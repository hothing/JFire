/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.config;

import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jfire.base.ui.config.AbstractConfigModuleController;
import org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutConfigModule;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntry;

/**
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class PropertySetFieldBasedEditLayoutConfigModuleController extends
		AbstractConfigModuleController {

	/**
	 * @param preferencePage
	 */
	public PropertySetFieldBasedEditLayoutConfigModuleController(
			AbstractConfigModulePreferencePage preferencePage) {
		super(preferencePage);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.config.IConfigModuleController#getConfigModuleClass()
	 */
	@Override
	public Class<? extends ConfigModule> getConfigModuleClass() {
		return PropertySetFieldBasedEditLayoutConfigModule.class;
	}

	private static final Set<String> CONFIG_MODULE_FETCH_GROUPS = new HashSet<String>();

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.config.IConfigModuleController#getConfigModuleFetchGroups()
	 */
	@Override
	public Set<String> getConfigModuleFetchGroups() {
		if (CONFIG_MODULE_FETCH_GROUPS.isEmpty()) {
			CONFIG_MODULE_FETCH_GROUPS.addAll(getCommonConfigModuleFetchGroups());
			CONFIG_MODULE_FETCH_GROUPS.add(PropertySetFieldBasedEditLayoutConfigModule.FETCH_GROUP_GRID_LAYOUT);
			CONFIG_MODULE_FETCH_GROUPS.add(PropertySetFieldBasedEditLayoutConfigModule.FETCH_GROUP_EDIT_LAYOUT_ENTRIES);
			CONFIG_MODULE_FETCH_GROUPS.add(PropertySetFieldBasedEditLayoutEntry.FETCH_GROUP_GRID_DATA);
			CONFIG_MODULE_FETCH_GROUPS.add(PropertySetFieldBasedEditLayoutEntry.FETCH_GROUP_STRUCT_FIELD_ID);
		}
		return CONFIG_MODULE_FETCH_GROUPS;
	}

}
