package org.nightlabs.installer.base.ui;

import java.util.List;

import org.nightlabs.installer.base.InstallationEntity;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.Navigator.Navigation;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class NavigationUI
{
	public static void setNavigation(InstallationEntity entity, Navigation navigation) throws InstallationException
	{
		entity.getNavigator().setNavigation(navigation);
		List<? extends InstallationEntity> children = entity.getChildren();
		if(children != null)
			for (InstallationEntity childEntity : children) {
				setNavigation(childEntity, navigation);
			}
	}
}
