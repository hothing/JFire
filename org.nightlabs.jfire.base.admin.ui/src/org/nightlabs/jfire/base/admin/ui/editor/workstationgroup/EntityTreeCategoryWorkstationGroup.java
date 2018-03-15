package org.nightlabs.jfire.base.admin.ui.editor.workstationgroup;

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
import org.nightlabs.jfire.config.WorkstationConfigSetup;
import org.nightlabs.jfire.config.dao.ConfigDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

public class EntityTreeCategoryWorkstationGroup
extends ActiveJDOEntityTreeCategory<ConfigID, ConfigGroup>
{

	protected class LabelProvider extends TableLabelProvider {
		public String getColumnText(Object o, int columnIndex) {
			// check for string first, so we don't need to be logged in when dsplaying a simple string
			if (o instanceof String) {
				return (String)o;
			} else if (o instanceof ConfigGroup) {
				return ((ConfigGroup)o).getName();
			} else {
//				return super.getText(o);
//				return org.eclipse.jface.viewers.LabelProvider.this.getText(o);
				return o == null ? "" : o.toString(); //$NON-NLS-1$
			}
		}
	}

	@Override
	protected Class<ConfigGroup> getJDOObjectClass() {
		return ConfigGroup.class;
	}

	public static final String[] FETCH_GROUPS_WORKSTATIONGROUP = {
		FetchPlan.DEFAULT, ConfigGroup.FETCH_GROUP_THIS_CONFIG_GROUP
		};

	@Override
	protected Collection<ConfigGroup> retrieveJDOObjects(Set<ConfigID> configIDs, ProgressMonitor monitor)
	{
		for (ConfigID configID : configIDs) {
			if (!configID.configType.equals(WorkstationConfigSetup.CONFIG_GROUP_CONFIG_TYPE_WORKSTATION_CONFIG))
//				objectIDs.remove(configID);
				return null;
		}
		return CollectionUtil.castCollection(
				ConfigDAO.sharedInstance().getConfigs(configIDs, FETCH_GROUPS_WORKSTATIONGROUP,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor)
				);
	}

	@Override
	protected Collection<ConfigGroup> retrieveJDOObjects(ProgressMonitor monitor) {
		return CollectionUtil.castCollection(
				ConfigDAO.sharedInstance().getConfigs(WorkstationConfigSetup.CONFIG_GROUP_CONFIG_TYPE_WORKSTATION_CONFIG,
											FETCH_GROUPS_WORKSTATIONGROUP, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor)
				);
	}

	@Override
	protected void sortJDOObjects(List<ConfigGroup> workstationGroups) {
		Collections.sort(workstationGroups, new Comparator<ConfigGroup>() {
			public int compare(ConfigGroup o1, ConfigGroup o2) {
				int res = o1.getOrganisationID().compareTo(o2.getOrganisationID());
				if (res != 0)
					return res;

				return o1.getConfigKey().compareTo(o2.getConfigKey());
			}
		});
	}

	public IEditorInput createEditorInput(Object o) {
		if (o == null)
			return null;
		ConfigGroup configGroup = (ConfigGroup) o;
		ConfigID configID = ConfigID.create(configGroup.getOrganisationID(), configGroup.getConfigKey(),
																				configGroup.getConfigType());
		return new WorkstationGroupEditorInput(configID);
	}

	public ITableLabelProvider createLabelProvider() {
		return new LabelProvider();
	}

}
