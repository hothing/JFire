package org.nightlabs.installer.elements.ui.swing;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.nightlabs.installer.Constants;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class PasswordInputElementUI extends TextInputElementUI
{
	private JPasswordField textField;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.elements.ui.swing.TextInputElementUI#getTextField()
	 */
	@Override
	public JTextField getTextField()
	{
		if(textField == null) {
			textField = new JPasswordField();
			String defaultValue = getInstallationEntity().getResult(Constants.RESULT);
			if(defaultValue != null)
				textField.setText(defaultValue);
		}
		return textField;
	}

	@Override
	public String getResult() {
		return new String(textField.getPassword());
	}
}
