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

package org.nightlabs.jfire.web.login;

import java.util.Properties;

import javax.naming.InitialContext;

import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.web.demoshop.BaseServlet;
import org.nightlabs.jfire.web.demoshop.resource.WebShopConfig;

/**
 * This is the webshop specific implementation of the Jfire login class.
 * When logging in, an instance of this class is stored in the session under the key
 * "login" (defined by SESSION_KEY). Thus, if there is a current user authenticated,
 * this object should exist. Otherwise a NotAuthenticatedException should be thrown.
 *
 * @author marco
 */
public class Login extends JFireLogin
{
	public static final String SESSION_KEY = "login";

	public static Login getLogin()
	{
		WebShopConfig config = BaseServlet.getConfig();
		Properties props = new Properties();
		props.setProperty(PROP_ORGANISATION_ID, config.getOrganisationId());
		props.setProperty(PROP_USER_ID, config.getUserId());
		props.setProperty(PROP_PASSWORD, config.getPassword());
		props.setProperty(PROP_PROVIDER_URL, config.getProviderUrl());
		props.setProperty(PROP_INITIAL_CONTEXT_FACTORY, config.getInitialContextFactory());
		return new Login(props);
	}


	public Login(Properties loginProperties) {
		super(loginProperties);
	}

	/**
	 * Overriden because super.getInitialContextProperties() checks loginContext for null and in case
	 * throws an IllegalStateException as Login.getLogin() always creates a new instance by default loginContext is always null.
	 * This is related to bug https://www.jfire.org/modules/bugs/view.php?id=1411
	 *
	 * @return Properties for an {@link InitialContext} configured with the values of this JFireLogin.
	 */
	@Override
	public Properties getInitialContextProperties()
	{
		if (initialContextProperties == null) {
			initialContextProperties = getLoginData().getInitialContextProperties();
		}
		return initialContextProperties;
	}
}
