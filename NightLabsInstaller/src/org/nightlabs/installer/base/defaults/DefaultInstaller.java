package org.nightlabs.installer.base.defaults;

import java.awt.Rectangle;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;

import org.nightlabs.installer.InstallationManager;
import org.nightlabs.installer.Logger;
import org.nightlabs.installer.Messages;
import org.nightlabs.installer.UIType;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.Installer;
import org.nightlabs.installer.base.Page;
import org.nightlabs.installer.base.ui.SwingUI;
import org.nightlabs.installer.base.ui.WebUI;

/**
 * The default installer implementation. This implementation
 * shows the swing frame with a given wizard icon if on swing ui
 * and starts the web server for web ui.
 *
 * @version $Revision: 1891 $ - $Date: 2009-08-24 20:53:28 +0200 (Mo, 24 Aug 2009) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DefaultInstaller extends DefaultInstallationEntity implements Installer
{
	/**
	 * The pages in this installer.
	 */
	private List<Page> pages;

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultInstallationEntity#getChildren()
	 */
	@Override
	public List<Page> getChildren() throws InstallationException
	{
		if(pages == null)
			pages = getChildren(DefaultPage.class);
		return pages;
	}

	public DefaultInstaller()
	{
		UIType uiType = InstallationManager.getInstallationManager().getUiType();
		if(uiType == UIType.swing) {
			URL wizardIconURL = getWizardIconURL();
			if(wizardIconURL != null) {
				ImageIcon imageIcon = new ImageIcon(wizardIconURL, Messages.getString("DefaultInstaller.wizardIconDescription"));  //$NON-NLS-1$ //$NON-NLS-2$
				if(imageIcon != null)
					SwingUI.getInstallerFrame().setPageIcon(imageIcon);
			}
			Rectangle bounds = getInstallerFrameBounds();
			if(bounds != null) {
				SwingUI.getInstallerFrame().setBounds(bounds);
			}
		} else if(uiType == UIType.web) {
			WebUI webUI = WebUI.sharedInstance();
			URL wizardIconURL = getWizardIconURL();
			if(wizardIconURL != null) {
				webUI.setInstallerWizardIcon(wizardIconURL);
			}
			Logger.out.println("Starting web server..."); //$NON-NLS-1$
			try {
				webUI.startServer();
			} catch (InstallationException e) {
				throw new RuntimeException("Starting web server failed");
			}
			Logger.out.println("Web server is up and running"); //$NON-NLS-1$
		}
	}

	/**
	 * Get the URL of an image to be shown in the installer swing wizard.
	 * @return The URL of the wizard icon or <code>null</code> to show no image.
	 */
	protected URL getWizardIconURL()
	{
		return Installer.class.getResource(Messages.getString("DefaultInstaller.wizardIcon"));
	}

	/**
	 * Get optional installer frame bounds. Implementations may wish to get these
	 * values from a resource bundle or somewhere else.
	 * <p>
	 * See {@link org.nightlabs.installer.pages.ui.swing.InstallerFrame}
	 * for default values. Currently, they are x=50, y=80, width=700, height=550.
	 * </p>
	 * @return The installer frame bounds or <code>null</code> to use the default bounds.
	 */
	protected Rectangle getInstallerFrameBounds()
	{
		return null;
	}
}
