package org.nightlabs.installer.elements.ui.web;

import java.io.IOException;
import java.util.SortedMap;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.WebUI;
import org.nightlabs.installer.elements.SwitchElement;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class SwitchElementUI extends WebElementUI
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultUI#render()
	 */
	@Override
	public void render() throws InstallationException
	{
		super.render();

		try {
			SwitchElement switchElement = (SwitchElement)getElement();
			
			WebUI w = WebUI.sharedInstance();
			w.openElementDiv(switchElement);
			
			// label
			w.openElementPartDiv("switchlabel");
			if(haveString(Constants.ANNOTATION))
				w.writeText(String.format("%1$s (%2$s)", getString(Constants.LABEL), getString(Constants.ANNOTATION)));
			else
				w.writeText(String.format("%s", getString(Constants.LABEL)));
			w.closeDiv();
			
			// radiobuttons
			w.openElementPartDiv("switchradiogroup");
			SortedMap<String, String> options = switchElement.getOptions();
			String defaultOption = switchElement.getDefaultOption();
			for (String optionID : options.keySet()) {
				// radiobutton
				w.openElementPartDiv("switchradiobutton");
				String optionValue = options.get(optionID);
				w.emptyInput(
						"switchinput", 
						getFullId()+Constants.SWITCH_OPTION+Constants.SEPARATOR+optionID, 
						"radio", 
						getFullId(), 
						optionValue,
						optionValue.equals(defaultOption) ? new WebUI.ElementAttribute("checked", "checked") : null);
				// radiobutton label
				w.writeText(getString(Constants.SWITCH_OPTION+Constants.SEPARATOR+optionID));
				w.closeDiv();
			}						
			w.closeDiv();

			w.closeDiv();
			
		} catch (IOException e) {
			throw new InstallationException("Rendering failed", e);
		}
	}
}
