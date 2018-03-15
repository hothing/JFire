package org.nightlabs.installer.base.defaults;

import java.util.List;

import org.nightlabs.installer.base.InstallationEntity;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.VisibilityDecider;


/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DefaultVisibilityDecider extends DefaultWorker implements VisibilityDecider
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.VisibilityDecider#isVisible()
	 */
	public boolean isVisible() throws InstallationException
	{
		List<? extends InstallationEntity> children = getInstallationEntity().getChildren();
		if(children == null || children.isEmpty())
			return true;
		for (InstallationEntity child : children) {
			VisibilityDecider visibilityDecider = child.getVisibilityDecider();
			if(visibilityDecider != null && visibilityDecider.isVisible())
				return true;
		}
		return false;
	}
}
