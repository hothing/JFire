package org.nightlabs.installer.pages.ui.quiet;

import org.nightlabs.installer.ExecutionProgressEvent;
import org.nightlabs.installer.ExecutionProgressListener;
import org.nightlabs.installer.InstallationManager;
import org.nightlabs.installer.Logger;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ExecutionPageUI extends DefaultUI
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultUI#render()
	 */
	@Override
	public void render() throws InstallationException
	{
		super.render();
		if(Logger.isVerbose())
			InstallationManager.getInstallationManager().addExecutionProgressListener(new ExecutionProgressListener()
			{
				public void executionProgress(ExecutionProgressEvent e)
				{
					Logger.out.println(e.getWorkDone()+" - "+e.getDescription());
					if(e.getType() == ExecutionProgressEvent.Type.done) {
						Logger.out.println("DONE.");
						System.exit(0);
					}
				}
			});
	}
}
