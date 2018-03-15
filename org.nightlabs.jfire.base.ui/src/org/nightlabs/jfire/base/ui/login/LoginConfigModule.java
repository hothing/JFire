/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
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
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.ui.login;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.nightlabs.concurrent.RWLock;
import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;
import org.nightlabs.j2ee.LoginData;

/**
 * This class holds all user specific data relevant for login in into JFire. It holds a list of
 * {@link LoginConfiguration}s that may be presented to the user upon login to reuse.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class LoginConfigModule extends ConfigModule implements Cloneable
{
	private static final long serialVersionUID = 3L;

	/**
	 * Holds the login configurations that have been saved upon request by the user.
	 */
	private LinkedList<LoginConfiguration> savedLoginConfigurations;

	/**
	 * Holds the login configuration that is currently used.
	 */
	private LoginConfiguration latestLoginConfiguration;

	@Override
	public void init() throws InitException {
		super.init();

		if (savedLoginConfigurations == null)
			setSavedLoginConfigurations(new LinkedList<LoginConfiguration>());

		for (LoginConfiguration loginConfiguration : getAllLoginConfigurations())
			loginConfiguration.setLoginConfigModule(this);
	}

	public void setLatestLoginConfiguration(LoginData loginData, String configurationName)
	{
		acquireWriteLock();
		try {
			LoginConfiguration loginConfiguration = null;

//			// we search for the same loginData - not very efficient to iterate, but there are not many instances in the list anyway
//			if (savedLoginConfigurations != null) {
//			for (LoginConfiguration lc : savedLoginConfigurations) {
//			if (lc._getLoginData() == loginData) {
//			loginConfiguration = lc;
//			break;
//			}
//			}
//			}
//			the LoginData + LoginConfiguration instances are cloned a few times along the way => we don't try to deduplicate - it's not really necessary.

			loginConfiguration = new LoginConfiguration(loginData, this);
			loginConfiguration.setName(configurationName);
			setLatestLoginConfiguration(loginConfiguration);

		} finally {
			releaseLock();
		}
	}

	public void saveLatestConfiguration() {
		acquireWriteLock();
		try {

			LoginConfiguration copy = latestLoginConfiguration.clone();
			savedLoginConfigurations.remove(copy);
			savedLoginConfigurations.addFirst(copy);

			setChanged();
		} finally {
			releaseLock();
		}
	}

	public LinkedList<LoginConfiguration> getSavedLoginConfigurations() {
		return savedLoginConfigurations;
	}

	public void setSavedLoginConfigurations(LinkedList<LoginConfiguration> loginConfigurations) {
		acquireWriteLock();
		try {
			this.savedLoginConfigurations = loginConfigurations;
			if (this.savedLoginConfigurations != null) {
				for (LoginConfiguration loginConfiguration : this.savedLoginConfigurations) {
					loginConfiguration.setLoginConfigModule(this);
				}
			}
			setChanged();
		} finally {
			releaseLock();
		}
	}

	public LoginConfiguration getLatestLoginConfiguration() {
		return latestLoginConfiguration;
	}

	public void setLatestLoginConfiguration(LoginConfiguration currentLoginConfiguration) {
		acquireWriteLock();
		try {
			this.latestLoginConfiguration = currentLoginConfiguration;

			if (this.latestLoginConfiguration != null)
				this.latestLoginConfiguration.setLoginConfigModule(this);

			setChanged();
		} finally {
			releaseLock();
		}
	}

	public boolean hasConfigWithName(String name) {
		acquireReadLock();
		try {
			for (LoginConfiguration conf : savedLoginConfigurations)
				if (conf.getName().equals(name))
					return true;
			return false;
		} finally {
			releaseLock();
		}
	}

	public LoginConfiguration getLastSavedLoginConfiguration() {
		acquireReadLock();
		try {
			if (savedLoginConfigurations.isEmpty())
				return null;
			else
				return savedLoginConfigurations.getFirst();
		} finally {
			releaseLock();
		}
	}

	public void makeLatestFirst() {
		acquireWriteLock();
		try {
			for (LoginConfiguration cfg : savedLoginConfigurations) {
				if (cfg.equals(latestLoginConfiguration)) {
					savedLoginConfigurations.remove(cfg);
					savedLoginConfigurations.addFirst(cfg);
					return;
				}
			}
		} finally {
			releaseLock();
		}
	}

	public void deleteSavedLoginConfiguration(LoginConfiguration toBeDeleted) {
		savedLoginConfigurations.remove(toBeDeleted);
		setChanged();
	}

	private Set<LoginConfiguration> getAllLoginConfigurations()
	{
		// Since we cannot be sure that the latestLoginConfiguration is part of the
		// savedLoginConfigurations (currently, it never is but this might change again),
		// we simply put all in a Set.
		Set<LoginConfiguration> loginConfigurations = new HashSet<LoginConfiguration>(savedLoginConfigurations != null ? savedLoginConfigurations.size() + 1 : 5);
		if (latestLoginConfiguration != null)
			loginConfigurations.add(latestLoginConfiguration);

		if (savedLoginConfigurations != null) {
			for (LoginConfiguration loginConfiguration : savedLoginConfigurations)
				loginConfigurations.add(loginConfiguration);
		}
		return loginConfigurations;
	}

	@Override
	protected void beforeSave()
	{
		// we modify ourselves => need write-lock (which is released in this.afterSave(...))
		acquireWriteLock();

		for (LoginConfiguration loginConfiguration : getAllLoginConfigurations()) {
			loginConfiguration.setLoginConfigModule(this);
			loginConfiguration.beforeSave();
		}
	}

	@Override
	protected void afterSave(boolean successful)
	{
		try {

			for (LoginConfiguration loginConfiguration : getAllLoginConfigurations())
				loginConfiguration.afterSave();

		} finally {
			// we acquired a write-lock in beforeSave() => need to release it
			releaseLock();
		}
	}

	protected RWLock getRWLock()
	{
		return rwLock;
	}
}
