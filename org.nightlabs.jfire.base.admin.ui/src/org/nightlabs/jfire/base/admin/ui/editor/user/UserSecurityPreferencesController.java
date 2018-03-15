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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.notification.NotificationAdapterJob;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeEvent;
import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeListener;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupSetCarrier;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.dao.AuthorityDAO;
import org.nightlabs.jfire.security.dao.RoleGroupDAO;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.dao.UserSecurityGroupDAO;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Controller class for the security preferences of a user.
 *
 * @version $Revision: 4472 $ - $Date: 2006-08-28 20:21:33 +0000 (Mon, 28 Aug 2006) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Niklas Schiffler <nick@nightlabs.de>
 */
public class UserSecurityPreferencesController extends EntityEditorPageController
{
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(UserSecurityPreferencesController.class);

	/**
	 * The user id.
	 */
	UserID userID;

	/**
	 * The user editor.
	 */
	EntityEditor editor;

	/**
	 * The user security preference model.
	 */
	UserSecurityPreferencesModel userModel;

	/**
	 * The user security preference model.
	 */
	RoleGroupSecurityPreferencesModel roleGroupModel;

	private boolean loading = false;

	/**
	 * Create an instance of this controller for
	 * an {@link UserEditor} and load the data.
	 */
	public UserSecurityPreferencesController(EntityEditor editor)
	{
		super(editor, true);
		this.userID = ((UserEditorInput)editor.getEditorInput()).getJDOObjectID();
		this.editor = editor;
		this.userModel = new UserSecurityPreferencesModel(userID);
		this.roleGroupModel = new RoleGroupSecurityPreferencesModel();
		JDOLifecycleManager.sharedInstance().addNotificationListener(User.class, userChangedListener);

		userModel.addModelChangeListener(new ModelChangeListener() {
			public void modelChanged(ModelChangeEvent event) {
				if (!loading) {
					// TODO maybe get a better progress monitor here.
					reloadRoleGroupsFromUserSecurityGroups();
				}
			}
		});
	}

	@Override
	public void dispose()
	{
		JDOLifecycleManager.sharedInstance().removeNotificationListener(User.class, userChangedListener);
		super.dispose();
	}

	private NotificationListener userChangedListener = new NotificationAdapterJob(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.SecurityPreferencesController.loadingChangedUser")) //$NON-NLS-1$
	{
		public void notify(NotificationEvent notificationEvent) {
			doLoad(getProgressMonitor());
		}
	};

	/**
	 * Load the user data and user groups.
	 * @param _monitor The progress monitor to use.
	 */
	public void doLoad(ProgressMonitor monitor)
	{
		loading = true;
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.SecurityPreferencesController.loadingUserPerson"), 100); //$NON-NLS-1$
		try {
			if(userID != null) {
				logger.info("Loading user "+userID.userID); //$NON-NLS-1$
				// load user with person data
				User user = UserDAO.sharedInstance().getUser(
						userID,
						new String[] {
								User.FETCH_GROUP_NAME,
								User.FETCH_GROUP_USER_LOCAL,
								UserLocal.FETCH_GROUP_USER_SECURITY_GROUPS,
//								PropertySet.FETCH_GROUP_FULL_DATA // TODO is the person really necessary here?!
								},
								NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
								new SubProgressMonitor(monitor, 33));

				userModel.setUser(user);

				// load user groups
				Collection<UserSecurityGroup> availableUserSecurityGroups = UserSecurityGroupDAO.sharedInstance().getUserSecurityGroups(
						new String[] {
								UserSecurityGroup.FETCH_GROUP_NAME,
								UserSecurityGroup.FETCH_GROUP_DESCRIPTION,
								UserSecurityGroup.FETCH_GROUP_MEMBERS
						},
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new SubProgressMonitor(monitor, 33)
				);
				//model.setUserSecurityGroups(availableUserSecurityGroups);

//				Collection<UserSecurityGroup> excludedUserSecurityGroups = new HashSet<UserSecurityGroup>(availableUserSecurityGroups);
//				excludedUserSecurityGroups.removeAll(user.getUserSecurityGroups());
//				userModel.setExcludedUserSecurityGroups(excludedUserSecurityGroups);

				Collection<UserSecurityGroup> includedUserSecurityGroups = new HashSet<UserSecurityGroup>();
				for (UserSecurityGroup userSecurityGroup : user.getUserLocal().getUserSecurityGroups())
					includedUserSecurityGroups.add(userSecurityGroup);

				userModel.setUserSecurityGroups(includedUserSecurityGroups);
				userModel.setAvailableUserSecurityGroups(availableUserSecurityGroups);
				UserLocalID userLocalID = UserLocalID.create(userID.organisationID, userID.userID, userID.organisationID);

				// load role groups
				RoleGroupSetCarrier roleGroupSetCarrier = RoleGroupDAO.sharedInstance().getRoleGroupSetCarrier(
						userLocalID,
						getAuthorityID(),
						(String[])null, 1, // not interested in User
						(String[])null, 1, // not interested in Authority
						FETCH_GROUPS_ROLE_GROUP,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new SubProgressMonitor(monitor, 34)
				);
				roleGroupModel.setRoleGroupsAssignedDirectly(roleGroupSetCarrier.getAssignedToUser());
				roleGroupModel.setRoleGroupsAssignedToUserGroups(roleGroupSetCarrier.getAssignedToUserGroups());
				roleGroupModel.setRoleGroupsAssignedToOtherUser(roleGroupSetCarrier.getAssignedToOtherUser());
				roleGroupModel.setAllRoleGroupsInAuthority(roleGroupSetCarrier.getAllInAuthority());
				roleGroupModel.setInAuthority(roleGroupSetCarrier.isInAuthority());
				roleGroupModel.setControlledByOtherUser(roleGroupSetCarrier.isControlledByOtherUser());

				logger.info("Loading user "+userID.userID+" done without errors"); //$NON-NLS-1$ //$NON-NLS-2$
				setLoaded(true); // must be done before fireModifyEvent!
				fireModifyEvent(null, user);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.done();
			loading = false;
		}
	}

	private static final String[] FETCH_GROUPS_ROLE_GROUP = {
		FetchPlan.DEFAULT, RoleGroup.FETCH_GROUP_NAME, RoleGroup.FETCH_GROUP_DESCRIPTION
	};

	private Job reloadRoleGroupsFromUserSecurityGroupsJob;

	public void reloadRoleGroupsFromUserSecurityGroups() {
		Job loadJob = new Job(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserSecurityPreferencesController.job.reloadingRoleGroups")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				Collection<UserSecurityGroupID> userSecurityGroupIDs = NLJDOHelper.getObjectIDSet(userModel.getUserSecurityGroups());
				Set<RoleGroup> roleGroups = RoleGroupDAO.sharedInstance().getRoleGroupsForUserSecurityGroups(
						userSecurityGroupIDs,
						getAuthorityID(),
						FETCH_GROUPS_ROLE_GROUP,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						monitor
				);

				if (reloadRoleGroupsFromUserSecurityGroupsJob == this) {
					roleGroupModel.setRoleGroupsAssignedToUserGroups(roleGroups);
					fireModifyEvent(null, roleGroupModel);
				}

//				UserLocalID userLocalID = UserLocalID.create(userID.organisationID, userID.userID);
//				RoleGroupSetCarrier roleGroupSetCarrier = RoleGroupDAO.sharedInstance().getRoleGroupSetCarrier(
//						userLocalID,
//						getAuthorityID(),
//						(String[])null, 1, // not interested in User
//						(String[])null, 1, // not interested in Authority
//						new String[] { FetchPlan.DEFAULT, RoleGroup.FETCH_GROUP_NAME, RoleGroup.FETCH_GROUP_DESCRIPTION },
//						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
//						monitor
//				);
//				if (reloadRoleGroupsFromUserSecurityGroupsJob == this) {
//					roleGroupModel.setRoleGroupsAssignedToUserGroups(roleGroupSetCarrier.getAssignedToUserGroups());
//					fireModifyEvent(null, roleGroupModel);
//				}
				return Status.OK_STATUS;
			}
		};

		reloadRoleGroupsFromUserSecurityGroupsJob = loadJob;
		loadJob.schedule();
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
	public boolean doSave(ProgressMonitor monitor)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("***********************************"); //$NON-NLS-1$
			logger.debug("doSave()"); //$NON-NLS-1$
			logger.debug("***********************************"); //$NON-NLS-1$
		}

		if (!isLoaded()) {
			logger.info("User not loaded will return. User "+userID.userID); //$NON-NLS-1$
			return false;
		}
		logger.info("Saving user: "+userID); //$NON-NLS-1$

		Collection<UserSecurityGroup> includedUserSecurityGroups = userModel.getUserSecurityGroups();
		Set<UserSecurityGroupID> includedUserSecurityGroupIDs = NLJDOHelper.getObjectIDSet(includedUserSecurityGroups);

		Collection<RoleGroup> includedRoleGroups = roleGroupModel.getRoleGroupsAssignedDirectly();
		Set<RoleGroupID> includedRoleGroupIDs = NLJDOHelper.getObjectIDSet(includedRoleGroups);

		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.SecurityPreferencesController.doSave.monitor.taskName"), 100); //$NON-NLS-1$
		try	{
			User user = userModel.getUser();
			AuthorizedObjectID authorizedObjectID = (AuthorizedObjectID) JDOHelper.getObjectId(user.getUserLocal());

			UserSecurityGroupDAO.sharedInstance().setUserSecurityGroupsOfMember(
					includedUserSecurityGroupIDs,
					authorizedObjectID,
					new SubProgressMonitor(monitor, 50)
			);

			AuthorityDAO.sharedInstance().setGrantedRoleGroups(
					authorizedObjectID,
					getAuthorityID(),
					includedRoleGroupIDs,
					new SubProgressMonitor(monitor, 50)
			);

			monitor.done();
			logger.info("Saving user done without errors: " + userID); //$NON-NLS-1$ //$NON-NLS-2$
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
	 * Get the user model.
	 * @return the user model
	 */
	public UserSecurityPreferencesModel getUserModel()
	{
		if (!isLoaded())
			throw new IllegalStateException("Cannot access model if controller not loaded."); //$NON-NLS-1$
		return userModel;
	}

	/**
	 * Get the role group model.
	 * @return the role group model
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
	public UserID getUserID()
	{
		return userID;
	}

	public void setPage(IFormPage page) {
		// TODO: Nothing done here yet
	}
}
