package org.nightlabs.installer.elements.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import org.nightlabs.installer.Messages;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.SwingUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class FileInputElementUI extends TextInputElementUI implements ActionListener
{
	private JButton searchButton = null;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.elements.ui.swing.TextInputElementUI#render()
	 */
	@Override
	public void render() throws InstallationException
	{
		super.render();
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets.left = 5;
		SwingUI.getActivePage().add(getSearchButton(), gbc, this);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.elements.ui.swing.TextInputElementUI#getTextFieldConstraints()
	 */
	@Override
	protected GridBagConstraints getTextFieldConstraints()
	{
		GridBagConstraints gbc = super.getTextFieldConstraints();
		gbc.gridwidth = 1;
		return gbc;
	}

	/**
	 * Get the searchButton.
	 * @return the searchButton
	 */
	protected JButton getSearchButton()
	{
		if(searchButton == null) {
			searchButton = new JButton();
			searchButton.setText(Messages.getString("FileInputElementUI.searchButtonLabel")); //$NON-NLS-1$
			searchButton.addActionListener(this);
		}
		return searchButton;
	}

	public void actionPerformed(ActionEvent e)
	{
		JFileChooser fileChooser = getFileChooser();
    int returnVal = fileChooser.showOpenDialog(SwingUI.getInstallerFrame());
    if(returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			getTextField().setText(f.getAbsolutePath());
    }
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.elements.ui.swing.SwingElementUI#getFileChooser()
	 */
	@Override
	protected JFileChooser getFileChooser()
	{
		JFileChooser fileChooser = super.getFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		try {
			File f = new File(getResult());
			//fileChooser.setCurrentDirectory(f);
			fileChooser.setSelectedFile(f);
		} catch(Throwable e) {}
		return fileChooser;
	}
}
