package org.nightlabs.installer.pages.ui.web;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.base.InstallationException;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class TitlePageUI extends PageUI
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.pages.ui.web.WebPageUI#getTitle()
	 */
	@Override
	protected String getTitle() throws InstallationException
	{
		return super.getTitle() + " : " + getString("title");
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.pages.ui.web.WebPageUI#getAnnotation()
	 */
	@Override
	protected String getAnnotation() throws InstallationException
	{
		return haveString(Constants.ANNOTATION) ? getString(Constants.ANNOTATION) : null;
	}
}
