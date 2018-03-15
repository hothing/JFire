package org.nightlabs.installer.base;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @version $Revision$ - $Date$
 */
public interface Navigator extends Configurable, InstallationEntityAssigned
{
	public enum Navigation {
		back,
		next,
//		again,
//		cancel,
//		finish
	}
	
	Navigation getNavigation() throws InstallationException;
	void setNavigation(Navigation navigation);
}
