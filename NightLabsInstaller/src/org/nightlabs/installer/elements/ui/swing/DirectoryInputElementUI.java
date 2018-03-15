package org.nightlabs.installer.elements.ui.swing;

import javax.swing.JFileChooser;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DirectoryInputElementUI extends FileInputElementUI
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.elements.ui.swing.FileElementUI#getFileChooser()
	 */
	@Override
	protected JFileChooser getFileChooser()
	{
		JFileChooser fileChooser = super.getFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//		try {
//			File f = new File(getResult());
//			//fileChooser.setCurrentDirectory(f);
//			fileChooser.setSelectedFile(f);
//		} catch(Throwable e) {}
		return fileChooser;
	}
}
