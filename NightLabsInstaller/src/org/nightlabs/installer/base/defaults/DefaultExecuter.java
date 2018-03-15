package org.nightlabs.installer.base.defaults;

import org.nightlabs.installer.base.Executer;
import org.nightlabs.installer.base.InstallationException;



/**
 * The default {@link Executer} implementation. This implementation
 * does nothing.
 *
 * @version $Revision: 1325 $ - $Date: 2008-07-04 17:44:23 +0200 (Fr, 04 Jul 2008) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DefaultExecuter extends DefaultWorker implements Executer
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.Executer#execute()
	 */
	public void execute() throws InstallationException
	{
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.Executer#getTotalWork()
	 */
	public int getTotalWork()
	{
		return -1;
	}
}
