package org.nightlabs.installer.base;

/**
 * An interface providing a user interface.
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @version $Revision$ - $Date$
 */
public interface UIProvider extends Configurable
{
	/**
	 * Get the user interface implementation.
	 * @return the user interface implementation.
	 * @throws InstallationException In case of an error
	 */
	UI getUI() throws InstallationException;
}
