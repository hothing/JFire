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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.ui.IEditorInput;
import org.nightlabs.base.ui.table.TableLabelProvider;
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
 * Entity tree category for {@link User}s.
 *
 * @version $Revision: 11672 $ - $Date: 2008-08-07 03:41:43 +0200 (Do, 07 Aug 2008) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author marco schulze - marco at nightlabs dot de
 */
public class EntityTreeCategoryUser
extends ActiveJDOEntityTreeCategory<UserID, User>
{
	protected class LabelProvider extends TableLabelProvider {

		public String getColumnText(Object o, int columnIndex) {
			// check for string first, so we don't need to be logged in when displaying a simple string
			if(o instanceof String) {
				return (String)o;
			} else if(o instanceof User) {
				final User user = (User) o;
//				return UserUtil.getUserDisplayName((User)o);
				if (user.getName() != null && ! "".equals(user.getName())) //$NON-NLS-1$
					return user.getName();

				return user.getUserID();
			} else {
				return super.getText(o);
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
	 * We override the default implementation in order to suppress subclasses
	 * of {@link User} (i.e. <code>UserGroup</code> instances) and to
	 * filter for the correct user-type on the server.
	 */
	@Override
	protected IJDOLifecycleListenerFilter createJDOLifecycleListenerFilter()
	{
		return new UserLifecycleListenerFilter(
				User.USER_TYPE_USER, new JDOLifecycleState[] { JDOLifecycleState.NEW });
	}

	public static final String[] FETCH_GROUPS_USER = {
		User.FETCH_GROUP_NAME,
		FetchPlan.DEFAULT
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
		Collection<User> users = UserDAO.sharedInstance().getUsers(
				IDGenerator.getOrganisationID(),
				Collections.singleton(User.USER_TYPE_USER),
				FETCH_GROUPS_USER,
				1,
				monitor
		);

		List<User> res = new ArrayList<User>(users.size());

		for (User user : users) {
			res.add(user);
		}

		return res;
	}

	@Override
	protected void sortJDOObjects(List<User> users)
	{
		Collections.sort(users); // User implements Comparable - no Comparator needed
	}
}
