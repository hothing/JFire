package org.nightlabs.installer.pages.ui.console;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.ConsoleFormatter;
import org.nightlabs.installer.base.ui.ConsoleUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class TitlePageUI extends ConsoleUI
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultUI#renderBefore()
	 */
	@Override
	public void renderBefore() throws InstallationException
	{
		super.renderBefore();
		ConsoleFormatter f = getConsoleFormatter();
		f.printlnCentered(getString("title"));
		f.println();
	}
}
