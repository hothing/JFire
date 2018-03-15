package org.nightlabs.installer.elements.ui.web;

import java.io.IOException;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.WebUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class TextInputElementUI extends WebElementUI
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
			w.writeText(getString("label"));
			w.writeText(": ");
			w.openInput("textinput", getFullId(), "text", getFullId(), getElement().getResult("result"), null);
			if(haveAnnotation()) {
				w.openDiv("annotation", getFullId()+".annotation");
				w.write(WebUI.htmlEntities(getAnnotationText()));
				w.closeDiv();
			}
			w.closeDiv();
		} catch (IOException e) {
			throw new InstallationException("Rendering failed", e);
		}

	}

	protected boolean haveAnnotation() throws InstallationException
	{
		return haveString(Constants.ANNOTATION);
	}

	protected String getAnnotationText() throws InstallationException
	{
		return String.format("(%s)", getString(Constants.ANNOTATION));
	}
}
