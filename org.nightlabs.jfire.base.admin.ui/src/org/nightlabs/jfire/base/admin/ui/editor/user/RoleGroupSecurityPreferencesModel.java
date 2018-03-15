/**
 * 
 */
package org.nightlabs.jfire.base.admin.ui.editor.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

/**
 * @author nick
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author marco schulze - marco at nightlabs dot de
 */
public class RoleGroupSecurityPreferencesModel extends BaseModel
{
	/**
	 * The included role groups.
	 */
	private Set<RoleGroup> roleGroupsAssignedDirectly = new HashSet<RoleGroup>();

	private boolean inAuthority;
	private boolean controlledByOtherUser;

	/**
	 * The included role groups inherited from UserGroups.
	 */
	private Set<RoleGroup> roleGroupsAssignedToUserGroups = Collections.emptySet();

	private Set<RoleGroup> roleGroupsAssignedToOtherUser = Collections.emptySet();

	/**
	 * All role groups that can be used in this authority (defined by {@link org.nightlabs.jfire.security.AuthorityType#getRoleGroups()}).
	 */
	private Set<RoleGroup> allRoleGroupsInAuthority = Collections.emptySet();

	public Set<RoleGroup> getRoleGroupsAssignedDirectly() {
		return Collections.unmodifiableSet(roleGroupsAssignedDirectly);
	}

	public void setRoleGroupsAssignedDirectly(Set<RoleGroup> roleGroupsAssignedDirectly) {
		if (Util.equals(this.roleGroupsAssignedDirectly, roleGroupsAssignedDirectly))
			return;

		this.roleGroupsAssignedDirectly = new HashSet<RoleGroup>(roleGroupsAssignedDirectly); // since we modify it, we better copy
		modelChanged();
	}

	public void addRoleGroup(RoleGroup roleGroup) {
		this.roleGroupsAssignedDirectly.add(roleGroup);
		modelChanged();
	}
	
	public void removeRoleGroup(RoleGroup roleGroup) {
		this.roleGroupsAssignedDirectly.remove(roleGroup);
		modelChanged();
	}

	public Collection<RoleGroup> getRoleGroupsAssignedToUserGroups() {
		return Collections.unmodifiableCollection(roleGroupsAssignedToUserGroups);
	}

	public void setRoleGroupsAssignedToUserGroups(Set<RoleGroup> roleGroupsAssignedToUserGroups) {
		if (Util.equals(this.roleGroupsAssignedToUserGroups, roleGroupsAssignedToUserGroups))
			return;

		this.roleGroupsAssignedToUserGroups = roleGroupsAssignedToUserGroups;
		modelChanged();
	}
	
	public void setAllRoleGroupsInAuthority(Set<RoleGroup> allRoleGroupsInAuthority) {
		if (Util.equals(this.allRoleGroupsInAuthority, allRoleGroupsInAuthority))
			return;

		this.allRoleGroupsInAuthority = allRoleGroupsInAuthority;
		modelChanged();
	}
	
	public Collection<RoleGroup> getAllRoleGroupsInAuthority() {
		return Collections.unmodifiableCollection(allRoleGroupsInAuthority);
	}

	public Collection<RoleGroup> getRoleGroupsAssignedToOtherUser() {
		return roleGroupsAssignedToOtherUser;
	}
	public void setRoleGroupsAssignedToOtherUser(Set<RoleGroup> roleGroupsAssignedToOtherUser) {
		if (Util.equals(this.roleGroupsAssignedToOtherUser, roleGroupsAssignedToOtherUser))
			return;

		this.roleGroupsAssignedToOtherUser = roleGroupsAssignedToOtherUser;
		modelChanged();
	}

	public void setInAuthority(boolean inAuthority) {
		if (this.inAuthority == inAuthority)
			return;

		this.inAuthority = inAuthority;
		modelChanged();
	}
	public void setControlledByOtherUser(boolean controlledByOtherUser) {
		if (this.controlledByOtherUser == controlledByOtherUser)
			return;

		this.controlledByOtherUser = controlledByOtherUser;
		modelChanged();
	}

	/**
	 * Find out whether the user is himself in the <code>Authority</code>. A user who is not in an authority directly, can still be managed 
	 * in the authority indirectly via its user-groups. If no {@link UserGroup} containing the user is in the Authority, the "other" user {@link User#USER_ID_OTHER}
	 * defines the rights.
	 *
	 * @return <code>true</code> if the user is directly in the <code>Authority</code> (and thus can have individual rights assigned).
	 */
	public boolean isInAuthority() {
		return inAuthority;
	}
	/**
	 * This is <code>true</code>, if the user is neither directly in an {@link Authority} nor via one of its {@link UserGroup}s.
	 * If this <code>RoleGroupIDSetCarrier</code> has been created for the other user, this flag is <code>false</code>.
	 *
	 * @return <code>true</code> if the user is neither directly in the authority, nor one of its user-groups.
	 */
	public boolean isControlledByOtherUser() {
		return controlledByOtherUser;
	}
}
