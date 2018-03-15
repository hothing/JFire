package org.nightlabs.installer.base.defaults;

import java.util.Properties;

import org.nightlabs.installer.base.Configurable;

/**
 * The default implementation for the {@link Configurable} interface.
 * 
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @version $Revision: 896 $ - $Date: 2007-05-15 15:42:34 +0200 (Di, 15 Mai 2007) $
 */
public class DefaultConfigurable implements Configurable
{
	/**
	 * The config properties.
	 */
	private Properties config;

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.Configurable#getConfig()
	 */
	public Properties getConfig()
	{
		return config;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.Configurable#setConfig(java.util.Properties)
	 */
	public void setConfig(Properties config)
	{
		this.config = config;
	}
}
