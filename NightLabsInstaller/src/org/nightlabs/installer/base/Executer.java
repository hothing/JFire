package org.nightlabs.installer.base;

/**
 * The excuter interface. Executers are called whenever an {@link InstallationEntity}
 * was run.
 * 
 * @version $Revision: 909 $ - $Date: 2007-05-17 16:07:01 +0200 (Do, 17 Mai 2007) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public interface Executer extends Configurable, InstallationEntityAssigned
{
	/**
	 * Do the work.
	 * @throws InstallationException in case of an error during execution
	 */
	void execute() throws InstallationException;
	
	/**
	 * Get the total amount work to do in this executer or <code>-1</code>
	 * for unknown amount of work.
	 * @return the total amount work to do in this executer or <code>-1</code>
	 */
	int getTotalWork();
}
