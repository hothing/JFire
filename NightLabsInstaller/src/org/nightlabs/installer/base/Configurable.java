package org.nightlabs.installer.base;

import java.util.Properties;

/**
 * An interface for configurable classes. The config is stored
 * with {@link Properties}.
 * 
 * @version $Revision: 895 $ - $Date: 2007-05-15 11:58:45 +0200 (Di, 15 Mai 2007) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public interface Configurable
{
	/**
	 * Set the config.
	 * @param config The config to set
	 */
	void setConfig(Properties config);
	
	/**
	 * Get the config.
	 * @return the config.
	 */
	Properties getConfig();
}
