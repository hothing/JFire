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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.base.ui.notification.NotificationAdapterJob;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupSecurityPreferencesModel;
import org.nightlabs.jfire.base.admin.ui.editor.user.UserEditor;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorizedObject;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupSetCarrier;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.dao.AuthorityDAO;
import org.nightlabs.jfire.security.dao.RoleGroupDAO;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.dao.UserSecurityGroupDAO;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.Util;

/**
 * Controller class for the security preferences of a user.
 *
 * @version $Revision: 4472 $ - $Date: 2006-08-28 20:21:33 +0000 (Mon, 28 Aug 2006) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Niklas Schiffler <nick@nightlabs.de>
 */
public class GroupSecurityPreferencesController extends EntityEditorPageController
{
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(GroupSecurityPreferencesController.class);

	/**
	 * The user id.
	 */
	UserSecurityGroupID userSecurityGroupID;

	/**
	 * The user editor.
	 */
	EntityEditor editor;

	/**
	 * The editor usergroup model.
	 */
	GroupSecurityPreferencesModel userGroupModel;

	/**
	 * The editor rolegroup model.
	 */
	RoleGroupSecurityPreferencesModel roleGroupModel;

	/**
	 * Create an instance of this controller for
	 * an {@link UserEditor} and load the data.
	 */
	public GroupSecurityPreferencesController(EntityEditor editor)
	{
		super(editor, true);
		this.userSecurityGroupID = ((UserSecurityGroupEditorInput)editor.getEditorInput()).getJDOObjectID();
		this.editor = editor;
		this.userGroupModel = new GroupSecurityPreferencesModel();
		this.roleGroupModel = new RoleGroupSecurityPreferencesModel();
		JDOLifecycleManager.sharedInstance().addNotificationListener(UserSecurityGroup.class, userGroupChangedListener);
	}

	@Override
	public void dispose()
	{
		JDOLifecycleManager.sharedInstance().removeNotificationListener(UserSecurityGroup.class, userGroupChangedListener);
		super.dispose();
	}

	private NotificationListener userGroupChangedListener = new NotificationAdapterJob(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usersecuritygroup.GroupSecurityPreferencesController.loadingChangedGroup")) //$NON-NLS-1$
	{
		public void notify(NotificationEvent notificationEvent) {
			for (Object o : notificationEvent.getSubjects()) {
				if (o == null)
					continue;

				DirtyObjectID dirtyObjectID = (DirtyObjectID) o;
				if (Util.equals(dirtyObjectID.getObjectID(), userSecurityGroupID)) {
					doLoad(getProgressMonitor());
					break;
				}
			}
		}
	};

	/**
	 * Load the usergroup data and users.
	 * @param _monitor The progress monitor to use.
	 */
	@Override
	public void doLoad(ProgressMonitor monitor)
	{
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usersecuritygroup.SecurityPreferencesController.loadingUsers"), 100); //$NON-NLS-1$
		try {
			if(userSecurityGroupID != null) {
				UserSecurityGroup group = UserSecurityGroupDAO.sharedInstance().getUserSecurityGroup(
						userSecurityGroupID,
						new String[] {
								User.FETCH_GROUP_NAME,
								User.FETCH_GROUP_USER_LOCAL,
								UserSecurityGroup.FETCH_GROUP_MEMBERS,
								},
								NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
								new SubProgressMonitor(monitor, 33));

				userGroupModel.setUserSecurityGroup(group);

				// load users
				Collection<User> users = UserDAO.sharedInstance().getUsers(
						IDGenerator.getOrganisationID(),
//						new String[] { User.USER_TYPE_ORGANISATION, User.USER_TYPE_USER },
						(String[]) null, // all types - groups are separate objects now
						new String[] {
							User.FETCH_GROUP_NAME,
							User.FETCH_GROUP_USER_LOCAL
						},
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new SubProgressMonitor(monitor, 33)
				);

				// filter out all internal users (concrete _System_ and _Other_), since they should not end up in a user-group
				Set<AuthorizedObject> groupMembers = new HashSet<AuthorizedObject>(group.getMembers());
				Set<User> members = new HashSet<User>(groupMembers.size());
				for (Iterator<User> it = users.iterator(); it.hasNext(); ) {
					User user = it.next();
					if (user.getName().startsWith("_") && user.getName().endsWith("_")) //$NON-NLS-1$ //$NON-NLS-2$
						it.remove();

					if (groupMembers.contains(user.getUserLocal()))
						members.add(user);
				}

				userGroupModel.setAvailableUsers(users);
				userGroupModel.setUsers(members);

//				Collection<User> excludedUsers = new HashSet<User>(users);
//				excludedUsers.removeAll(userGroup.getUsers());
//				userGroupModel.setExcludedUsers(excludedUsers);
//				userGroupModel.setExcludedUsersUnchanged(new ArrayList<User>(excludedUsers));
//
//				Collection<User> includedUsers = new HashSet<User>(userGroup.getUsers());
//				userGroupModel.setIncludedUsers(includedUsers);
//				userGroupModel.setIncludedUsersUnchanged(new ArrayList<User>(includedUsers));

				// load role groups
				RoleGroupSetCarrier roleGroupSetCarrier = RoleGroupDAO.sharedInstance().getRoleGroupSetCarrier(
						userSecurityGroupID,
						getAuthorityID(),
						(String[])null, 1, // not interested in User
						(String[])null, 1, // not interested in Authority
						new String[] { FetchPlan.DEFAULT, RoleGroup.FETCH_GROUP_NAME, RoleGroup.FETCH_GROUP_DESCRIPTION},
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new SubProgressMonitor(monitor, 34));

				roleGroupModel.setRoleGroupsAssignedDirectly(roleGroupSetCarrier.getAssignedToUser());
				roleGroupModel.setRoleGroupsAssignedToUserGroups(roleGroupSetCarrier.getAssignedToUserGroups());
				roleGroupModel.setRoleGroupsAssignedToOtherUser(roleGroupSetCarrier.getAssignedToOtherUser());
				roleGroupModel.setAllRoleGroupsInAuthority(roleGroupSetCarrier.getAllInAuthority());
				roleGroupModel.setInAuthority(roleGroupSetCarrier.isInAuthority());
				roleGroupModel.setControlledByOtherUser(roleGroupSetCarrier.isControlledByOtherUser());

				setLoaded(true); // must be done before fireModifyEvent!
				fireModifyEvent(null, group);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.done();
		}
	}

	protected AuthorityID getAuthorityID()
	{
		try {
			return AuthorityID.create(Login.getLogin().getOrganisationID(), Authority.AUTHORITY_ID_ORGANISATION);
		} catch (LoginException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Save the user data.
	 * @param monitor The progress monitor to use.
	 */
	@Override
	public boolean doSave(ProgressMonitor monitor)
	{
		if (!isLoaded())
			return false;

		Collection<RoleGroup> includedRoleGroups = roleGroupModel.getRoleGroupsAssignedDirectly();
		Set<RoleGroupID> includedRoleGroupIDs = NLJDOHelper.getObjectIDSet(includedRoleGroups);

		Collection<User> includedUsers = userGroupModel.getIncludedUsers();
		Set<UserLocalID> includedUserLocalIDs = new HashSet<UserLocalID>(includedUsers.size());
		for (User u : includedUsers)
			includedUserLocalIDs.add((UserLocalID) JDOHelper.getObjectId(u.getUserLocal()));

		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usersecuritygroup.SecurityPreferencesController.doSave.monitor.taskName"), 100); //$NON-NLS-1$
		try	{
			UserSecurityGroup userSecurityGroup = userGroupModel.getUserSecurityGroup();
			UserSecurityGroupID userSecurityGroupID = (UserSecurityGroupID) JDOHelper.getObjectId(userSecurityGroup);

			UserSecurityGroupDAO.sharedInstance().setMembersOfUserSecurityGroup(
					userSecurityGroupID,
					includedUserLocalIDs,
					new SubProgressMonitor(monitor, 50)
			);

			AuthorityDAO.sharedInstance().setGrantedRoleGroups(
					userSecurityGroupID,
					getAuthorityID(),
					includedRoleGroupIDs,
					new SubProgressMonitor(monitor, 50)
			);

			monitor.done();
		} catch(Exception e) {
			logger.error("Saving user failed", e); //$NON-NLS-1$
			monitor.setCanceled(true);
			throw new RuntimeException(e);
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

	/**
	 * Get the usergroup model.
	 * @return the usergroup model
	 */
	public GroupSecurityPreferencesModel getUserGroupModel()
	{
		if (!isLoaded())
			throw new IllegalStateException("Cannot access model if controller not loaded."); //$NON-NLS-1$
		return userGroupModel;
	}

	/**
	 * Get the rolegroup model.
	 * @return the rolegroup model
	 */
	public RoleGroupSecurityPreferencesModel getRoleGroupModel()
	{
		if (!isLoaded())
			throw new IllegalStateException("Cannot access model if controller not loaded."); //$NON-NLS-1$
		return roleGroupModel;
	}

	/**
	 * Get the userID.
	 * @return the userID
	 */
	public UserSecurityGroupID getUserSecurityGroupID()
	{
		return userSecurityGroupID;
	}

	public void setPage(IFormPage page) {
		// TODO: Nothing done here yet
	}
}
