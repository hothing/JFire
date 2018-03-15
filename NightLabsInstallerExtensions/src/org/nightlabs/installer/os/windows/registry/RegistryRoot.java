package org.nightlabs.installer.os.windows.registry;

public enum RegistryRoot
{
	HKEY_LOCAL_MACHINE("HKLM"), // = HKey_Local_machine (default)
	HKEY_CURRENT_USER("HKCU"), // = HKey_current_user
	HKEY_USERS("HKU"), //  = HKey_users
	HKEY_CLASSES_ROOT("HKCR"); // = HKey_classes_root

	private RegistryRoot(String shortForm) { this.shortForm = shortForm; }
	private String shortForm;
	public String getShortForm() { return this.shortForm; }
}