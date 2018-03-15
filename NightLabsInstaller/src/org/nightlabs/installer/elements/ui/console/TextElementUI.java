package org.nightlabs.installer.elements.ui.console;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.ConsoleFormatter;
import org.nightlabs.installer.base.ui.ConsoleUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class TextElementUI extends ConsoleUI
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultUI#render()
	 */
	@Override
	public void render() throws InstallationException
	{
		ConsoleFormatter f = getConsoleFormatter();
		f.println(getString("text"));
		f.println();
	}
}
