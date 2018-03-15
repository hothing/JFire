package org.nightlabs.installer.pages.ui.swing;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.SwingUI;


/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class TitlePageUI extends PageUI
{
	@Override
	public void renderBefore() throws InstallationException
	{
		super.renderBefore();
		SwingUI.getInstallerFrame().setHeaderText(
				getString("title"),  //$NON-NLS-1$
				(haveString("annotation") ? getString("annotation") : null)); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
