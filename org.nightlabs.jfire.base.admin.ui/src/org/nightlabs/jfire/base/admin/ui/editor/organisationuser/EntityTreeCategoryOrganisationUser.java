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
package org.nightlabs.jfire.base.admin.ui.editor.organisationuser;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.ui.IEditorInput;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jfire.base.admin.ui.editor.user.UserEditorInput;
import org.nightlabs.jfire.base.ui.entity.tree.ActiveJDOEntityTreeCategory;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLifecycleListenerFilter;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Entity tree category for those {@link User}s that are organisations in cooperation with us.
 *
 * @version $Revision: 4838 $ - $Date: 2006-10-31 13:18:46 +0000 (Tue, 31 Oct 2006) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author marco schulze - marco at nightlabs dot de
 */
public class EntityTreeCategoryOrganisationUser
extends ActiveJDOEntityTreeCategory<UserID, User>
{
	protected class LabelProvider extends TableLabelProvider {
		public String getColumnText(Object o, int columnIndex) {
			// check for string first, so we don't need to be logged in when dsplaying a simple string
			if(o instanceof String) {
				return (String)o;
			} else if(o instanceof User) {
				return ((User)o).getName();
//				return UserUtil.getUserDisplayName((User)o);
			} else {
				return ""; //$NON-NLS-1$
			}
		}
	}

	public IEditorInput createEditorInput(Object o)
	{
		User user = (User)o;
		UserID userID = UserID.create(user.getOrganisationID(), user.getUserID());
		return new UserEditorInput(userID);
	}

	public ITableLabelProvider createLabelProvider() {
		return new LabelProvider();
	}

	@Override
	protected Class<User> getJDOObjectClass()
	{
		return User.class;
	}

	/**
	 * We override the default implementation in order to supress subclasses
	 * of {@link User} (i.e. <code>UserGroup</code> instances) and to
	 * filter for the correct user-type on the server.
	 */
	@Override
	protected IJDOLifecycleListenerFilter createJDOLifecycleListenerFilter()
	{
		return new UserLifecycleListenerFilter(
				User.USER_TYPE_ORGANISATION, new JDOLifecycleState[] { JDOLifecycleState.NEW });
	}

	public static final String[] FETCH_GROUPS_USER = {
		FetchPlan.DEFAULT,
		User.FETCH_GROUP_NAME,
	};

	@Override
	protected Collection<User> retrieveJDOObjects(Set<UserID> userIDs, ProgressMonitor monitor)
	{
		return UserDAO.sharedInstance().getUsers(userIDs,
			FETCH_GROUPS_USER,
			1,
			monitor);
	}

	@Override
	protected Collection<User> retrieveJDOObjects(ProgressMonitor monitor)
	{
		return UserDAO.sharedInstance().getUsers(
				IDGenerator.getOrganisationID(),
				Collections.singleton(User.USER_TYPE_ORGANISATION),
				FETCH_GROUPS_USER,
				1,
				monitor);
	}

	@Override
	protected void sortJDOObjects(List<User> users)
	{
		Collections.sort(users); // User implements Comparable - no Comparator needed
	}
}
