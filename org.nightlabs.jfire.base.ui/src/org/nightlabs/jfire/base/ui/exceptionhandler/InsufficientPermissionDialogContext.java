package org.nightlabs.jfire.base.ui.exceptionhandler;

import java.util.Set;

import org.nightlabs.jfire.security.Role;
import org.nightlabs.jfire.security.id.RoleID;

public class InsufficientPermissionDialogContext
{
	private Set<RoleID> requiredRoleIDs;
	private Set<Role> requiredRoles;

	public InsufficientPermissionDialogContext(Set<RoleID> requiredRoleIDs, Set<Role> requiredRoles) {
		this.requiredRoleIDs = requiredRoleIDs;
		this.requiredRoles = requiredRoles;
	}

	public Set<RoleID> getRequiredRoleIDs() {
		return requiredRoleIDs;
	}
	public Set<Role> getRequiredRoles() {
		return requiredRoles;
	}
}
