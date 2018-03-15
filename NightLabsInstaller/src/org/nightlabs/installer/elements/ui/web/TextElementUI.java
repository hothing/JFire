package org.nightlabs.installer.elements.ui.web;

import java.io.IOException;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.WebUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class TextElementUI extends WebElementUI
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultUI#render()
	 */
	@Override
	public void render() throws InstallationException
	{
		super.render();
		try {
			WebUI w = WebUI.sharedInstance();
			w.openElementDiv(getElement());
			w.writeFormattedText(getString("text"));
			w.closeDiv();
		} catch (IOException e) {
			throw new InstallationException("Rendering failed", e);
		}
	}
}
