package org.nightlabs.installer.base.defaults;

import java.util.List;

import org.nightlabs.installer.base.Element;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.Page;

/**
 * The default page implementation. This implementation does nothing.
 * 
 * @version $Revision: 896 $ - $Date: 2007-05-15 15:42:34 +0200 (Di, 15 Mai 2007) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DefaultPage extends DefaultInstallationEntity implements Page
{
	/**
	 * Elements within this page.
	 */
	private List<Element> elements;

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultInstallationEntity#getChildren()
	 */
	public List<Element> getChildren() throws InstallationException
	{
		if(elements == null) 
			elements = getChildren(DefaultElement.class);
		return elements;
	}
}
