package org.nightlabs.installer.base;

/**
 * The initializer interface. Initializers are called before an {@link InstallationEntity}
 * is run.
 *
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @version $Revision$ - $Date$
 */
public interface Initializer extends Configurable, InstallationEntityAssigned
{
	/**
	 * Do the work.
	 * @throws InstallationException in case of an error during initialization
	 */
	void initialize() throws InstallationException;

	/**
	 * Get the total amount work to do in this initializer or <code>-1</code>
	 * for unknown amount of work.
	 * @return the total amount work to do in this initializer or <code>-1</code>
	 */
	int getTotalWork();
}
