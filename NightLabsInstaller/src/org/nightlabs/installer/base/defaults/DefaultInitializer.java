package org.nightlabs.installer.base.defaults;

import org.nightlabs.installer.base.Initializer;
import org.nightlabs.installer.base.InstallationException;

/**
 * The default {@link Initializer} implementation. This implementation
 * does nothing.
 *
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @version $Revision$ - $Date$
 */
public class DefaultInitializer extends DefaultWorker implements Initializer
{

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.Initializer#getTotalWork()
	 */
	public int getTotalWork()
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.Initializer#initialize()
	 */
	public void initialize() throws InstallationException
	{
	}
}
