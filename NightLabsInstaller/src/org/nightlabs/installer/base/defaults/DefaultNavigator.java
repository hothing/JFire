package org.nightlabs.installer.base.defaults;

import java.util.List;

import org.nightlabs.installer.base.InstallationEntity;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.Navigator;


/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DefaultNavigator extends DefaultWorker implements Navigator
{
	Navigation navigation = null;
	
	public Navigation getNavigation() throws InstallationException
	{
		if(navigation == null) {
			List<? extends InstallationEntity> children = getInstallationEntity().getChildren();
			if(children != null) {
				for (InstallationEntity entity : children) {
					Navigation childNavigation = entity.getNavigator().getNavigation();
					if(childNavigation != Navigation.next)
						return childNavigation;
				}
			}
			return Navigation.next;
		}
		return navigation;
	}

	public void setNavigation(Navigation navigation)
	{
		this.navigation = navigation;
	}
}
