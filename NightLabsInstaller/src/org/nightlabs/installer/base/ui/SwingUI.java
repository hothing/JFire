package org.nightlabs.installer.base.ui;

import java.awt.GridBagConstraints;

import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.nightlabs.installer.pages.ui.swing.InstallerFrame;
import org.nightlabs.installer.pages.ui.swing.SwingPageUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class SwingUI
{
	private static SwingPageUI activePage;
	
	private static InstallerFrame installerFrame = null;
	
	public static InstallerFrame getInstallerFrame()
	{
		if(installerFrame == null) {
			try {
				UIManager.setLookAndFeel((LookAndFeel)Class.forName(UIManager.getSystemLookAndFeelClassName()).newInstance());
			} catch(Throwable e) {} 
			installerFrame = new InstallerFrame();
			installerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		return installerFrame;
	}

	/**
	 * Get the activePage.
	 * @return the activePage
	 */
	public static SwingPageUI getActivePage()
	{
		return activePage;
	}

	/**
	 * Set the activePage.
	 * @param activePage the activePage to set
	 */
	public static void setActivePage(SwingPageUI activePage)
	{
		SwingUI.activePage = activePage;
	}

	public static GridBagConstraints getGBC(int x, int y)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		return gbc;
	}
}
