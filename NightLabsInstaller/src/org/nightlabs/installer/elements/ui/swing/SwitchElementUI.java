package org.nightlabs.installer.elements.ui.swing;

import java.awt.GridBagConstraints;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.SwingUI;
import org.nightlabs.installer.elements.SwitchElement;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class SwitchElementUI extends SwingElementUI
{
	Map<JRadioButton, String> idForButton;

	@Override
	public void render() throws InstallationException
	{
		super.render();

		int y=0;
		GridBagConstraints gbc;

		JLabel label = new JLabel();
		if(haveString(Constants.ANNOTATION))
			label.setText(String.format("%1$s (%2$s)", getString(Constants.LABEL), getString(Constants.ANNOTATION)));
		else
			label.setText(String.format("%s", getString(Constants.LABEL)));
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy = y++;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 3;
		gbc.insets.bottom += 5;
		SwingUI.getActivePage().add(label, gbc, this);


		ButtonGroup group = new ButtonGroup();

		SwitchElement switchElement = (SwitchElement)getInstallationEntity();
		SortedMap<String, String> options = switchElement.getOptions();
		String defaultOption = switchElement.getDefaultOption();
		idForButton = new HashMap<JRadioButton, String>(options.size());
		for (String optionID : options.keySet()) {
			JRadioButton b1 = new JRadioButton(getString(Constants.SWITCH_OPTION+Constants.SEPARATOR+optionID));
			if(defaultOption != null && defaultOption.equals(switchElement.getOptionValue(optionID)))
				b1.setSelected(true);
			gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridx = 0;
			gbc.gridy = y++;
			gbc.weightx = 1.0;
//			gbc.weighty = 1.0;
			gbc.gridwidth = 3;
			gbc.anchor = GridBagConstraints.WEST;
			SwingUI.getActivePage().add(b1, gbc, this);
			group.add(b1);
			idForButton.put(b1, optionID);
		}
	}

	@Override
	public String getResult()
	{
		SwitchElement switchElement = (SwitchElement)getInstallationEntity();
		for (JRadioButton b : idForButton.keySet()) {
			if(b.isSelected()) {
				String optionID = idForButton.get(b);
				return switchElement.getOptionValue(optionID);
			}
		}
		throw new IllegalStateException("No button selected");
	}
}
