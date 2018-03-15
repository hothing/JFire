package org.nightlabs.installer.base.defaults;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.UI;


/**
 * The default user interface implementation.
 * 
 * @version $Revision: 905 $ - $Date: 2007-05-17 14:19:21 +0200 (Do, 17 Mai 2007) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DefaultUI extends DefaultWorker implements UI
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.UI#render()
	 */
	public void render() throws InstallationException
	{
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.UI#renderAfter()
	 */
	public void renderAfter() throws InstallationException
	{
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.UI#renderBefore()
	 */
	public void renderBefore() throws InstallationException
	{
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.UI#getString(java.lang.String)
	 */
	public String getString(String key) throws InstallationException
	{
		return getInstallationEntity().getString(key);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.UI#haveString(java.lang.String)
	 */
	public boolean haveString(String key) throws InstallationException
	{
		return getInstallationEntity().haveString(key);
	}
}
