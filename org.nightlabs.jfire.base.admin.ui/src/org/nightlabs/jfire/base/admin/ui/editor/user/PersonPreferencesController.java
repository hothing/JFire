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

import javax.jdo.FetchPlan;

import org.apache.log4j.Logger;
import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.Util;

/**
 * A controller that loads a user with its person.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class PersonPreferencesController extends EntityEditorPageController
{

	private static final String[] FETCH_GROUPS = new String[] {
		FetchPlan.DEFAULT,
		User.FETCH_GROUP_USER_LOCAL,
		User.FETCH_GROUP_PERSON,
		PropertySet.FETCH_GROUP_FULL_DATA,
		IExpression.FETCH_GROUP_IEXPRESSION_FULL_DATA
	};

	private static final String[] STRUCT_LOCAL_FETCH_GROUPS = new String[] {
		FetchPlan.DEFAULT,
		IStruct.FETCH_GROUP_ISTRUCT_FULL_DATA,
		IExpression.FETCH_GROUP_IEXPRESSION_FULL_DATA
	};

	private static final long serialVersionUID = -1651161683093714800L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(PersonPreferencesController.class);

	/**
	 * The user id.
	 */
	private UserID userID;

	/**
	 * The user editor.
	 */
	private EntityEditor editor;

	/**
	 * The editor model
	 */
	private User user;

	/**
	 * The structLocal to use.
	 */
	private StructLocal structLocal;

	/**
	 * Create an instance of this controller for
	 * an {@link UserEditor} and load the data.
	 */
	public PersonPreferencesController(EntityEditor editor)
	{
		super(editor);
		this.userID = (UserID) ((JDOObjectEditorInput<?>)editor.getEditorInput()).getJDOObjectID();
		this.editor = editor;
	}

	/**
	 * Load the user data and user groups.
	 * @param monitor The progress monitor to use.
	 */
	public void doLoad(ProgressMonitor monitor)
	{
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.PersonPreferencesController.loadingUserPerson"), 2); //$NON-NLS-1$
		try {
//			Thread.sleep(1000);
			if(userID != null) {
				logger.info("Loading user "+userID.userID); //$NON-NLS-1$
				// load user with person data
				User user = UserDAO.sharedInstance().getUser(
						userID, FETCH_GROUPS,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						monitor
				);
				monitor.worked(1);


//				structLocal = StructLocalDAO.sharedInstance().getStructLocal(
//						Organisation.DEV_ORGANISATION_ID,
//						Person.class, STRUCT_LOCAL_FETCH_GROUPS, Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE, monitor);
				user = Util.cloneSerializable(user);

				if (user.getPerson() == null)
					user.setPerson(new Person(user.getOrganisationID(), PropertySet.TEMPORARY_PROP_ID));

				this.structLocal = StructLocalDAO.sharedInstance().getStructLocal(
						user.getPerson().getStructLocalObjectID(),
						new SubProgressMonitor(monitor, 1)
				);

				this.user = user;
				fireModifyEvent(null, getUser());
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Save the user data.
	 * @param monitor The progress monitor to use.
	 */
	public boolean doSave(ProgressMonitor monitor)
	{
		if ( logger.isInfoEnabled() ) {
			logger.info("***********************************"); //$NON-NLS-1$
			logger.info("doSave()"); //$NON-NLS-1$
			logger.info("***********************************"); //$NON-NLS-1$
		}

		if (!isLoaded()) {
			logger.info("User not loaded will return. User "+userID.userID); //$NON-NLS-1$
			return false;
		}
		logger.info("Saving user "+userID.userID); //$NON-NLS-1$
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.PersonPreferencesController.doSave.monitor.taskName"), 6); //$NON-NLS-1$
		try	{
			monitor.worked(1);
			logger.info("Saving user "+userID.userID+" person "+user.getPerson()); //$NON-NLS-1$ //$NON-NLS-2$
			User oldUser = user;
			user = UserDAO.sharedInstance().storeUser(
					user, (String)null, true, FETCH_GROUPS,
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new SubProgressMonitor(monitor, 5)
			);
			user = Util.cloneSerializable(user);
			if (
				oldUser.getUserID().equals(Login.getLogin().getUserID()) &&
				oldUser.getUserLocal().getNewPassword() != null
				)
			{
				// If the current users password is changed,
				// this needs to be registered in Login
				Login.sharedInstance().setPassword(oldUser.getUserLocal().getNewPassword());
			}

			fireModifyEvent(oldUser, getUser());

			logger.info("Saving user "+userID.userID+" person done without errors"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch(Exception e) {
			logger.error("Saving user failed", e); //$NON-NLS-1$
			monitor.setCanceled(true);
			throw new RuntimeException(e);
		} finally {
			monitor.done();
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
	 * Get the userID.
	 * @return the userID
	 */
	public UserID getUserID()
	{
		return userID;
	}

	/**
	 * Returns the user associated with this controller
	 */
	public User getUser() {
		return user;
	}

	public StructLocal getStructLocal() {
		return structLocal;
	}
}
