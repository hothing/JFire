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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.jboss.cascadedauthentication;

import java.io.Serializable;

import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @deprecated This class will soon be removed since it is periphery of {@link LoginInitialContextFactory},
 * which is about to be replaced by
 * <code>org.nightlabs.unifiedjndi.jboss.client.UnifiedNamingContextFactory</code>.
 */
@Deprecated
class UserDescriptor
implements Serializable
{
	public static final long serialVersionUID = 1L;

	public UserDescriptor(String userName, String password)
	{
		this.userName = userName;
		this.password = password;
	}

	private String userName;
	private String password;

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof UserDescriptor)) return false;
		UserDescriptor o = (UserDescriptor) obj;
		return
			Util.equals(o.userName, this.userName) &&
			Util.equals(o.password, this.password);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(userName) ^ + Util.hashCode(password);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '[' + userName + ']';
	}
}
