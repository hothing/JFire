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
package org.nightlabs.jfire.base.admin.ui.editor.usersecuritygroup;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.ui.IEditorInput;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.entity.tree.ActiveJDOEntityTreeCategory;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.SimpleLifecycleListenerFilter;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.dao.UserSecurityGroupDAO;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Entity tree category for {@link UserSecurityGroup}s.
 *
 * @version $Revision: 10665 $ - $Date: 2008-06-03 03:47:08 +0200 (Di, 03 Jun 2008) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class EntityTreeCategoryUserSecurityGroup
extends ActiveJDOEntityTreeCategory<UserSecurityGroupID, UserSecurityGroup>
{
	protected class LabelProvider extends TableLabelProvider {
		public String getColumnText(Object o, int arg1) {
			if (o instanceof String)
				return (String)o;
			else if (o instanceof UserSecurityGroup) {
				final UserSecurityGroup group = (UserSecurityGroup)o;
				return group.getName() == null || "".equals(group.getName()) ? group.getUserSecurityGroupID() : group.getName(); //$NON-NLS-1$
			}
			else
				return o.toString();
		}
	}

	public IEditorInput createEditorInput(Object o)
	{
		UserSecurityGroup userSecurityGroup = (UserSecurityGroup)o;
		UserSecurityGroupID userSecurityGroupID = UserSecurityGroupID.create(userSecurityGroup.getOrganisationID(), userSecurityGroup.getUserSecurityGroupID());
		return new UserSecurityGroupEditorInput(userSecurityGroupID);
	}

	public ITableLabelProvider createLabelProvider() {
		return new LabelProvider();
	}

	@Override
	protected Class<UserSecurityGroup> getJDOObjectClass()
	{
		return UserSecurityGroup.class;
	}

	/**
	 * We override the default implementation in order to
	 * filter for the correct user-type (i.e. solely user-group)
	 * on the server.
	 */
	@Override
	protected IJDOLifecycleListenerFilter createJDOLifecycleListenerFilter()
	{
		return new SimpleLifecycleListenerFilter(UserSecurityGroup.class, false, new JDOLifecycleState[] { JDOLifecycleState.NEW });
	}

	@Override
	protected Collection<UserSecurityGroup> retrieveJDOObjects(Set<UserSecurityGroupID> objectIDs, ProgressMonitor monitor)
	{
		return UserSecurityGroupDAO.sharedInstance().getUserSecurityGroups(
				objectIDs,
				FETCH_GROUPS_USER_GROUP,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				monitor
		);
	}

	private String[] FETCH_GROUPS_USER_GROUP = {
			javax.jdo.FetchPlan.DEFAULT,
			UserSecurityGroup.FETCH_GROUP_NAME
	};

	@Override
	protected Collection<UserSecurityGroup> retrieveJDOObjects(ProgressMonitor monitor)
	{
		return UserSecurityGroupDAO.sharedInstance().getUserSecurityGroups(
						FETCH_GROUPS_USER_GROUP,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						monitor
		);
	}

	@Override
	protected void sortJDOObjects(List<UserSecurityGroup> objects)
	{
		Collections.sort(objects, new Comparator<UserSecurityGroup>() {
			@Override
			public int compare(UserSecurityGroup o1, UserSecurityGroup o2) {
				String n1 = o1.getName() == null || "".equals(o1.getName()) ? o1.getUserSecurityGroupID() : o1.getName(); //$NON-NLS-1$
				String n2 = o2.getName() == null || "".equals(o2.getName()) ? o2.getUserSecurityGroupID() : o2.getName(); //$NON-NLS-1$
				return n1.compareTo(n2);
			}
		});
	}
}
