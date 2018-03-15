package org.nightlabs.installer.base;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public interface UI extends Configurable, InstallationEntityAssigned
{
	void renderBefore() throws InstallationException;
	void render() throws InstallationException;
	void renderAfter() throws InstallationException;
	String getString(String key) throws InstallationException;
	boolean haveString(String key) throws InstallationException;
}
