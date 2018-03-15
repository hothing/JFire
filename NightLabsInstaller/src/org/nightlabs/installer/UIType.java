package org.nightlabs.installer;

/**
 * Available user interface types for the installer.
 * 
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @version $Revision: 1570 $ - $Date: 2009-01-18 19:17:13 +0100 (So, 18 Jan 2009) $
 */
public enum UIType
{
	/**
	 * Installation with console UI..
	 */
	console,
	
	/**
	 * Installation with Swing UI.
	 */
	swing,
	
	/**
	 * Installation with web UI. 
	 */
	web,
	
	/**
	 * Pre-configured installation following only default values.
	 */
	quiet,
	
	/**
	 * Automatically guess UI type. 
	 */
	auto;
	
	/**
	 * Get an UI type by name.
	 * @param name The name of the UI type
	 * @return The UI type if found or <code>null</code> if no
	 * 		UI type matches the given string
	 */
	public static UIType getByName(String name)
	{
		for (UIType type : values())
			if(name.toLowerCase().equals(type.toString().toLowerCase()))
				return type;
		return null;
	}
}