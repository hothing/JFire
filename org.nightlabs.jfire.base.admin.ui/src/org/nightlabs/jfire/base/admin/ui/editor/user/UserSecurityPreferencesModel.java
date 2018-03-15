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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.UserID;

/**
 * A model for a the security preferences of a user.
 * 
 * @version $Revision: 10665 $ - $Date: 2008-06-03 03:47:08 +0200 (Di, 03 Jun 2008) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class UserSecurityPreferencesModel extends CollectionModel<UserSecurityGroup> {
	/**
	 * The user id.
	 */
	private UserID userID;

	/**
	 * The user
	 */
	private User user;

	private Config userConfig;

	private Set<UserSecurityGroup> availableUserSecurityGroups;

	/**
	 * Create an instance of SecurityPreferencesModel.
	 * @param userID The user id.
	 */
	public UserSecurityPreferencesModel(UserID userID) {
		this.userID = userID;
	}

	/**
	 * Adds the given user group to the model.
	 * @param userGroup The {@link UserGroup} to be added.
	 */
	public void addUserSecurityGroup(UserSecurityGroup userGroup) {
		addElement(userGroup);
	}

	/**
	 * Removes the given user group from the model if it exists.
	 * @param userGroup The {@link UserGroup} to be removed.
	 */
	public void removeUserSecurityGroup(UserSecurityGroup userGroup) {
		removeElement(userGroup);
	}

	/**
	 * Returns an unmodifiable set of the {@link UserGroup}s of this model.
	 * @return all {@link UserGroup}s of this model.
	 */
	public Collection<UserSecurityGroup> getUserSecurityGroups() {
		return getElements();
	}

	/**
	 * Sets the {@link UserGroup}s of this model
	 * @param userGroups The {@link UserGroup}s to be set.
	 */
	public void setUserSecurityGroups(Collection<UserSecurityGroup> userGroups) {
		setElements(userGroups);
	}

	/**
	 * Get the user.
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Set the user.
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
		modelChanged();
	}

	/**
	 * Get the userID.
	 * @return the userID
	 */
	public UserID getUserID() {
		return userID;
	}

	/**
	 * Get the userConfig.
	 * @return the userConfig
	 */
	public Config getUserConfig() {
		return userConfig;
	}

	/**
	 * Set the userConfig.
	 * @param userConfig the userConfig to set
	 */
	public void setUserConfig(Config userConfig) {
		this.userConfig = userConfig;
		modelChanged();
	}

	/**
	 * Sets the user groups currently available
	 * @param availableUserSecurityGroups A collection of the available user groups
	 */
	public void setAvailableUserSecurityGroups(Collection<UserSecurityGroup> availableUserGroups) {
		this.availableUserSecurityGroups = new HashSet<UserSecurityGroup>(availableUserGroups);
		modelChanged();
	}

	/**
	 * Returns an unmodifiable collection of the available user groups of this model
	 * @return an unmodifiable collection of the available user groups of this model
	 */
	public Collection<UserSecurityGroup> getAvailableUserSecurityGroups() {
		return Collections.unmodifiableSet(availableUserSecurityGroups);
	}
}
