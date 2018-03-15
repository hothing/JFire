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

package org.nightlabs.jfire.base.admin.ui.rolegroup;

import org.nightlabs.jfire.security.RoleGroup;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class RoleGroupContainer implements Comparable<RoleGroupContainer>
{
	private RoleGroup roleGroup;
	private boolean assignedToUser = false;
	private boolean assignedToUserGroup = false;


	public RoleGroupContainer(RoleGroup rg)
	{
		this.roleGroup = rg;
	}

	public boolean isAssignedToUser()
	{
		return assignedToUser;
	}

	public boolean isAssignedToUserGroup()
	{
		return assignedToUserGroup;
	}

	public void setAssignedToUser(boolean b)
	{
		this.assignedToUser = b;
	}

	public void setAssignedToUserGroup(boolean b)
	{
		this.assignedToUserGroup = b;
	}

	public RoleGroup getRoleGroup()
	{
		return roleGroup;
	}

	@Override
	public int compareTo(RoleGroupContainer other)
	{
		RoleGroup otherRoleGroup = other.getRoleGroup();
		return this.roleGroup.getName().getText().compareTo(otherRoleGroup.getName().getText());
	}
}
