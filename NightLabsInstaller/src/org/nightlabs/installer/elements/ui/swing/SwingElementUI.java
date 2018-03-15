package org.nightlabs.installer.elements.ui.swing;

import javax.swing.JFileChooser;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class SwingElementUI extends DefaultUI
{
	private volatile JFileChooser fileChooser;
//	private Object fileChooserMutex = new Object();

	public SwingElementUI()
	{
// The getFileChooser() method sometimes blocks forever. Even though we found quite a few posts in the web that say it's a JFileChooser bug,
// it might be related to using it on the non-Swing-Thread => therefore commented this out. Marco.
//		// pre-load file chooser in background
//		if(fileChooser == null) {
//			Thread thread = new Thread() {
//				/* (non-Javadoc)
//				 * @see java.lang.Thread#run()
//				 */
//				@Override
//				public void run()
//				{
//					try {
//						getFileChooser();
//					} catch(Throwable e) {
//						// hide all errors that may occur
//					}
//				}
//			};
//			thread.setName("filechooser-loader");
//			thread.setDaemon(true);
//			thread.start();
//		}
	}

	public String getResult() throws InstallationException
	{
		return null;
	}

	protected JFileChooser getFileChooser()
	{
		if(fileChooser == null) {
			fileChooser = new JFileChooser();

			// No need to synchronize anymore, because we only work on the Swing UI Thread now. Marco.
//			synchronized (fileChooserMutex) {
//				if(fileChooser == null)
//					fileChooser = new JFileChooser();
//			}
		}
		return fileChooser;
	}
}
