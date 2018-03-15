package org.nightlabs.installer.elements.ui.console;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.ConsoleUI;

/**
 * @version $Revision: 946 $ - $Date: 2007-06-02 17:12:06 +0200 (Sa, 02 Jun 2007) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class LabelElementUI extends ConsoleUI
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultUI#renderBefore()
	 */
	@Override
	public void renderBefore() throws InstallationException
	{
		super.renderBefore();
		getConsoleFormatter().println(getString("label"));
	}
}
