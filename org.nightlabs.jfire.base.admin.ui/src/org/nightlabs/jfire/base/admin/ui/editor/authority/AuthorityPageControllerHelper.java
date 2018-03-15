package org.nightlabs.jfire.base.admin.ui.editor.authority;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeEvent;
import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeListener;
import org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupSecurityPreferencesModel;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.AuthorizedObject;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupSetCarrier;
import org.nightlabs.jfire.security.SecuredObject;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.dao.AuthorityDAO;
import org.nightlabs.jfire.security.dao.AuthorityTypeDAO;
import org.nightlabs.jfire.security.dao.AuthorizedObjectDAO;
import org.nightlabs.jfire.security.dao.RoleGroupDAO;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.Util;

/**
 * {@link AuthorityPageControllerHelper}s are used by different widgets like the
 * {@link AbstractAuthorityPage} to edit an {@link Authority} and its assignment to a {@link SecuredObject}.
 * <p>
 * This helper can load its data using the {@link #load(SecuredObject, ProgressMonitor)} method
 * where you have to supply the {@link SecuredObject} whose securing {@link Authority} should be loaded.
 * </p>
 * <p>
 * Subclasses have to implement the method {@link #createInheritedSecuringAuthorityResolver()} where
 * they create another helper that loads the assignment-inheritance-data of the current {@link SecuredObject}.
 * </p>
 * <p>
 * Usually a subclasses of this helper is used for a custom {@link IEntityEditorPageController}s which then
 * is assigned to custom {@link AbstractAuthorityPage}s.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public abstract class AuthorityPageControllerHelper
{

	private AuthorityTypeID authorityTypeID;
	private AuthorityID authorityID;

	private AuthorityType authorityType;
	private Authority authority;

	public AuthorityPageControllerHelper() { }

	private static final String[] FETCH_GROUPS_AUTHORITY_TYPE = {
		FetchPlan.DEFAULT,
		AuthorityType.FETCH_GROUP_ROLE_GROUPS
	};

	private static final String[] FETCH_GROUPS_ROLE_GROUP = {
		FetchPlan.DEFAULT,
		RoleGroup.FETCH_GROUP_NAME,
		RoleGroup.FETCH_GROUP_DESCRIPTION
	};

	private static final String[] FETCH_GROUPS_AUTHORIZED_OBJECT = {
		FetchPlan.DEFAULT,
		AuthorizedObject.FETCH_GROUP_NAME,
		AuthorizedObject.FETCH_GROUP_DESCRIPTION,
		UserLocal.FETCH_GROUP_USER,
		UserLocal.FETCH_GROUP_USER_SECURITY_GROUPS,
		UserSecurityGroup.FETCH_GROUP_MEMBERS
	};

	private static final String[] FETCH_GROUPS_AUTHORITY = {
		FetchPlan.DEFAULT,
		Authority.FETCH_GROUP_NAME,
		Authority.FETCH_GROUP_DESCRIPTION
	};

	private SecuredObject securedObject;

	public void load(SecuredObject securedObject, ProgressMonitor monitor)
	{
		this.securedObject = securedObject;
		load(
				(securedObject == null ? null : securedObject.getSecuringAuthorityTypeID()),
				(securedObject == null ? null : securedObject.getSecuringAuthorityID()),
				null,
				monitor
		);
	}

	/**
	 * Load the data.
	 *
	 * @param authorityTypeID the id of the {@link AuthorityType} or <code>null</code> to clear all data.
	 * @param authorityID the id of the {@link Authority}. Can be <code>null</code> if <code>newAuthority</code>
	 *		is passed instead or to indicate that there
	 *		is no authority assigned to the object which is currently edited.
	 * @param newAuthority If a new <code>Authority</code> has been created (and not yet persisted), it has no object-id
	 *		assigned. Hence, instead of passing the <code>authorityID</code>, you can pass the new authority.
	 * @throws NamingException if a problem with JNDI arises.
	 * @throws CreateException if an EJB cannot be created.
	 * @throws LoginException if login fails.
	 * @throws RemoteException if communication via RMI fails.
	 */
	protected synchronized void load(AuthorityTypeID authorityTypeID, AuthorityID authorityID, Authority newAuthority, ProgressMonitor monitor)
	{
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.AuthorityPageControllerHelper.job.loadingAuthorityData"), 120); //$NON-NLS-1$

		if (authorityTypeID == null) {
			authorityID = null;
			newAuthority = null;
		}

		if (JDOHelper.getObjectId(newAuthority) != null) {
			authorityID = (AuthorityID) JDOHelper.getObjectId(newAuthority);
			newAuthority = null;
		}

		if (authorityID != null)
			newAuthority = null;

		this.authorityTypeID = authorityTypeID;
		this.authorityID = authorityID;

		this.authorityType = null;
		this.authority = newAuthority;

		if (authorityTypeID == null) {
			monitor.worked(40);
		}
		else {
			authorityType = AuthorityTypeDAO.sharedInstance().getAuthorityType(
					authorityTypeID,
					FETCH_GROUPS_AUTHORITY_TYPE,
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					new SubProgressMonitor(monitor, 30));
		}

		changedModels.clear();
		roleGroupSecurityPreferencesModel2AuthorizedObject = new HashMap<RoleGroupSecurityPreferencesModel, AuthorizedObject>();
		authorizedObject2RoleGroupSecurityPreferencesModel = new HashMap<AuthorizedObject, RoleGroupSecurityPreferencesModel>();
		if (authorityID == null) {
			if (this.authority == null) {
				authorizedObjects = new HashMap<AuthorizedObject, Boolean>();
				authorizedObjectID2authorizedObjectMap = new HashMap<AuthorizedObjectID, AuthorizedObject>();
				monitor.worked(70);
			}
			else {
				Collection<AuthorizedObject> c = AuthorizedObjectDAO.sharedInstance().getAuthorizedObjects(
						FETCH_GROUPS_AUTHORIZED_OBJECT,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new SubProgressMonitor(monitor, 35));

				Set<RoleGroupID> roleGroupIDs = NLJDOHelper.getObjectIDSet(authorityType.getRoleGroups());
				Set<RoleGroup> roleGroupsInAuthorityType = new HashSet<RoleGroup>(
						RoleGroupDAO.sharedInstance().getRoleGroups(
								roleGroupIDs,
								FETCH_GROUPS_ROLE_GROUP,
								NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
								new SubProgressMonitor(monitor, 35))
				);

				authorizedObjects = new HashMap<AuthorizedObject, Boolean>(c.size());
				authorizedObjectID2authorizedObjectMap = new HashMap<AuthorizedObjectID, AuthorizedObject>(c.size());
				for (AuthorizedObject ao : c) {
					// ignore the system user - it always has all access rights anyway and cannot be configured
					if (ao instanceof UserLocal && User.USER_ID_SYSTEM.equals(((UserLocal)ao).getUserID()))
						continue;

					AuthorizedObjectID aoid = (AuthorizedObjectID) JDOHelper.getObjectId(ao);
					assert aoid != null : "(AuthorizedObjectID) JDOHelper.getObjectId(ao) != null"; //$NON-NLS-1$
					authorizedObjectID2authorizedObjectMap.put(aoid, ao);
					authorizedObjects.put(ao, Boolean.FALSE);

					RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel = new RoleGroupSecurityPreferencesModel();
					roleGroupSecurityPreferencesModel.setAllRoleGroupsInAuthority(roleGroupsInAuthorityType);
					roleGroupSecurityPreferencesModel.setRoleGroupsAssignedDirectly(new HashSet<RoleGroup>());
					roleGroupSecurityPreferencesModel.setRoleGroupsAssignedToUserGroups(new HashSet<RoleGroup>());
					roleGroupSecurityPreferencesModel.setRoleGroupsAssignedToOtherUser(new HashSet<RoleGroup>());
					roleGroupSecurityPreferencesModel.setControlledByOtherUser(true);
					roleGroupSecurityPreferencesModel.setInAuthority(false);

					roleGroupSecurityPreferencesModel.addModelChangeListener(roleGroupSecurityPreferencesModelChangeListener);
					authorizedObject2RoleGroupSecurityPreferencesModel.put(ao, roleGroupSecurityPreferencesModel);
					roleGroupSecurityPreferencesModel2AuthorizedObject.put(roleGroupSecurityPreferencesModel, ao);
				}
			}
		}
		else {
			authority = Util.cloneSerializable(AuthorityDAO.sharedInstance().getAuthority(
					authorityID,
					FETCH_GROUPS_AUTHORITY,
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					new SubProgressMonitor(monitor, 20)));

			Collection<RoleGroupSetCarrier> roleGroupSetCarriers = RoleGroupDAO.sharedInstance().getRoleGroupSetCarriers(
					authorityID,
					FETCH_GROUPS_AUTHORIZED_OBJECT, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					FETCH_GROUPS_AUTHORITY, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, // was just fetched with exactly this and should be in the cache
					FETCH_GROUPS_ROLE_GROUP, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					new SubProgressMonitor(monitor, 35));

			authorizedObjects = new HashMap<AuthorizedObject, Boolean>(roleGroupSetCarriers.size());
			authorizedObjectID2authorizedObjectMap = new HashMap<AuthorizedObjectID, AuthorizedObject>(roleGroupSetCarriers.size());

			for (RoleGroupSetCarrier roleGroupSetCarrier : roleGroupSetCarriers) {
				// ignore the system authorizedObject - it always has all access rights anyway and cannot be configured
				AuthorizedObject ao = roleGroupSetCarrier.getAuthorizedObject();
				if (ao instanceof UserLocal && User.USER_ID_SYSTEM.equals(((UserLocal)ao).getUserID()))
					continue;

				AuthorizedObjectID aoid = (AuthorizedObjectID) JDOHelper.getObjectId(ao);
				assert aoid != null : "(AuthorizedObjectID) JDOHelper.getObjectId(ao) != null"; //$NON-NLS-1$
				authorizedObjectID2authorizedObjectMap.put(aoid, ao);
				authorizedObjects.put(ao, roleGroupSetCarrier.isInAuthority() ? Boolean.TRUE : Boolean.FALSE);

				RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel = new RoleGroupSecurityPreferencesModel();
				roleGroupSecurityPreferencesModel.setAllRoleGroupsInAuthority(roleGroupSetCarrier.getAllInAuthority());
				roleGroupSecurityPreferencesModel.setRoleGroupsAssignedDirectly(roleGroupSetCarrier.getAssignedToUser());
				roleGroupSecurityPreferencesModel.setRoleGroupsAssignedToUserGroups(roleGroupSetCarrier.getAssignedToUserGroups());
				roleGroupSecurityPreferencesModel.setRoleGroupsAssignedToOtherUser(roleGroupSetCarrier.getAssignedToOtherUser());
				roleGroupSecurityPreferencesModel.setInAuthority(roleGroupSetCarrier.isInAuthority());
				roleGroupSecurityPreferencesModel.setControlledByOtherUser(roleGroupSetCarrier.isControlledByOtherUser());

				roleGroupSecurityPreferencesModel.addModelChangeListener(roleGroupSecurityPreferencesModelChangeListener);
				authorizedObject2RoleGroupSecurityPreferencesModel.put(roleGroupSetCarrier.getAuthorizedObject(), roleGroupSecurityPreferencesModel);
				roleGroupSecurityPreferencesModel2AuthorizedObject.put(roleGroupSecurityPreferencesModel, roleGroupSetCarrier.getAuthorizedObject());
			}

			monitor.worked(5);
		}

		// check if our authorizedObjects have all fetch-groups we need by accessing some fields
		for (AuthorizedObject ao : authorizedObjects.keySet()) {
			ao.getUserSecurityGroups();
			if (ao instanceof UserSecurityGroup)
				((UserSecurityGroup)ao).getMembers();
		}

		this.inheritedSecuringAuthorityResolver = null;
		
		loadInheritanceData(new SubProgressMonitor(monitor, 20));
		
		monitor.done();
		
		propertyChangeSupport.firePropertyChange(PROPERTY_NAME_AUTHORITY_LOADED, null, authority);
	}

	private ModelChangeListener roleGroupSecurityPreferencesModelChangeListener = new ModelChangeListener() {
		@Override
		public void modelChanged(ModelChangeEvent event) {
			RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel = (RoleGroupSecurityPreferencesModel) event.getSource();
			changedModels.add(roleGroupSecurityPreferencesModel);
			AuthorizedObject authorizedObject = roleGroupSecurityPreferencesModel2AuthorizedObject.get(roleGroupSecurityPreferencesModel);
			if (authorizedObject == null)
				throw new IllegalStateException("roleGroupSecurityPreferencesModel2AuthorizedObject.get(roleGroupSecurityPreferencesModel) returned null!"); //$NON-NLS-1$

			if (authorizedObject instanceof UserSecurityGroup) {
				// A user-security-group has been modified - that affects all authorizedObjects that are members of this group!
				// Therefore, we need to recalculate their group-added role-groups.
				UserSecurityGroup group = (UserSecurityGroup) authorizedObject;
				for (AuthorizedObject ao : group.getMembers())
					recalculateAuthorizedObject_RoleGroupsAssignedToUserSecurityGroups(ao);
			}
			else if (authorizedObject instanceof UserLocal && User.USER_ID_OTHER.equals(((UserLocal)authorizedObject).getUserID())) {
				Set<RoleGroup> rightsOfOtherAuthorizedObject = new HashSet<RoleGroup>();
				if (roleGroupSecurityPreferencesModel.isInAuthority()) {
					rightsOfOtherAuthorizedObject.addAll(roleGroupSecurityPreferencesModel.getRoleGroupsAssignedDirectly());
					rightsOfOtherAuthorizedObject.addAll(roleGroupSecurityPreferencesModel.getRoleGroupsAssignedToUserGroups()); // not sure if the "_Other_" user can be in groups, but better assume that yes
				}

				// recalculate rights for all authorizedObjects that are neither directly nor via a user-security-group in this authority
				for (AuthorizedObject u : authorizedObjects.keySet()) {
					RoleGroupSecurityPreferencesModel m = authorizedObject2RoleGroupSecurityPreferencesModel.get(u);
					if (!m.isControlledByOtherUser())
						continue;

					m.setRoleGroupsAssignedToOtherUser(rightsOfOtherAuthorizedObject);
				}
			}

			propertyChangeSupport.firePropertyChange(PROPERTY_NAME_ROLE_GROUP_SECURITY_PREFERENCES_MODEL_CHANGED, null, roleGroupSecurityPreferencesModel);
		}
	};

	public AuthorityTypeID getAuthorityTypeID() {
		return authorityTypeID;
	}
	public AuthorityType getAuthorityType() {
		return authorityType;
	}
	public AuthorityID getAuthorityID() {
		return authorityID;
	}
	public Authority getAuthority() {
		return authority;
	}

	private Map<AuthorizedObject, RoleGroupSecurityPreferencesModel> authorizedObject2RoleGroupSecurityPreferencesModel = new HashMap<AuthorizedObject, RoleGroupSecurityPreferencesModel>();
	private Map<RoleGroupSecurityPreferencesModel, AuthorizedObject> roleGroupSecurityPreferencesModel2AuthorizedObject = new HashMap<RoleGroupSecurityPreferencesModel, AuthorizedObject>();

	private Map<AuthorizedObjectID, AuthorizedObject> authorizedObjectID2authorizedObjectMap = new HashMap<AuthorizedObjectID, AuthorizedObject>();
	private Map<AuthorizedObject, Boolean> authorizedObjects = new HashMap<AuthorizedObject, Boolean>();
	private Set<RoleGroupSecurityPreferencesModel> changedModels = new HashSet<RoleGroupSecurityPreferencesModel>();

	/**
	 * Get a read-only mapping from {@link AuthorizedObject} to {@link RoleGroupSecurityPreferencesModel}.
	 * The contents of this {@link Map} are the same as those in the <code>Map</code> returned by
	 * {@link #getRoleGroupSecurityPreferencesModel2AuthorizedObject()}.
	 *
	 * @return a read-only {@link Map}.
	 */
	public Map<AuthorizedObject, RoleGroupSecurityPreferencesModel> getAuthorizedObject2RoleGroupSecurityPreferencesModel() {
		return Collections.unmodifiableMap(authorizedObject2RoleGroupSecurityPreferencesModel);
	}

	/**
	 * Get a read-only mapping from {@link RoleGroupSecurityPreferencesModel} to {@link AuthorizedObject}.
	 * This <code>Map</code> contains exactly the same records as the <code>Map</code>
	 * returned by {@link #getAuthorizedObject2RoleGroupSecurityPreferencesModel()} - only the key and value
	 * is switched for each record.
	 *
	 * @return a read-only {@link Map}.
	 */
	public Map<RoleGroupSecurityPreferencesModel, AuthorizedObject> getRoleGroupSecurityPreferencesModel2AuthorizedObject() {
		return Collections.unmodifiableMap(roleGroupSecurityPreferencesModel2AuthorizedObject);
	}

	/**
	 * Get all authorizedObjects with a flag indicating whether they are in the authority at the moment the data is loaded.
	 * This flag does not change, when
	 * {@link #addAuthorizedObjectToAuthority(AuthorizedObject)} or {@link #removeAuthorizedObjectFromAuthority(AuthorizedObject)} is called. It only changes,
	 * when data was stored to the server and {@link #load(AuthorityTypeID, AuthorityID, Authority, ProgressMonitor)} has
	 * been called again.
	 *
	 * @return all authorizedObjects of the local organisation with a flag indicating whether they are in the current authority or not.
	 */
	public Map<AuthorizedObject, Boolean> getAuthorizedObjects() {
		return Collections.unmodifiableMap(authorizedObjects);
	}

	public List<Map.Entry<AuthorizedObject, Boolean>> createModifiableAuthorizedObjectList()
	{
		List<Map.Entry<AuthorizedObject, Boolean>> result = new ArrayList<Map.Entry<AuthorizedObject,Boolean>>(authorizedObjects.size());

		for (Map.Entry<AuthorizedObject, Boolean> me : authorizedObjects.entrySet())
			result.add(new AuthorizedObjectBooleanMapEntry(me.getKey(), me.getValue()));

		return result;
	}

	private class AuthorizedObjectBooleanMapEntry implements Map.Entry<AuthorizedObject, Boolean>
	{
		private AuthorizedObject key;
		private Boolean value;

		public AuthorizedObjectBooleanMapEntry(AuthorizedObject key, Boolean value) {
			if (key == null)
				throw new IllegalArgumentException("key must not be null!"); //$NON-NLS-1$
			if (value == null)
				throw new IllegalArgumentException("value must not be null!"); //$NON-NLS-1$

			this.key = key;
			this.value = value;
		}

		@Override
		public AuthorizedObject getKey() {
			return key;
		}

		@Override
		public Boolean getValue() {
			return value;
		}

		@Override
		public Boolean setValue(Boolean value) {
			if (value == null)
				throw new IllegalArgumentException("value must not be null!"); //$NON-NLS-1$

			Boolean oldValue = this.value;

			if (!value.equals(oldValue)) {
				if (value.booleanValue())
					addAuthorizedObjectToAuthority(key);
				else
					removeAuthorizedObjectFromAuthority(key);

				this.value = value;
			}

			return oldValue;
		}
	}

	public void addAuthorizedObjectToAuthority(AuthorizedObject authorizedObject)
	{
		if (authority == null)
			throw new IllegalStateException("authority == null"); //$NON-NLS-1$
		if (authorizedObject == null)
			throw new IllegalArgumentException("authorizedObject == null"); //$NON-NLS-1$

		RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel = authorizedObject2RoleGroupSecurityPreferencesModel.get(authorizedObject);
		// replace the authorizedObject by our internal one (where we are sure about fetch-groups
		authorizedObject = roleGroupSecurityPreferencesModel2AuthorizedObject.get(roleGroupSecurityPreferencesModel);

		roleGroupSecurityPreferencesModel.beginDeferModelChangedEvents();
		try {
			roleGroupSecurityPreferencesModel.setInAuthority(true);
			roleGroupSecurityPreferencesModel.setControlledByOtherUser(false);

			if (authorizedObject instanceof UserSecurityGroup) {
				// authorizedObject is a group => recalculate rights of members
				Set<RoleGroup> emptyRoleGroups = Collections.emptySet();
				UserSecurityGroup userSecurityGroup = (UserSecurityGroup) authorizedObject;
				for (AuthorizedObject member : userSecurityGroup.getMembers()) {
					RoleGroupSecurityPreferencesModel m = authorizedObject2RoleGroupSecurityPreferencesModel.get(member);
					m.setControlledByOtherUser(false);
					m.setRoleGroupsAssignedToOtherUser(emptyRoleGroups);

					recalculateAuthorizedObject_RoleGroupsAssignedToUserSecurityGroups(member);
				}
			}
			else if (authorizedObject instanceof UserLocal && User.USER_ID_OTHER.equals(((UserLocal)authorizedObject).getUserID())) {
				// authorizedObject is the special user "_Other_" => recalculate rights of users which are not in authority
				Set<RoleGroup> rightsOfOtherAuthorizedObject = new HashSet<RoleGroup>();
				rightsOfOtherAuthorizedObject.addAll(roleGroupSecurityPreferencesModel.getRoleGroupsAssignedDirectly());
				rightsOfOtherAuthorizedObject.addAll(roleGroupSecurityPreferencesModel.getRoleGroupsAssignedToUserGroups()); // not sure if it can be in groups, but better assume that yes
				Set<RoleGroup> emptySet = Collections.emptySet();
				for (AuthorizedObject u : authorizedObjects.keySet()) {
					RoleGroupSecurityPreferencesModel m = authorizedObject2RoleGroupSecurityPreferencesModel.get(u);

					if (m.isControlledByOtherUser())
						m.setRoleGroupsAssignedToOtherUser(rightsOfOtherAuthorizedObject);
					else
						m.setRoleGroupsAssignedToOtherUser(emptySet);
				}
			}
		} finally {
			roleGroupSecurityPreferencesModel.endDeferModelChangedEvents();
		}

		propertyChangeSupport.firePropertyChange(PROPERTY_NAME_USER_ADDED, null, authorizedObject);
	}

	private void recalculateAuthorizedObject_RoleGroupsAssignedToUserSecurityGroups(AuthorizedObject authorizedObject)
	{
		Set<RoleGroup> roleGroupsAssignedToUserSecurityGroups = new HashSet<RoleGroup>();
		for (UserSecurityGroup userSecurityGroup : authorizedObject.getUserSecurityGroups()) {
			RoleGroupSecurityPreferencesModel m = authorizedObject2RoleGroupSecurityPreferencesModel.get(userSecurityGroup);
			if (m.isInAuthority()) {
				roleGroupsAssignedToUserSecurityGroups.addAll(m.getRoleGroupsAssignedDirectly());
				roleGroupsAssignedToUserSecurityGroups.addAll(m.getRoleGroupsAssignedToUserGroups()); // I don't think we should support nested groups, but if we do one day, this line is important
			}
		}
		authorizedObject2RoleGroupSecurityPreferencesModel.get(authorizedObject).setRoleGroupsAssignedToUserGroups(roleGroupsAssignedToUserSecurityGroups);
	}

	public void removeAuthorizedObjectFromAuthority(AuthorizedObject authorizedObject)
	{
		if (authority == null)
			throw new IllegalStateException("authority == null"); //$NON-NLS-1$
		if (authorizedObject == null)
			throw new IllegalArgumentException("authorizedObject == null"); //$NON-NLS-1$

		RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel = authorizedObject2RoleGroupSecurityPreferencesModel.get(authorizedObject);
		// replace the authorizedObject by our internal one (where we are sure about fetch-groups
		authorizedObject = roleGroupSecurityPreferencesModel2AuthorizedObject.get(roleGroupSecurityPreferencesModel);

		roleGroupSecurityPreferencesModel.beginDeferModelChangedEvents();
		try {
			roleGroupSecurityPreferencesModel.setInAuthority(false);

			if (!resolveAuthorizedObjectHasUserSecurityGroupInAuthority(authorizedObject)) {
				if (!(authorizedObject instanceof UserSecurityGroup)) // user-groups are not controlled by the other-user
					roleGroupSecurityPreferencesModel.setControlledByOtherUser(true);
			}

			if (authorizedObject instanceof UserSecurityGroup) {
				UserSecurityGroup userSecurityGroup = (UserSecurityGroup) authorizedObject;
				for (AuthorizedObject member : userSecurityGroup.getMembers()) {
					RoleGroupSecurityPreferencesModel m = authorizedObject2RoleGroupSecurityPreferencesModel.get(member);
					if (!m.isInAuthority()) {
						// the authorizedObject u is not directly in this authority - is one of its groups in the authority?
						if (!resolveAuthorizedObjectHasUserSecurityGroupInAuthority(member)) {
							if (!(member instanceof UserSecurityGroup)) { // user-groups are not controlled by the other-user
								m.setControlledByOtherUser(true);
								AuthorizedObject otherUser = authorizedObjectID2authorizedObjectMap.get(UserLocalID.create(authority.getOrganisationID(), User.USER_ID_OTHER, authority.getOrganisationID()));
								RoleGroupSecurityPreferencesModel otherModel = authorizedObject2RoleGroupSecurityPreferencesModel.get(otherUser);
								Set<RoleGroup> roleGroupsAssignedToOtherUser = new HashSet<RoleGroup>(otherModel.getRoleGroupsAssignedDirectly().size() + otherModel.getRoleGroupsAssignedToUserGroups().size());
								roleGroupsAssignedToOtherUser.addAll(otherModel.getRoleGroupsAssignedDirectly());
								roleGroupsAssignedToOtherUser.addAll(otherModel.getRoleGroupsAssignedToUserGroups());
								m.setRoleGroupsAssignedToOtherUser(roleGroupsAssignedToOtherUser);
							}
						}
					}

					recalculateAuthorizedObject_RoleGroupsAssignedToUserSecurityGroups(member);
				}
			}
			else if (authorizedObject instanceof UserLocal && User.USER_ID_OTHER.equals(((UserLocal)authorizedObject).getUserID())) {
				Set<RoleGroup> emptySet = Collections.emptySet();
				for (RoleGroupSecurityPreferencesModel m : authorizedObject2RoleGroupSecurityPreferencesModel.values())
					m.setRoleGroupsAssignedToOtherUser(emptySet);
			}
		} finally {
			roleGroupSecurityPreferencesModel.endDeferModelChangedEvents();
		}

		propertyChangeSupport.firePropertyChange(PROPERTY_NAME_USER_REMOVED, null, authorizedObject);
	}

	/**
	 * @param authorizedObject the authorizedObject
	 * @return <code>true</code>, if the authorizedObject has at least one user-security-group in the current authority. <code>false</code>, if none of the authorizedObject's groups is in this authority.
	 */
	private boolean resolveAuthorizedObjectHasUserSecurityGroupInAuthority(AuthorizedObject authorizedObject)
	{
		for (UserSecurityGroup group : authorizedObject.getUserSecurityGroups()) {
			if (authorizedObject2RoleGroupSecurityPreferencesModel.get(group).isInAuthority())
				return true;
		}
		return false;
	}

	/**
	 * This method assigns the securing authority to the server, if {@link #isAssignSecuringAuthorityRequested()}
	 * returns <code>true</code>. Otherwise, it returns without writing to the server. It clears the flag
	 * {@link #assignSecuringAuthorityRequested}.
	 *
	 * @param monitor the monitor for progress feedback
	 */
	protected void assignSecuringAuthority(ProgressMonitor monitor)
	{
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.AuthorityPageControllerHelper.job.assignAuthority"), 100); //$NON-NLS-1$
		try {
			if (assignSecuringAuthorityRequested) {
				AuthorityDAO.sharedInstance().assignSecuringAuthority(
						JDOHelper.getObjectId(securedObject), assignSecuringAuthorityID, assignSecuringAuthorityInherited,
						new SubProgressMonitor(monitor, 100));

				assignSecuringAuthorityRequested = false;
			}
			else
				monitor.worked(100);
		} finally {
			monitor.done();
		}
	}

	public synchronized void store(ProgressMonitor monitor)
	{
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.AuthorityPageControllerHelper.job.saveAuthority"), 200); //$NON-NLS-1$
		try {
			if (this.securedObject == null)
				throw new IllegalStateException("this.securedObject == null"); //$NON-NLS-1$
			if (this.authorityType == null)
				throw new IllegalStateException("this.authorityType == null"); //$NON-NLS-1$
//			if (this.authority == null) // NULL IS LEGAL WHEN NO AUTHORITY IS ASSIGNED (NONE IS ALLOWED)!!!!!!! Marco.
//				throw new IllegalStateException("this.authority == null"); //$NON-NLS-1$

// TODO DataNucleus WORKAROUND: neither Authority nor AuthorityName get dirty when the names map is modified. Temporarily storing in any case.
// TODO we need an NLJDOHelper.isDirtyRecursively(...) method that checks a whole object graph.
//			if (authorityID == null || JDOHelper.isDirty(authority)) {

			if (this.authority == null) {
				authorityID = null;
			}
			else {
				Authority a = AuthorityDAO.sharedInstance().storeAuthority(
						authority,
						true,
						FETCH_GROUPS_AUTHORITY,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new SubProgressMonitor(monitor, 20)
				);
				AuthorityID aid = (AuthorityID) JDOHelper.getObjectId(a);
				if (aid == null)
					throw new IllegalStateException("Authority returned by server does not have an object-id assigned!"); //$NON-NLS-1$

				authorityID = aid;
				authority = a;
			}

				if (assignSecuringAuthorityRequested && assignSecuringAuthorityID == null && authorityID != null)
					assignSecuringAuthorityID = authorityID;
//			}
//			else
//				monitor.worked(20);

////			Set<AuthorizedObjectID> authorizedObjectIDsToRemove = NLJDOHelper.getObjectIDSet(authorizedObjectsToRemove);
////			AuthorizedObjectDAO.sharedInstance().removeAuthorizedObjectsFromAuthority(
////					authorizedObjectIDsToRemove,
////					authorityID,
////					new SubProgressMonitor(monitor, 10)
////			);
//			authorizedObjectsToRemove.clear();
//
////			Set<AuthorizedObjectID> authorizedObjectIDsToAdd = NLJDOHelper.getObjectIDSet(authorizedObjectsToAdd);
////			AuthorizedObjectDAO.sharedInstance().removeAuthorizedObjectsFromAuthority(
////					authorizedObjectIDsToAdd,
////					authorityID,
////					new SubProgressMonitor(monitor, 10)
////			);
//			authorizedObjectsToAdd.clear();

			{
				int ticksForThisWorkPart = 80;

				if (changedModels.isEmpty())
					monitor.worked(ticksForThisWorkPart);
				else {
					int ticksPerModel = ticksForThisWorkPart / changedModels.size();
					int ticksDone = 0;
					for (RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel : changedModels) {
						AuthorizedObject authorizedObject = roleGroupSecurityPreferencesModel2AuthorizedObject.get(roleGroupSecurityPreferencesModel);
						AuthorizedObjectID authorizedObjectID = (AuthorizedObjectID) JDOHelper.getObjectId(authorizedObject);
						if (authorizedObjectID == null)
							throw new IllegalStateException("JDOHelper.getObjectId(authorizedObject) returned null for authorizedObject: " + authorizedObject); //$NON-NLS-1$


						Set<RoleGroupID> roleGroupIDs = null;

						if (roleGroupSecurityPreferencesModel.isInAuthority()) {
							roleGroupIDs = NLJDOHelper.getObjectIDSet(
									roleGroupSecurityPreferencesModel.getRoleGroupsAssignedDirectly()
							);
						}

						// roleGroupIDs being null means that the authorizedObject is removed from the authority by the following call.
						AuthorizedObjectDAO.sharedInstance().setGrantedRoleGroups(authorizedObjectID, authorityID, roleGroupIDs,
								new SubProgressMonitor(monitor, ticksPerModel));

						ticksDone += ticksPerModel;
					}
					changedModels.clear();

					int ticksLeft = ticksForThisWorkPart - ticksDone; // maybe there is some left
					if (ticksLeft > 0)
						monitor.worked(ticksLeft);
				}
			}

			// assign the new securingAuthority (if necessary)
			assignSecuringAuthority(new SubProgressMonitor(monitor, 10));

			
			if (securedObjectInheritable != null) {
				// this is most likely in the cache and might have changed above,
				// so we have to invalidate it in the cache *now*, we can't wait for the notification mechanism
				Cache.sharedInstance().removeByObjectID(JDOHelper.getObjectId(securedObjectInheritable), false);
			}
			// reload everything
			load(authorityTypeID, authorityID, null, new SubProgressMonitor(monitor, 90));
		} finally {
			monitor.done();
		}
	}

	//////////////////
	// BEGIN stuff for assigning a new authority
	//////////////////

	/**
	 * Indicates whether the property <code>securingAuthorityID</code> of the <code>SecuredObject</code> shall be modified
	 * on the server when the contents of this page are saved. If this method returns <code>true</code>, your implementation of
	 * {@link IEntityEditorPageController} (preferably a subclass of {@link ActiveEntityEditorPageController}) used to manage the
	 * {@link SecuredObject} shall assign the {@link Authority} by a call to
	 * {@link AuthorityDAO#assignSecuringAuthority(Object, AuthorityID, boolean, org.nightlabs.progress.ProgressMonitor)}. Note,
	 * that this method should be called after
	 */
	private boolean assignSecuringAuthorityRequested;

	/**
	 * @see #getAssignSecuringAuthorityID()
	 */
	private AuthorityID assignSecuringAuthorityID;

	/**
	 * Get the id of the newly assigned authority. This can be <code>null</code> in order to indicate that the
	 * property <code>securingAuthorityID</code> of the <code>SecuredObject</code> shall be set to <code>null</code>.
	 *
	 * @return <code>null</code> or the new authority-id.
	 */
	public AuthorityID getAssignSecuringAuthorityID() {
		return assignSecuringAuthorityID;
	}

	/**
	 * Get whether the authority will be set to be inherited from the parent.
	 * @return <code>true</code> if the authority should be inherited and the assignment has been requested, <code>false</code> otherwise.
	 */
	public boolean isAssignSecuringAuthorityInherited() {
		return assignSecuringAuthorityInherited;
	}

	public boolean isAssignSecuringAuthorityRequested() {
		return assignSecuringAuthorityRequested;
	}

	public void setAssignSecuringAuthority(AuthorityID newAuthorityID, boolean inherited) {
		this.assignSecuringAuthorityID = newAuthorityID;
		this.assignSecuringAuthorityInherited = inherited;
		assignSecuringAuthorityRequested = true;
	}

	private boolean assignSecuringAuthorityInherited;

	//////////////////
	// BEGIN PropertyChangeSupport
	//////////////////
	/**
	 * The {@link #load(AuthorityTypeID, AuthorityID, Authority, ProgressMonitor)} method has been called (and is finished).
	 * The loaded authority can be accessed by {@link PropertyChangeEvent#getNewValue()}.
	 */
	public static final String PROPERTY_NAME_AUTHORITY_LOADED = "authorityLoaded"; //$NON-NLS-1$

	/**
	 * A {@link PropertyChangeEvent} with this property name is fired, when a authorizedObject has been removed from the
	 * currently managed {@link Authority}. The affected authorizedObject object can be accessed by
	 * {@link PropertyChangeEvent#getNewValue()}.
	 */
	public static final String PROPERTY_NAME_USER_REMOVED = "authorizedObjectRemoved"; //$NON-NLS-1$

	/**
	 * A {@link PropertyChangeEvent} with this property name is fired, when a authorizedObject has been added to the
	 * currently managed {@link Authority}. The affected authorizedObject object can be accessed by
	 * {@link PropertyChangeEvent#getNewValue()}.
	 */
	public static final String PROPERTY_NAME_USER_ADDED = "authorizedObjectAdded"; //$NON-NLS-1$

	/**
	 * A {@link PropertyChangeEvent} with this property name is fired, when a {@link RoleGroupSecurityPreferencesModel}
	 * has been changed which is part of the currently managed {@link Authority}. The affected {@link RoleGroupSecurityPreferencesModel}
	 * can be accessed by {@link PropertyChangeEvent#getNewValue()}.
	 * <p>
	 * The affected authorizedObject can be obtained via the <code>Map</code> returned by {@link #getRoleGroupSecurityPreferencesModel2AuthorizedObject()}.
	 * </p>
	 */
	public static final String PROPERTY_NAME_ROLE_GROUP_SECURITY_PREFERENCES_MODEL_CHANGED = "roleGroupSecurityPreferencesModelChanged"; //$NON-NLS-1$

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName,
				listener);
	}
	//////////////////
	// END PropertyChangeSupport
	//////////////////

	/**
	 * This is set to <code>null</code> on {@link #load(AuthorityTypeID, AuthorityID, Authority, ProgressMonitor)}
	 * so after load the inheritance data can be reloaded.
	 */
	private volatile InheritedSecuringAuthorityResolver inheritedSecuringAuthorityResolver;
	private Inheritable securedObjectInheritable;
	
	/**
	 * Get the {@link InheritedSecuringAuthorityResolver} which is used to find out the {@link Authority} that is assigned to the
	 * parent-{@link SecuredObject} of that <code>SecuredObject</code> that is currently edited.
	 * This method can return <code>null</code> if there is no inheritance mechanism implemented
	 * for the <code>SecuredObject</code> in the concrete use case.
	 *
	 * @return an instance of <code>InheritedSecuringAuthorityResolver</code> or <code>null</code>, if there is no inheritance mechanism.
	 */
	protected abstract InheritedSecuringAuthorityResolver createInheritedSecuringAuthorityResolver();

	/**
	 * Get the current {@link InheritedSecuringAuthorityResolver} for this helper.
	 * Note, that this method might return <code>null</code>, check {@link #isManageInheritance()}
	 * to see if this helper manages inheritance.
	 * @return The current instance of {@link InheritedSecuringAuthorityResolver} or <code>null</code> if {@link #isManageInheritance()} is <code>false</code>.
	 */
	public InheritedSecuringAuthorityResolver getInheritedSecuringAuthorityResolver() {
		if (inheritedSecuringAuthorityResolver == null) {
			synchronized (this) {
				if (inheritedSecuringAuthorityResolver == null) {
					inheritedSecuringAuthorityResolver = createInheritedSecuringAuthorityResolver();
				}
			}
		}
		return inheritedSecuringAuthorityResolver;
	}
	
	private void loadInheritanceData(ProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.AuthorityPageControllerHelper.task.loadInheritance"), 2); //$NON-NLS-1$
		inheritedSecuringAuthorityResolver = null;
		securedObjectInheritable = null;
		InheritedSecuringAuthorityResolver resolver = getInheritedSecuringAuthorityResolver();
		if (resolver != null) {
			monitor.worked(1);
			securedObjectInheritable = resolver.retrieveSecuredObjectInheritable(monitor);
			monitor.worked(1);
		}
		monitor.done();
	}
	
	public Inheritable getSecuredObjectInheritable() {
		return securedObjectInheritable;
	}
	
	public boolean isAuthorityInitiallyInherited() {
		if (securedObjectInheritable != null) {
			FieldMetaData fmd = securedObjectInheritable.getFieldMetaData(SecuredObject.FieldName.securingAuthorityID);
			if (fmd != null) {
				return fmd.isValueInherited();
			}
		}
		return false;
	}
	
	/**
	 * Check, if this helper manages inheritance, if this is <code>true</code> {@link #getInheritedSecuringAuthorityResolver()}
	 * should not return <code>null</code>.
	 */
	public boolean isManageInheritance() {
		return getInheritedSecuringAuthorityResolver() != null;
	}
}