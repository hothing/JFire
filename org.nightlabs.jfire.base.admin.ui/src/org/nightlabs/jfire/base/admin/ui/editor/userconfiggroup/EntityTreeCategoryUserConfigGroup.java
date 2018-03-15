package org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.ui.IEditorInput;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.entity.tree.ActiveJDOEntityTreeCategory;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.UserConfigSetup;
import org.nightlabs.jfire.config.dao.ConfigDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class EntityTreeCategoryUserConfigGroup
extends ActiveJDOEntityTreeCategory<ConfigID, ConfigGroup> {

	public EntityTreeCategoryUserConfigGroup() {
		super();
	}

	protected class LabelProvider extends TableLabelProvider {
		public String getColumnText(Object o, int columnIndex) {
			// check for string first, so we don't need to be logged in when dsplaying a simple string
			if (o instanceof String) {
				return (String)o;
			} else if (o instanceof ConfigGroup) {
				return ((ConfigGroup)o).getName();
			} else {
//				return super.getText(o);
				return o == null ? "" : o.toString();				 //$NON-NLS-1$
			}
		}
	}

	@Override
	protected Class<ConfigGroup> getJDOObjectClass() {
		return ConfigGroup.class;
	}

	public static final String[] FETCH_GROUPS_USER_CONFIG_GROUPS = {
		FetchPlan.DEFAULT, ConfigGroup.FETCH_GROUP_THIS_CONFIG_GROUP
		};

	public static Comparator<ConfigGroup> configGroupComparator = new Comparator<ConfigGroup>()
	{
		public int compare(ConfigGroup o1, ConfigGroup o2)
		{
			int res = o1.getOrganisationID().compareTo(o2.getOrganisationID());
			if (res != 0)
				return res;

			return o1.getConfigKey().compareTo(o2.getConfigKey());
		}
	};

	@Override
	protected Collection<ConfigGroup> retrieveJDOObjects(ProgressMonitor monitor) {
		return CollectionUtil.castCollection(
				ConfigDAO.sharedInstance().getConfigs(UserConfigSetup.CONFIG_GROUP_CONFIG_TYPE_USER_CONFIG,
						FETCH_GROUPS_USER_CONFIG_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor)
				);
	}

	@Override
	protected Collection<ConfigGroup> retrieveJDOObjects(Set<ConfigID> objectIDs, ProgressMonitor monitor)
	{
		for (ConfigID configID : objectIDs) {
			if (!configID.configType.equals(UserConfigSetup.CONFIG_GROUP_CONFIG_TYPE_USER_CONFIG))
//				objectIDs.remove(configID);
				return null;
		}
		return CollectionUtil.castCollection(
				ConfigDAO.sharedInstance().getConfigs(objectIDs, FETCH_GROUPS_USER_CONFIG_GROUPS,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor)
				);
	}

	@Override
	protected void sortJDOObjects(List<ConfigGroup> objects) {
		Collections.sort(objects, configGroupComparator);
	}

	public IEditorInput createEditorInput(Object o)
	{
		ConfigGroup configGroup = (ConfigGroup) o;
		ConfigID configID = ConfigID.create(
				configGroup.getOrganisationID(),
				configGroup.getConfigKey(),
				configGroup.getConfigType());
		return new UserConfigGroupEditorInput(configID);
	}

	public ITableLabelProvider createLabelProvider() {
		return new LabelProvider();
	}

}
