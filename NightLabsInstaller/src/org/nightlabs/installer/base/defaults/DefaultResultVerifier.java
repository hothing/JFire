package org.nightlabs.installer.base.defaults;

import org.nightlabs.installer.base.ResultVerifier;
import org.nightlabs.installer.base.VerificationException;


/**
 * The default result verifier implementation. This implementation
 * does nothing.
 * 
 * @version $Revision: 1049 $ - $Date: 2007-09-10 16:24:26 +0200 (Mo, 10 Sep 2007) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DefaultResultVerifier extends DefaultWorker implements ResultVerifier
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.ResultVerifier#verify(org.nightlabs.installer.InstallEntity)
	 */
	public void verify() throws VerificationException
	{
	}
}
