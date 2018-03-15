package org.nightlabs.installer.base.ui;

import org.nightlabs.installer.base.defaults.DefaultUI;

/**
 * @version $Revision: 946 $ - $Date: 2007-06-02 17:12:06 +0200 (Sa, 02 Jun 2007) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ConsoleUI extends DefaultUI
{
	private static ConsoleFormatter consoleFormatter = null;
	public static ConsoleFormatter getConsoleFormatter()
	{
		if(consoleFormatter == null)
			consoleFormatter = new ConsoleFormatter();
		return consoleFormatter;
	}
}
