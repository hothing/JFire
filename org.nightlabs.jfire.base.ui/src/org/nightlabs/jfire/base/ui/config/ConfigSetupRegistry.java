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

package org.nightlabs.jfire.base.ui.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.nightlabs.base.ui.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.ui.extensionpoint.EPProcessorException;
import org.nightlabs.base.ui.notification.NotificationAdapterJob;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.UserConfigSetup;
import org.nightlabs.jfire.config.WorkstationConfigSetup;
import org.nightlabs.jfire.config.dao.ConfigSetupDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigSetupID;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ConfigSetupRegistry extends AbstractEPProcessor
{
	private static final String CLASS_ELEMENT = "class"; //$NON-NLS-1$
	private static final String CONFIG_SETUP_TYPE_ELEMENT = "configSetupType"; //$NON-NLS-1$
	private static final String VISUALISER_ELEMENT = "visualiser"; //$NON-NLS-1$

	public static final String EXTENSION_POINT_ID = "org.nightlabs.jfire.base.ui.configsetupvisualiser"; //$NON-NLS-1$

	private static final String[] CONFIG_SETUP_FETCH_GROUPS = new String[]
	  { FetchPlan.DEFAULT, ConfigSetup.FETCH_GROUP_CONFIG_MODULE_CLASSES };
//	private static final String[] DEFAULT_FETCH_GROUP_CONFIGS = new String[]
//    { FetchPlan.DEFAULT, Config.FETCH_GROUP_CONFIG_GROUP };

	/**
	 * IMPORTANT: The following registry does only work correctly if the following properties are true...<br>
	 *
	 * <p>There can be at most one ConfigSetup linked to a given <code>Objectclass</code> in the JDO-Datastore.</p>
	 * <p>This means that there is at most one ConfigSetup with the <code>configType</code> (exclusive)or
	 * 		<code>groupConfigType</code> equal to <code>Objectclass</code>.</p>
	 */

	/**
	 * key: String ConfigSetup.configType
	 * value: ConfigSetup configSetup
	 */
	private final Map<String, ConfigSetup> configSetupsByType = null;

	/**
	 * key: String ConfigSetup.groupConfigType
	 */
	private final Map<String, ConfigSetup> configSetupsByGroupType = null;

	/**
	 * key: String configSetupType
	 * value: ConfigSetupVisualiser setupVisualiser
	 */
	private final Map<String, ConfigSetupVisualiser> setupVisualiserByType = new HashMap<String, ConfigSetupVisualiser>();

	/**
	 * key: String configSetupType
	 * value: ConfigPreferencesNode mergedTreeNode
	 */
	private final Map<String, ConfigPreferenceNode> mergedTreeNodes = new HashMap<String, ConfigPreferenceNode>();

	/**
	 *
	 */
	public ConfigSetupRegistry() {
		super();
	}

	/**
	 * Returns a merged tree of ConfigPreferenceNodes.
	 * The set contains all registered PreferencePages that edit a ConfigModule
	 * registered in the ConfigSetup holding Configs with a configType as of the given configID.
	 * Additionally a ConfigPreferenceNode for all remaining ConfigModuleClasses
	 * in the found ConfigSetup will be added to the returned node, but these
	 * won't contain a PreferencePage and therefore will not be editable.
	 */
	public ConfigPreferenceNode getMergedPreferenceRootNode(String scope, ConfigID configID, ProgressMonitor monitor)
	throws NoSetupPresentException
	{
		ConfigSetup setup = ConfigSetupDAO.sharedInstance().getConfigSetupForConfigType(configID, monitor);
		if (setup == null)
			throw new NoSetupPresentException("No Setup found related to this configID: "+configID); //$NON-NLS-1$

		ConfigPreferenceNode rootNode = mergedTreeNodes.get(scope+setup.getConfigSetupType());
		if (rootNode != null)
			return rootNode;
		ConfigPreferenceNode registeredRootNode = ConfigPreferencePageRegistry.sharedInstance().getPreferencesRootNode();
		rootNode = new ConfigPreferenceNode("", "", "", null, null, null, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		Set<String> mergeModules = new HashSet<String>();
		mergeModules.addAll(setup.getConfigModuleClasses());

		// Merge recursively
		for (ConfigPreferenceNode childNode : registeredRootNode.getChildren())
			mergeSetupNodes(setup, mergeModules, childNode, rootNode);

		// For all remaining classes add a null-Node
		for (String moduleClassName : mergeModules) {
			ConfigPreferenceNode node = new ConfigPreferenceNode("", moduleClassName, "", rootNode, null, null, null); //$NON-NLS-1$ //$NON-NLS-2$
			rootNode.addChild(node);
		}

		mergedTreeNodes.put(scope+setup.getConfigSetupType(), rootNode);
		return rootNode;
	}

	/**
	 * Private helper that recursively adds registrations of ConfigPreferencePages
	 * to a new ConfigPreferenceNode if the given ConfigSetup has a registration
	 * for the appropriate ConfigModule-class. Stops in the tree when no registration
	 * was found in the setup, so the further in the tree even if adequate will
	 * not be found.
	 */
	private void mergeSetupNodes(
			ConfigSetup setup,
			Set<String> mergeModules,
			ConfigPreferenceNode orgNode,
			ConfigPreferenceNode newNodeParent)
	{
		String nodeClassName = orgNode.getConfigModuleClass() != null ? orgNode.getConfigModuleClass().getName() : "";  //$NON-NLS-1$
		boolean hasRegistration = setup.getConfigModuleClasses().contains(nodeClassName);

		if (hasRegistration) { // <-- Needs additional checking here to determine if we should add the item into the tree (see comments below).
			mergeModules.remove(nodeClassName);

			// >> ]Kai >> From observation:
			// It seems here that is not enough to just check the class name against the registry in the ConfigSetup. For example,
			// BOTH UserConfigSetup and WorkstationConfigSetup have the same class name 'org.nightlabs.jfire.accounting.pay.config.ModeOfPaymentConfigModule'
			// in their registries. And since the ModeOfPaymentConfigModule for BOTH User and Workstation have the same class name, it incidentally follows
			// that we loaded both of them as well.
			// --> We can definitely check the instance of the ConfigSetup to determine whether it is a User or Workstation ConfigSetup.
			// --> However, if we can get access to orgNode's ConfigModule, then we can immediately query through its getConfigType(), to see if it is attached to
			//     either the UserConfigSetup or the WorkstationConfigSetup. But I cant seem to be able to get the ConfigModule from the given parameters.
			//
			// Now, without having to load the ConfigModule, we can use the following fact (sounds like a work around):
			//   That there exists a mutually exclusive behaviour between the User and Workstation pages. That is, the page displaying 'User Configuration Settings'
			//   should not display options with 'Workstation'-related items on its treenodes. And similarly, the 'Workstation Feature Configuration' should not
			//   display options with 'User'-related items.
			//
			// Thus, we add an additional checking for this 'mutually exclusive' behaviour, based only on the information we currently have:
			//   - Using the fact of the mutually exclusive and specific conditions between Workstation (org.nightlabs.jfire.workstation.Workstation)
			//     and User (org.nightlabs.jfire.security.User).
			String orgConfigPrefID = orgNode.getConfigPreferenceID();
			boolean isMutuallyExclusive = setup instanceof UserConfigSetup && !orgConfigPrefID.contains("Workstation") //$NON-NLS-1$
			                              || setup instanceof WorkstationConfigSetup && !orgConfigPrefID.contains("User"); //$NON-NLS-1$

			if (isMutuallyExclusive) {
				ConfigPreferenceNode newNode = new ConfigPreferenceNode(
						orgNode.getConfigPreferenceID(),
						orgNode.getConfigPreferenceName(),
						orgNode.getCategoryID(),
						newNodeParent,
						orgNode.getElement(),
						orgNode.getPreferencePage(),
						null // FIXME: insert here the modID stuff?
					);

				newNodeParent.addChild(newNode);

				for (ConfigPreferenceNode child : orgNode.getChildren())
					mergeSetupNodes(setup, mergeModules, child, newNode);
			}

		}
	}

	/**
	 * Listener for changes of ConfigSetups.
	 */
	private final NotificationListener setupChangeListener = new NotificationAdapterJob() {
		public void notify(NotificationEvent notificationEvent) {
			if (notificationEvent.getFirstSubject() instanceof DirtyObjectID) {
				DirtyObjectID dirtyObjectID = (DirtyObjectID) notificationEvent.getFirstSubject();
				if (dirtyObjectID.getObjectID() instanceof ConfigSetupID) {
					ConfigSetupID setupID = (ConfigSetupID)dirtyObjectID.getObjectID();
					try {
						ConfigSetup newSetup = ConfigSetupDAO.sharedInstance().getConfigSetup(setupID,
								CONFIG_SETUP_FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, getProgressMonitor());
//						integrateConfigSetup(newSetup);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	};


	/**
	 * Returns a ConfigSetupVisualiser for the ConfigSetup of the given
	 * configSetupType or null if none can be found.
	 */
	public ConfigSetupVisualiser getVisualiser(String configSetupType) {
		return setupVisualiserByType.get(configSetupType);
	}

	/**
	 * Returns the visualiser assosiated to the ConfigSetup the given Config
	 * is part of, or null if it can't be found.
	 */
	public ConfigSetupVisualiser getVisualiserForConfig(ConfigID configID, ProgressMonitor monitor) {
		ConfigSetup setup = ConfigSetupDAO.sharedInstance().getConfigSetupForConfigType(configID, monitor);
		if (setup == null)
			return null;
		return getVisualiser(setup.getConfigSetupType());
	}

	private static ConfigSetupRegistry sharedInstance;

	public static ConfigSetupRegistry sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new ConfigSetupRegistry();
			JDOLifecycleManager.sharedInstance().addNotificationListener(ConfigSetup.class, sharedInstance.setupChangeListener);
//			JDOLifecycleManager.sharedInstance().addNotificationListener(Config.class, sharedInstance.configChangeListener);
//			JDOLifecycleManager.sharedInstance().addNotificationListener(ConfigGroup.class, sharedInstance.configGroupChangeListener);
			// FIXME: where are these listeners deregistered?
			sharedInstance.process();
		}
		return sharedInstance;
	}

	@Override
	public String getExtensionPointID() {
		return EXTENSION_POINT_ID;
	}

	@Override
	public void processElement(IExtension extension, IConfigurationElement element) throws Exception {
		if (element.getName().equals(VISUALISER_ELEMENT)) {
			String configSetupType = element.getAttribute(CONFIG_SETUP_TYPE_ELEMENT);
			if (configSetupType == null || "".equals(configSetupType)) //$NON-NLS-1$
				throw new EPProcessorException("Attribute configSetupType is invalid for a configsetupvisualiser"); //$NON-NLS-1$
			ConfigSetupVisualiser visualiser = null;
			try {
				visualiser = (ConfigSetupVisualiser)element.createExecutableExtension(CLASS_ELEMENT);
			} catch (CoreException e) {
				throw new EPProcessorException("Could not instatiate ConfigSetupVisualiser",e); //$NON-NLS-1$
			}
			setupVisualiserByType.put(configSetupType, visualiser);
		}
	}

	public class NoSetupPresentException extends Exception {
		private static final long serialVersionUID = 1L;

		public NoSetupPresentException() {
			super();
		}

		public NoSetupPresentException(String message) {
			super(message);
		}

		public NoSetupPresentException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
