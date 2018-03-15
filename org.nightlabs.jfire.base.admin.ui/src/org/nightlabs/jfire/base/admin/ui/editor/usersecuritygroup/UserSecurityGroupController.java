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

import javax.jdo.FetchPlan;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.editor.user.UserEditor;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.dao.UserSecurityGroupDAO;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * A controller that loads a userSecurityGroup with its person.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class UserSecurityGroupController extends ActiveEntityEditorPageController<UserSecurityGroup>
{

	private static final String[] FETCH_GROUPS_USER_SECURITY_GROUP = new String[] {
		FetchPlan.DEFAULT,
		User.FETCH_GROUP_USER_LOCAL,
		User.FETCH_GROUP_PERSON,
		PropertySet.FETCH_GROUP_FULL_DATA}
	;

	private static final long serialVersionUID = -1651161683093714800L;

//	/**
//	 * LOG4J logger used by this class
//	 */
//	private static final Logger logger = Logger.getLogger(UserSecurityGroupController.class);

	/**
	 * The userSecurityGroup id.
	 */
	private UserSecurityGroupID userSecurityGroupID;

	/**
	 * Create an instance of this controller for
	 * an {@link UserEditor} and load the data.
	 */
	public UserSecurityGroupController(EntityEditor editor)
	{
		super(editor);
		this.userSecurityGroupID = (UserSecurityGroupID) ((JDOObjectEditorInput<?>)editor.getEditorInput()).getJDOObjectID();
	}

	@Override
	protected UserSecurityGroup retrieveEntity(ProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usersecuritygroup.UserSecurityGroupController.job.loadingUserSecurityGroup"), 1); //$NON-NLS-1$
		try {
			if(userSecurityGroupID != null) {
				// load userSecurityGroup with person data
				UserSecurityGroup group = UserSecurityGroupDAO.sharedInstance().getUserSecurityGroup(
						userSecurityGroupID, getEntityFetchGroups(),
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new SubProgressMonitor(monitor, 1)
				);
				monitor.worked(1);
				return group;
			}
			return null;
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.done();
		}
	}

	@Override
	protected UserSecurityGroup storeEntity(UserSecurityGroup controllerObject, ProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usersecuritygroup.UserSecurityGroupController.job.savingUserSecurityGroup"), 6); //$NON-NLS-1$
		try	{
			monitor.worked(1);
			return UserSecurityGroupDAO.sharedInstance().storeUserSecurityGroup(
					controllerObject, true, getEntityFetchGroups(),
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new SubProgressMonitor(monitor, 5)
			);
		} catch(Exception e) {
			monitor.setCanceled(true);
			throw new RuntimeException(e);
		} finally {
			monitor.done();
		}
	}

	@Override
	protected String[] getEntityFetchGroups() {
		return FETCH_GROUPS_USER_SECURITY_GROUP;
	}

	/**
	 * Get the userID.
	 * @return the userID
	 */
	public UserSecurityGroupID getUserSecurityGroupID()
	{
		return userSecurityGroupID;
	}
}
