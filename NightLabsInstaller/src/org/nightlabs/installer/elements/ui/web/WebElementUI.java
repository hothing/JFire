package org.nightlabs.installer.elements.ui.web;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.base.Element;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.Page;
import org.nightlabs.installer.base.defaults.DefaultUI;
import org.nightlabs.installer.base.ui.WebUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class WebElementUI extends DefaultUI
{
	protected Element getElement()
	{
		return (Element)getInstallationEntity();
	}
	
	protected Page getPage()
	{
		return (Page)getElement().getParent();
	}
	
	protected String getFullId()
	{
		return getPage().getId()+Constants.SEPARATOR+getElement().getId();
	}
	
	protected String getResult() throws InstallationException
	{
		return WebUI.sharedInstance().getParameterValue(getFullId());
	}
	
	public void storeResult() throws InstallationException
	{
		String result = getResult();
		if(result != null)
			getElement().setResult("result", result);
	}
}
