package org.nightlabs.installer.elements.ui.swing;

import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.SwingUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class CheckElementUI extends SwingElementUI
{
	private JCheckBox checkBox = null;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultUI#render()
	 */
	@Override
	public void render() throws InstallationException
	{
		GridBagConstraints gbc = SwingUI.getGBC(0, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 3;
		SwingUI.getActivePage().add(getCheckBox(), gbc, this);
	}

	protected JCheckBox getCheckBox() throws InstallationException
	{
		if(checkBox == null) {
			checkBox = new JCheckBox();
			if(haveString("annotation"))
				checkBox.setText(String.format("%1$s (%2$s)", getString(Constants.LABEL), getString(Constants.ANNOTATION)));
			else
				checkBox.setText(getString(Constants.LABEL));
			checkBox.setSelected(Constants.CHECK_TRUE.equals(getInstallationEntity().getResult(Constants.RESULT)));
		}
		return checkBox;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.elements.ui.swing.SwingElementUI#getResult()
	 */
	@Override
	public String getResult() throws InstallationException
	{
		if(getCheckBox().isSelected())
			return Constants.CHECK_TRUE;
		else
			return Constants.CHECK_FALSE;
	}
}
