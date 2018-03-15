package org.nightlabs.jfire.base.admin.ui.configgroup;

import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.id.ConfigID;

class ConfigGroupModel {
	ConfigID configGroupID;
	
	Set<Config> assignedConfigs = new HashSet<Config>();
	Set<Config> notAssignedConfigs = new HashSet<Config>();
	
	Set<Config> removedConfigs = new HashSet<Config>();
	Set<Config> addedConfigs = new HashSet<Config>();
}
