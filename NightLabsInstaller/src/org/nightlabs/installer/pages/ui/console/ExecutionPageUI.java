package org.nightlabs.installer.pages.ui.console;

import org.nightlabs.installer.ExecutionProgressEvent;
import org.nightlabs.installer.ExecutionProgressListener;
import org.nightlabs.installer.InstallationManager;
import org.nightlabs.installer.Messages;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.ConsoleUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ExecutionPageUI extends ConsoleUI
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultUI#render()
	 */
	@Override
	public void render() throws InstallationException
	{
		getConsoleFormatter().printlnCentered(Messages.getString("ExecutionPageUI.installationStarting")); //$NON-NLS-1$
		InstallationManager.getInstallationManager().addExecutionProgressListener(new ExecutionProgressListener() {
			public void executionProgress(ExecutionProgressEvent e)
			{
				if(e.getType() == ExecutionProgressEvent.Type.done) {
					System.out.println(Messages.getString("ExecutionPageUI.installationDone")); //$NON-NLS-1$
					System.exit(0);
				}
				System.out.println("  * "+e.getDescription());
			}
		});
	}
}
