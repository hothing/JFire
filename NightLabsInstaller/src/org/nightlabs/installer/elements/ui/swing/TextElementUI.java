package org.nightlabs.installer.elements.ui.swing;

import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.SwingUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class TextElementUI extends SwingElementUI
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultUI#render()
	 */
	@Override
	public void render() throws InstallationException
	{
		JTextArea textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		textArea.setText(getString("text"));
		textArea.setBorder(BorderFactory.createEmptyBorder());
		textArea.setFont(SwingUI.getInstallerFrame().getFont());
		textArea.setBackground(SwingUI.getInstallerFrame().getBackground());

//		JScrollPane areaScrollPane = new JScrollPane();
//		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//		areaScrollPane.setBorder(BorderFactory.createEmptyBorder());
//		areaScrollPane.setBackground(SwingUI.getInstallerFrame().getBackground());
//		areaScrollPane.setViewportView(textArea);

		GridBagConstraints gbc = SwingUI.getGBC(0, 0);
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
//		gbc.weighty = 1000.0;
		SwingUI.getActivePage().add(textArea, gbc, this);
	}
}
